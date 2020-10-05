package com.ldtteam.aequivaleo.analyzer;

import com.google.common.collect.*;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.information.locked.ILockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import com.ldtteam.aequivaleo.bootstrap.WorldBootstrapper;
import com.ldtteam.aequivaleo.api.compound.information.CompoundInstanceData;
import com.ldtteam.aequivaleo.compound.data.serializers.CompoundInstanceDataSerializer;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.ldtteam.aequivaleo.results.ResultsInformationCache;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class AequivaleoReloadListener extends ReloadListener<Map<ResourceLocation, List<CompoundInstanceData>>>
{
    private static final String JSON_EXTENSION = ".json";
    private static final int JSON_EXTENSION_LENGTH = JSON_EXTENSION.length();

    private static final Logger LOGGER = LogManager.getLogger(AequivaleoReloadListener.class);
    private static final ResourceLocation GENERAL_DATA_NAME = new ResourceLocation(Constants.MOD_ID, "general");

    @SubscribeEvent
    public static void onAddReloadListener(final AddReloadListenerEvent reloadListenerEvent)
    {
        LOGGER.info("Registering reload listener for graph rebuilding.");
        reloadListenerEvent.addListener(new AequivaleoReloadListener());
    }

    @SubscribeEvent
    public static void onServerStarted(final FMLServerStartedEvent serverStartedEvent)
    {
        LOGGER.info("Building initial equivalency graph.");
        reloadResources(parseData(serverStartedEvent.getServer().getDataPackRegistries().getResourceManager()));
    }

    @Override
    protected Map<ResourceLocation, List<CompoundInstanceData>> prepare(final IResourceManager resourceManagerIn, final IProfiler profilerIn)
    {
        return parseData(resourceManagerIn);
    }

    @Override
    protected void apply(final Map<ResourceLocation, List<CompoundInstanceData>> objectIn, final IResourceManager resourceManagerIn, final IProfiler profilerIn)
    {
        LOGGER.info("Reloading resources has been triggered, recalculating graph.");
        reloadResources(objectIn);
    }

    private static Map<ResourceLocation, List<CompoundInstanceData>> parseData(final IResourceManager resourceManager) {
        if (ServerLifecycleHooks.getCurrentServer() == null)
        {
            return ImmutableMap.of();
        }

        final Map<ResourceLocation, List<CompoundInstanceData>> data = new HashMap<>();
        final List<ServerWorld> worlds = Lists.newArrayList(ServerLifecycleHooks.getCurrentServer().getWorlds());

        data.put(
          GENERAL_DATA_NAME,
          read(
            resourceManager,
          "aequivaleo/locked/general"
          )
        );

        worlds.forEach(world -> {
            final String path = "aequivaleo/locked/" + world.getDimensionKey().getLocation().getNamespace() + "/" + world.getDimensionKey().getLocation().getPath();

            data.put(
              world.getDimensionKey().getLocation(),
              read(
                resourceManager,
                path
              )
            );
        });

        return data;
    }

    @NotNull
    private static List<CompoundInstanceData> read(@NotNull final IResourceManager resourceManager, @NotNull final String targetPath)  {
        final List<CompoundInstanceData> collectedData = Lists.newArrayList();
        final int targetPathLength = targetPath.length() + 1; //Account for the seperator.

        final Gson gson = IAequivaleoAPI.getInstance().getGson();

        resourceManager.getAllResourceLocations(targetPath, s -> s.endsWith(JSON_EXTENSION)).forEach(resourceLocation -> {
            String locationPath = resourceLocation.getPath();
            ResourceLocation resourceLocationWithoutExtension = new ResourceLocation(resourceLocation.getNamespace(), locationPath.substring(targetPathLength, locationPath.length() - JSON_EXTENSION_LENGTH));

            try (
              IResource iresource = resourceManager.getResource(resourceLocation);
              InputStream inputstream = iresource.getInputStream();
              Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
            ) {
                CompoundInstanceData data = gson.fromJson(reader, CompoundInstanceDataSerializer.HANDLED_TYPE);
                if (data != null) {
                    collectedData.add(data);
                } else {
                    LOGGER.error("Couldn't load data file {} from {} as it's null or empty", resourceLocationWithoutExtension, resourceLocation);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
                LOGGER.error("Couldn't parse data file {} from {}", resourceLocationWithoutExtension, resourceLocation, jsonparseexception);
            }
        });

        return collectedData;
    }

    private static void reloadResources(final Map<ResourceLocation, List<CompoundInstanceData>> data)
    {
        if (data.isEmpty() || ServerLifecycleHooks.getCurrentServer() == null)
            return;

        final List<ServerWorld> worlds = Lists.newArrayList(ServerLifecycleHooks.getCurrentServer().getWorlds());

        LOGGER.info("Analyzing information");
        final ClassLoader classLoader = Aequivaleo.class.getClassLoader();
        final AtomicInteger genericThreadCounter = new AtomicInteger();
        final int maxThreadCount = Math.max(4, Runtime.getRuntime().availableProcessors() - 2);
        final ExecutorService aequivaleoReloadExecutor = Executors.newFixedThreadPool(maxThreadCount, runnable -> {
            final Thread thread = new Thread(runnable);
            thread.setContextClassLoader(classLoader);
            thread.setName(String.format("Aequivaleo analysis runner: %s", genericThreadCounter.incrementAndGet()));
            return thread;
        });

        CompletableFuture.allOf(worlds.stream().map(world -> CompletableFuture.runAsync(
          new AequivaleoWorldAnalysisRunner(
            data.get(GENERAL_DATA_NAME),
            world,
            data.get(world.getDimensionKey().getLocation()
            )
          ),
          aequivaleoReloadExecutor
        )).toArray(CompletableFuture[]::new))
          .thenRunAsync(ResultsInformationCache::updateAllPlayers)
          .thenRunAsync(() -> {
              worlds.forEach(world -> {
                  PluginManger.getInstance().run(plugin -> plugin.onReloadFinishedFor(world));
                }
              );
          })
          .thenRunAsync(aequivaleoReloadExecutor::shutdown);
    }

    private static class AequivaleoWorldAnalysisRunner implements Runnable
    {

        private final List<CompoundInstanceData> generalData;
        private final ServerWorld serverWorld;
        private final List<CompoundInstanceData> worldData;

        private AequivaleoWorldAnalysisRunner(
          final List<CompoundInstanceData> generalData,
          final ServerWorld serverWorld,
          final List<CompoundInstanceData> worldData) {
            this.generalData = generalData;
            this.serverWorld = serverWorld;
            this.worldData = worldData;
        }

        @Override
        public void run()
        {
            reloadEquivalencyData();
        }

        private void reloadEquivalencyData()
        {
            LOGGER.info("Starting aequivaleo data reload for world: " + getServerWorld().getDimensionKey().getLocation().toString());
            try {
                WorldBootstrapper.onWorldReload(getServerWorld());

                final Map<ICompoundContainer<?>, Collection<CompoundInstanceData>> generalGroupedData = groupDataByContainer(generalData);
                final Map<ICompoundContainer<?>, Collection<CompoundInstanceData>> worldGroupedData = groupDataByContainer(worldData);

                final Map<ICompoundContainer<?>, Set<CompoundInstance>> targetMap = Maps.newHashMap();

                final Set<ICompoundContainer<?>> keySets = ImmutableSet.<ICompoundContainer<?>>builder()
                  .addAll(generalGroupedData.keySet())
                  .addAll(worldGroupedData.keySet())
                  .build();

                keySets.forEach(container -> {
                    generalGroupedData.getOrDefault(container, Sets.newHashSet()).stream().sorted(Comparator.comparingInt(compoundInstanceData -> compoundInstanceData.getMode()
                                                                                                                                                    .ordinal())).forEachOrdered(
                        compoundInstanceData -> compoundInstanceData.handle(targetMap)
                    );

                    worldGroupedData.getOrDefault(container, Sets.newHashSet()).stream().sorted(Comparator.comparingInt(compoundInstanceData -> compoundInstanceData.getMode()
                                                                                                                                                    .ordinal())).forEachOrdered(
                      compoundInstanceData -> compoundInstanceData.handle(targetMap)
                    );
                });

                targetMap.forEach(ILockedCompoundInformationRegistry.getInstance(getServerWorld().getDimensionKey())
                  ::registerLocking);

                JGraphTBasedCompoundAnalyzer analyzer = new JGraphTBasedCompoundAnalyzer(getServerWorld());
                ResultsInformationCache.getInstance(getServerWorld().getDimensionKey()).set(analyzer.calculateAndGet());
            } catch (Throwable t) {
                LOGGER.fatal(String.format("Failed to analyze: %s", getServerWorld().getDimensionKey().getLocation()), t);
            }
            LOGGER.info("Finished aequivaleo data reload for world: " + getServerWorld().getDimensionKey().getLocation().toString());
        }

        private static Map<ICompoundContainer<?>, Collection<CompoundInstanceData>> groupDataByContainer(final List<CompoundInstanceData> data) {
            return GroupingUtils.groupBy(
              data,
              CompoundInstanceData::getContainer
            )
              .stream()
              .collect(Collectors.toMap(
                c -> c.iterator().next().getContainer(),
                Function.identity()
              ));
        }

        public ServerWorld getServerWorld()
        {
            return serverWorld;
        }
    }
}
