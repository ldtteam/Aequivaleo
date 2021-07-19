package com.ldtteam.aequivaleo.analyzer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.information.ICompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.compound.information.datagen.data.CompoundInstanceData;
import com.ldtteam.aequivaleo.api.recipe.equivalency.GenericRecipeEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.data.GenericRecipeData;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import com.ldtteam.aequivaleo.bootstrap.WorldBootstrapper;
import com.ldtteam.aequivaleo.compound.data.serializers.CompoundInstanceDataSerializer;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.ldtteam.aequivaleo.recipe.equivalency.RecipeCalculator;
import com.ldtteam.aequivaleo.recipe.equivalency.data.GenericRecipeDataSerializer;
import com.ldtteam.aequivaleo.results.EquivalencyResults;
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
public class AequivaleoReloadListener extends ReloadListener<AequivaleoReloadListener.DataDrivenData>
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
        reloadResources(parseData(serverStartedEvent.getServer().getDataPackRegistries().getResourceManager()), false);
    }

    @NotNull
    @Override
    protected DataDrivenData prepare(@NotNull final IResourceManager resourceManagerIn, @NotNull final IProfiler profilerIn)
    {
        return parseData(resourceManagerIn);
    }

    @Override
    protected void apply(@NotNull final DataDrivenData objectIn, @NotNull final IResourceManager resourceManagerIn, @NotNull final IProfiler profilerIn)
    {
        LOGGER.info("Reloading resources has been triggered, recalculating graph.");
        reloadResources(objectIn, true);
    }

    private static DataDrivenData parseData(final IResourceManager resourceManager) {
        if (ServerLifecycleHooks.getCurrentServer() == null)
        {
            return new DataDrivenData();
        }

        final Map<ResourceLocation, List<CompoundInstanceData>> valueData = new HashMap<>();
        final Map<ResourceLocation, List<CompoundInstanceData>> lockedData = new HashMap<>();
        final Map<ResourceLocation, List<IEquivalencyRecipe>> additionalRecipes = new HashMap<>();
        final List<ServerWorld> worlds = Lists.newArrayList(ServerLifecycleHooks.getCurrentServer().getWorlds());

        valueData.put(
          GENERAL_DATA_NAME,
          readInstanceData(
            resourceManager,
            "aequivaleo/value/general"
          )
        );

        worlds.forEach(world -> {
            final String path = "aequivaleo/value/" + world.getDimensionKey().getLocation().getNamespace() + "/" + world.getDimensionKey().getLocation().getPath();

            valueData.put(
              world.getDimensionKey().getLocation(),
              readInstanceData(
                resourceManager,
                path
              )
            );
        });

        lockedData.put(
          GENERAL_DATA_NAME,
          readInstanceData(
            resourceManager,
          "aequivaleo/locked/general"
          )
        );

        worlds.forEach(world -> {
            final String path = "aequivaleo/locked/" + world.getDimensionKey().getLocation().getNamespace() + "/" + world.getDimensionKey().getLocation().getPath();

            lockedData.put(
              world.getDimensionKey().getLocation(),
              readInstanceData(
                resourceManager,
                path
              )
            );
        });

        additionalRecipes.put(
          GENERAL_DATA_NAME,
          readAdditionalRecipeData(
            resourceManager,
            "aequivaleo/recipes/general"
          )
        );

        worlds.forEach(world -> {
            final String path = "aequivaleo/recipes/" + world.getDimensionKey().getLocation().getNamespace() + "/" + world.getDimensionKey().getLocation().getPath();

            additionalRecipes.put(
              world.getDimensionKey().getLocation(),
              readAdditionalRecipeData(
                resourceManager,
                path
              )
            );
        });

        final DataDrivenData dataDrivenData = new DataDrivenData();
        dataDrivenData.valueData.putAll(valueData);
        dataDrivenData.lockedData.putAll(lockedData);
        dataDrivenData.dataDrivenRecipes.putAll(additionalRecipes);
        return dataDrivenData;
    }

    @NotNull
    private static List<CompoundInstanceData> readInstanceData(@NotNull final IResourceManager resourceManager, @NotNull final String targetPath)  {
        final List<CompoundInstanceData> collectedData = Lists.newArrayList();
        final int targetPathLength = targetPath.length() + 1; //Account for the seperator.

        final Gson gson = IAequivaleoAPI.getInstance().getGson();

        resourceManager.getAllResourceLocations(targetPath, s -> s.endsWith(JSON_EXTENSION)).forEach(resourceLocation -> {
            String locationPath = resourceLocation.getPath();
            ResourceLocation resourceLocationWithoutExtension = new ResourceLocation(resourceLocation.getNamespace(), locationPath.substring(targetPathLength, locationPath.length() - JSON_EXTENSION_LENGTH));

            try (
              IResource iresource = resourceManager.getResource(resourceLocation);
              InputStream inputstream = iresource.getInputStream();
              Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
            ) {
                CompoundInstanceData data = gson.fromJson(reader, CompoundInstanceDataSerializer.HANDLED_TYPE);
                if (data != null) {
                    collectedData.add(data);
                } else {
                    LOGGER.error("Couldn't load data file {} from {} as it's null or empty", resourceLocationWithoutExtension, resourceLocation);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException jsonParseException) {
                LOGGER.error("Couldn't parse data file {} from {}", resourceLocationWithoutExtension, resourceLocation, jsonParseException);
            }
        });

        return collectedData;
    }

    @NotNull
    private static List<IEquivalencyRecipe> readAdditionalRecipeData(@NotNull final IResourceManager resourceManager, @NotNull final String targetPath)  {
        final List<IEquivalencyRecipe> collectedData = Lists.newArrayList();
        final int targetPathLength = targetPath.length() + 1; //Account for the seperator.

        final Gson gson = IAequivaleoAPI.getInstance().getGson();

        resourceManager.getAllResourceLocations(targetPath, s -> s.endsWith(JSON_EXTENSION)).forEach(resourceLocation -> {
            String locationPath = resourceLocation.getPath();
            ResourceLocation resourceLocationWithoutExtension = new ResourceLocation(resourceLocation.getNamespace(), locationPath.substring(targetPathLength, locationPath.length() - JSON_EXTENSION_LENGTH));

            final ResourceLocation name = new ResourceLocation(resourceLocationWithoutExtension.getNamespace(), resourceLocationWithoutExtension.getPath().replace(targetPath, ""));

            try (
              IResource iresource = resourceManager.getResource(resourceLocation);
              InputStream inputstream = iresource.getInputStream();
              Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
            ) {
                GenericRecipeData data = gson.fromJson(reader, GenericRecipeDataSerializer.HANDLED_TYPE);
                if (data != null) {
                    if (data.getConditions().size() != 1 || data.getConditions().iterator().next().test())
                        collectedData.add(new GenericRecipeEquivalencyRecipe(name, data.getInputs(), data.getRequiredKnownOutputs(), data.getOutputs()));
                    else
                        LOGGER.info("Skipping the load of file {} from {} its conditions indicate it is disabled.", resourceLocationWithoutExtension, resourceLocation);
                } else {
                    LOGGER.error("Couldn't load data file {} from {} as it's null or empty", resourceLocationWithoutExtension, resourceLocation);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException jsonParseException) {
                LOGGER.error("Couldn't parse data file {} from {}", resourceLocationWithoutExtension, resourceLocation, jsonParseException);
            }
        });

        return collectedData;
    }

    private static void reloadResources(final DataDrivenData data, final boolean forceReload)
    {
        final Map<ResourceLocation, List<CompoundInstanceData>> valueData = data.valueData;
        final Map<ResourceLocation, List<CompoundInstanceData>> lockedData = data.lockedData;
        final Map<ResourceLocation, List<IEquivalencyRecipe>> additionalRecipes = data.dataDrivenRecipes;

        if ((lockedData.isEmpty() && valueData.isEmpty()) || ServerLifecycleHooks.getCurrentServer() == null)
            return;

        final List<ServerWorld> worlds = Lists.newArrayList(ServerLifecycleHooks.getCurrentServer().getWorlds());

        LOGGER.info("Analyzing information");
        final ClassLoader classLoader = Aequivaleo.class.getClassLoader();
        final AtomicInteger genericThreadCounter = new AtomicInteger();
        final int maxThreadCount = Math.max(1, Math.max(4, Runtime.getRuntime().availableProcessors() - 2));
        final ExecutorService aequivaleoReloadExecutor = Executors.newFixedThreadPool(maxThreadCount, runnable -> {
            final Thread thread = new Thread(runnable);
            thread.setContextClassLoader(classLoader);
            thread.setName(String.format("Aequivaleo analysis runner: %s", genericThreadCounter.incrementAndGet()));
            return thread;
        });

        RecipeCalculator.IngredientHandler.getInstance().reset();

        CompletableFuture.allOf(worlds.stream()
                                  .map(world -> CompletableFuture.runAsync(
          new AequivaleoWorldAnalysisRunner(
            world,
            valueData.get(GENERAL_DATA_NAME),
            valueData.get(world.getDimensionKey().getLocation()),
            lockedData.get(GENERAL_DATA_NAME),
            lockedData.get(world.getDimensionKey().getLocation()),
            additionalRecipes.get(GENERAL_DATA_NAME),
            additionalRecipes.get(world.getDimensionKey().getLocation()),
            forceReload),
          aequivaleoReloadExecutor
        )).toArray(CompletableFuture[]::new))
          .thenRunAsync(EquivalencyResults::updateAllPlayers)
          .thenRunAsync(() -> worlds.forEach(world -> PluginManger.getInstance().run(plugin -> plugin.onReloadFinishedFor(world))
          ))
          .thenRunAsync(() -> RecipeCalculator.IngredientHandler.getInstance().logErrors())
          .thenRunAsync(aequivaleoReloadExecutor::shutdown);
    }

    private static class AequivaleoWorldAnalysisRunner implements Runnable
    {

        private final ServerWorld                serverWorld;
        private final List<CompoundInstanceData> valueGeneralData;
        private final List<CompoundInstanceData> valueWorldData;
        private final List<CompoundInstanceData> lockedGeneralData;
        private final List<CompoundInstanceData> lockedWorldData;
        private final List<IEquivalencyRecipe> genericAdditionalRecipes;
        private final List<IEquivalencyRecipe> worldAdditionalRecipes;
        private final boolean                  forceReload;

        private AequivaleoWorldAnalysisRunner(
          final ServerWorld serverWorld,
          final List<CompoundInstanceData> valueGeneralData,
          final List<CompoundInstanceData> valueWorldData,
          final List<CompoundInstanceData> lockedGeneralData,
          final List<CompoundInstanceData> lockedWorldData,
          final List<IEquivalencyRecipe> genericAdditionalRecipes,
          final List<IEquivalencyRecipe> worldAdditionalRecipes,
          final boolean forceReload)
        {
            this.valueGeneralData = valueGeneralData;
            this.valueWorldData = valueWorldData;
            this.lockedGeneralData = lockedGeneralData;
            this.serverWorld = serverWorld;
            this.lockedWorldData = lockedWorldData;
            this.genericAdditionalRecipes = genericAdditionalRecipes;
            this.worldAdditionalRecipes = worldAdditionalRecipes;
            this.forceReload = forceReload;
        }

        @Override
        public void run()
        {
            reloadEquivalencyData();
        }

        private void reloadEquivalencyData()
        {
            LOGGER.info("Starting aequivaleo data reload for world: " + getServerWorld().getDimensionKey().getLocation());
            try {
                WorldBootstrapper.onWorldReload(getServerWorld());

                final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> valueGeneralGroupedData = groupDataByContainer(valueGeneralData);
                final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> valueWorldGroupedData = groupDataByContainer(valueWorldData);

                final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> lockedGeneralGroupedData = groupDataByContainer(lockedGeneralData);
                final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> lockedWorldGroupedData = groupDataByContainer(lockedWorldData);

                final Map<ICompoundContainer<?>, Set<CompoundInstance>> valueTargetMap = Maps.newHashMap();
                final Map<ICompoundContainer<?>, Set<CompoundInstance>> lockedTargetMap = Maps.newHashMap();

                final Set<Set<ICompoundContainer<?>>> valueKeySets = ImmutableSet.<Set<ICompoundContainer<?>>>builder()
                                                                   .addAll(valueGeneralGroupedData.keySet())
                                                                   .addAll(valueWorldGroupedData.keySet())
                                                                   .build();

                valueKeySets.forEach(container -> {
                    valueGeneralGroupedData.getOrDefault(container, Sets.newHashSet()).stream().sorted(Comparator.comparingInt(compoundInstanceData -> compoundInstanceData.getMode()
                                                                                                                                                          .ordinal())).forEachOrdered(
                      compoundInstanceData -> compoundInstanceData.handle(valueTargetMap)
                    );

                    valueWorldGroupedData.getOrDefault(container, Sets.newHashSet()).stream().sorted(Comparator.comparingInt(compoundInstanceData -> compoundInstanceData.getMode()
                                                                                                                                                        .ordinal())).forEachOrdered(
                      compoundInstanceData -> compoundInstanceData.handle(valueTargetMap)
                    );
                });

                final Set<Set<ICompoundContainer<?>>> lockedKeySets = ImmutableSet.<Set<ICompoundContainer<?>>>builder()
                  .addAll(lockedGeneralGroupedData.keySet())
                  .addAll(lockedWorldGroupedData.keySet())
                  .build();

                lockedKeySets.forEach(container -> {
                    lockedGeneralGroupedData.getOrDefault(container, Sets.newHashSet()).stream().sorted(Comparator.comparingInt(compoundInstanceData -> compoundInstanceData.getMode()
                                                                                                                                                    .ordinal())).forEachOrdered(
                        compoundInstanceData -> compoundInstanceData.handle(lockedTargetMap)
                    );

                    lockedWorldGroupedData.getOrDefault(container, Sets.newHashSet()).stream().sorted(Comparator.comparingInt(compoundInstanceData -> compoundInstanceData.getMode()
                                                                                                                                                    .ordinal())).forEachOrdered(
                      compoundInstanceData -> compoundInstanceData.handle(lockedTargetMap)
                    );
                });

                valueTargetMap.forEach(ICompoundInformationRegistry.getInstance(getServerWorld().getDimensionKey())
                                          ::registerValue);

                lockedTargetMap.forEach(ICompoundInformationRegistry.getInstance(getServerWorld().getDimensionKey())
                  ::registerLocking);


                genericAdditionalRecipes.forEach(IEquivalencyRecipeRegistry.getInstance(getServerWorld().getDimensionKey())::register);
                worldAdditionalRecipes.forEach(IEquivalencyRecipeRegistry.getInstance(getServerWorld().getDimensionKey())::register);

                JGraphTBasedCompoundAnalyzer analyzer = new JGraphTBasedCompoundAnalyzer(getServerWorld(), forceReload, true);
                EquivalencyResults.getInstance(getServerWorld().getDimensionKey()).set(analyzer.calculateAndGet());
            } catch (Throwable t) {
                LOGGER.fatal(String.format("Failed to analyze: %s", getServerWorld().getDimensionKey().getLocation()), t);
            }
            LOGGER.info("Finished aequivaleo data reload for world: " + getServerWorld().getDimensionKey().getLocation());
        }

        private static Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> groupDataByContainer(final List<CompoundInstanceData> data) {
            return GroupingUtils.groupByUsingSet(
              data,
              CompoundInstanceData::getContainers
            )
              .stream()
              .collect(Collectors.toMap(
                c -> c.iterator().next().getContainers(),
                Function.identity()
              ));
        }

        public ServerWorld getServerWorld()
        {
            return serverWorld;
        }
    }

    public static class DataDrivenData {
        final Map<ResourceLocation, List<CompoundInstanceData>> valueData = new HashMap<>();
        final Map<ResourceLocation, List<CompoundInstanceData>> lockedData = new HashMap<>();
        final Map<ResourceLocation, List<IEquivalencyRecipe>> dataDrivenRecipes = new HashMap<>();
    }
}
