package com.ldtteam.aequivaleo.analysis;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.analysis.AnalysisState;
import com.ldtteam.aequivaleo.api.analysis.IBlacklistDimensionManager;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.information.ICompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.compound.information.datagen.data.CompoundInstanceData;
import com.ldtteam.aequivaleo.api.recipe.equivalency.GenericRecipeEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.data.GenericRecipeData;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistry;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistryEntry;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistryEntryType;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.bootstrap.WorldBootstrapper;
import com.ldtteam.aequivaleo.compound.data.serializers.CompoundInstanceDataSerializer;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.ldtteam.aequivaleo.recipe.equivalency.RecipeCalculator;
import com.ldtteam.aequivaleo.recipe.equivalency.data.GenericRecipeDataSerializer;
import com.ldtteam.aequivaleo.results.EquivalencyResults;
import com.ldtteam.aequivaleo.utils.WorldUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.util.Triple;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class AequivaleoReloadListener implements PreparableReloadListener
{
    private static final String JSON_EXTENSION        = ".json";
    private static final int    JSON_EXTENSION_LENGTH = JSON_EXTENSION.length();

    private static final Logger           LOGGER            = LogManager.getLogger(AequivaleoReloadListener.class);
    private static final ResourceLocation GENERAL_DATA_NAME = new ResourceLocation(Constants.MOD_ID, "general");

    private static final Gson GSON = IAequivaleoAPI.getInstance().getGson();

    private static final List<Supplier<ISyncedRegistry<?>>> SYNCED_REGISTRIES = new ArrayList<>();
    static {
        SYNCED_REGISTRIES.add(() -> ModRegistries.COMPOUND_TYPE);
    }

    @SubscribeEvent
    public static void onAddReloadListener(final AddReloadListenerEvent reloadListenerEvent)
    {
        LOGGER.info("Registering reload listener for graph rebuilding.");
        reloadListenerEvent.addListener(new AequivaleoReloadListener());
    }

    @SubscribeEvent
    public static void onServerStarted(final ServerStartedEvent serverStartedEvent)
    {
        LOGGER.info("Building initial equivalency graph.");
        reloadResources(parseData(serverStartedEvent.getServer().getResourceManager()), false, serverStartedEvent.getClass().getClassLoader());
    }

    public AequivaleoReloadListener()
    {
    }

    private static void reloadResources(final DataDrivenData data, final boolean forceReload, final ClassLoader classLoader)
    {
        final Map<ResourceLocation, List<CompoundInstanceData>> valueData = data.valueData;
        final Map<ResourceLocation, List<CompoundInstanceData>> lockedData = data.lockedData;
        final Map<ResourceLocation, List<IEquivalencyRecipe>> additionalRecipes = data.dataDrivenRecipes;

        if ((lockedData.isEmpty() && valueData.isEmpty()) || ServerLifecycleHooks.getCurrentServer() == null)
        {
            return;
        }

        final List<ServerLevel> worlds = Lists.newArrayList(ServerLifecycleHooks.getCurrentServer().getAllLevels());

        LOGGER.info("Analyzing information");
        try
        {
            final AtomicInteger genericThreadCounter = new AtomicInteger();
            final int maxThreadCount = Math.max(1, Math.max(4, Runtime.getRuntime().availableProcessors() - 2));
            final ExecutorService aequivaleoReloadExecutor = Executors.newFixedThreadPool(maxThreadCount, runnable -> {
                final Thread thread = new Thread(runnable);
                thread.setContextClassLoader(classLoader);
                thread.setName(String.format("Aequivaleo analysis runner: %s", genericThreadCounter.incrementAndGet()));
                return thread;
            });

            RecipeCalculator.IngredientHandler.getInstance().reset();

            CompletableFuture.allOf(buildAnalysisFutures(forceReload, valueData, lockedData, additionalRecipes, worlds, aequivaleoReloadExecutor))
              .thenRunAsync(() -> worlds.forEach(world -> AnalysisStateManager.setStateIfNotError(world.dimension(), AnalysisState.SYNCING)), aequivaleoReloadExecutor)
              .thenRunAsync(AequivaleoReloadListener::synchronizeSyncedRegistries, aequivaleoReloadExecutor)
              .thenRunAsync(EquivalencyResults::updateAllPlayers, aequivaleoReloadExecutor)
              .thenRunAsync(() -> worlds.forEach(world -> AnalysisStateManager.setStateIfNotError(world.dimension(), AnalysisState.POST_PROCESSING)), aequivaleoReloadExecutor)
              .thenRunAsync(() -> worlds.forEach(world -> PluginManger.getInstance().run(plugin -> plugin.onReloadFinishedFor(world))), aequivaleoReloadExecutor)
              .thenRunAsync(() -> RecipeCalculator.IngredientHandler.getInstance().logErrors(), aequivaleoReloadExecutor)
              .thenRunAsync(() -> worlds.forEach(world -> AnalysisStateManager.setStateIfNotError(world.dimension(), AnalysisState.COMPLETED)), aequivaleoReloadExecutor)
              .thenRunAsync(aequivaleoReloadExecutor::shutdown, aequivaleoReloadExecutor);
        }
        catch (Exception ex)
        {
            LOGGER.error("General failure during setup of the async analysis engine", ex);
            worlds.forEach(world -> AnalysisStateManager.setState(world.dimension(), AnalysisState.ERRORED));
        }
    }

    private static DataDrivenData parseData(final ResourceManager resourceManager)
    {
        if (ServerLifecycleHooks.getCurrentServer() == null)
        {
            return new DataDrivenData();
        }

        final Map<ResourceLocation, List<CompoundInstanceData>> valueData = new HashMap<>();
        final Map<ResourceLocation, List<CompoundInstanceData>> lockedData = new HashMap<>();
        final Map<ResourceLocation, List<IEquivalencyRecipe>> additionalRecipes = new HashMap<>();
        final List<ServerLevel> worlds = Lists.newArrayList(ServerLifecycleHooks.getCurrentServer().getAllLevels());

        try
        {
            worlds.forEach(world -> AnalysisStateManager.setState(world.dimension(), AnalysisState.LOADING_DATA));

            valueData.put(
              GENERAL_DATA_NAME,
              readInstanceData(
                resourceManager,
                "aequivaleo/value/general"
              )
            );

            worlds.forEach(world -> {
                try
                {
                    final String path = "aequivaleo/value/" + world.dimension().location().getNamespace() + "/" + world.dimension().location().getPath();

                    valueData.put(
                      world.dimension().location(),
                      readInstanceData(
                        resourceManager,
                        path
                      )
                    );
                }
                catch (Exception ex)
                {
                    LOGGER.error("Failed to load value data for: " + world.dimension(), ex);
                    AnalysisStateManager.setState(world.dimension(), AnalysisState.ERRORED);
                }
            });

            lockedData.put(
              GENERAL_DATA_NAME,
              readInstanceData(
                resourceManager,
                "aequivaleo/locked/general"
              )
            );

            worlds.forEach(world -> {
                try
                {
                    final String path = "aequivaleo/locked/" + world.dimension().location().getNamespace() + "/" + world.dimension().location().getPath();

                    lockedData.put(
                      world.dimension().location(),
                      readInstanceData(
                        resourceManager,
                        path
                      )
                    );
                }
                catch (Exception ex)
                {
                    LOGGER.error("Failed to load locking data for: " + world.dimension(), ex);
                    AnalysisStateManager.setState(world.dimension(), AnalysisState.ERRORED);
                }
            });

            additionalRecipes.put(
              GENERAL_DATA_NAME,
              readAdditionalRecipeData(
                resourceManager,
                "aequivaleo/recipes/general"
              )
            );

            worlds.forEach(world -> {
                try
                {
                    final String path = "aequivaleo/recipes/" + world.dimension().location().getNamespace() + "/" + world.dimension().location().getPath();

                    additionalRecipes.put(
                      world.dimension().location(),
                      readAdditionalRecipeData(
                        resourceManager,
                        path
                      )
                    );
                }
                catch (Exception ex)
                {
                    LOGGER.error("Failed to load additional recipe data for: " + world.dimension(), ex);
                    AnalysisStateManager.setState(world.dimension(), AnalysisState.ERRORED);
                }
            });

            final DataDrivenData dataDrivenData = new DataDrivenData();
            dataDrivenData.valueData.putAll(valueData);
            dataDrivenData.lockedData.putAll(lockedData);
            dataDrivenData.dataDrivenRecipes.putAll(additionalRecipes);
            return dataDrivenData;
        }
        catch (Exception ex)
        {
            LOGGER.error("General failure occurred during loading of data.", ex);
            worlds.forEach(world -> AnalysisStateManager.setState(world.dimension(), AnalysisState.ERRORED));
            return new DataDrivenData();
        }
    }

    private static CompletableFuture<?>[] buildAnalysisFutures(
      final boolean forceReload,
      final Map<ResourceLocation, List<CompoundInstanceData>> valueData,
      final Map<ResourceLocation, List<CompoundInstanceData>> lockedData,
      final Map<ResourceLocation, List<IEquivalencyRecipe>> additionalRecipes,
      final List<ServerLevel> worlds, final ExecutorService aequivaleoReloadExecutor)
    {
        return GroupingUtils.groupByUsingSetToMap(worlds, (world) -> IBlacklistDimensionManager.getInstance().isBlacklisted(world.dimension()))
          .entrySet()
          .stream()
          .map(e -> e.getKey() ? buildBlacklistedAnalysisFutures(Lists.newArrayList(e.getValue()), aequivaleoReloadExecutor) : buildRunnableAnalysisFutures(
            forceReload,
            valueData,
            lockedData,
            additionalRecipes,
            Lists.newArrayList(e.getValue()),
            aequivaleoReloadExecutor
          ))
          .flatMap(Collection::stream)
          .toArray(CompletableFuture[]::new);
    }

    @NotNull
    private static List<CompoundInstanceData> readInstanceData(@NotNull final ResourceManager resourceManager, @NotNull final String targetPath)
    {
        final List<CompoundInstanceData> collectedData = Lists.newArrayList();
        final int targetPathLength = targetPath.length() + 1; //Account for the separator.

        final Gson gson = IAequivaleoAPI.getInstance().getGson();

        resourceManager.listResources(targetPath, s -> s.endsWith(JSON_EXTENSION)).forEach(resourceLocation -> {
            String locationPath = resourceLocation.getPath();
            ResourceLocation resourceLocationWithoutExtension =
              new ResourceLocation(resourceLocation.getNamespace(), locationPath.substring(targetPathLength, locationPath.length() - JSON_EXTENSION_LENGTH));

            try (
              Resource iresource = resourceManager.getResource(resourceLocation);
              InputStream inputstream = iresource.getInputStream();
              Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
            )
            {
                CompoundInstanceData data = gson.fromJson(reader, CompoundInstanceDataSerializer.HANDLED_TYPE);
                if (data != null)
                {
                    collectedData.add(data);
                }
                else
                {
                    LOGGER.error("Couldn't load data file {} from {} as it's null or empty", resourceLocationWithoutExtension, resourceLocation);
                    throw new IllegalStateException("Empty data file.");
                }
            }
            catch (IllegalArgumentException | IOException | JsonParseException ex)
            {
                LOGGER.error("Couldn't parse data file {} from {}", resourceLocationWithoutExtension, resourceLocation, ex);
                throw new IllegalStateException("Parsing failure.", ex);
            }
        });

        return collectedData;
    }

    @NotNull
    private static List<IEquivalencyRecipe> readAdditionalRecipeData(@NotNull final ResourceManager resourceManager, @NotNull final String targetPath)
    {
        final List<IEquivalencyRecipe> collectedData = Lists.newArrayList();
        final int targetPathLength = targetPath.length() + 1; //Account for the separator.

        final Gson gson = IAequivaleoAPI.getInstance().getGson();

        resourceManager.listResources(targetPath, s -> s.endsWith(JSON_EXTENSION)).forEach(resourceLocation -> {
            String locationPath = resourceLocation.getPath();
            ResourceLocation resourceLocationWithoutExtension =
              new ResourceLocation(resourceLocation.getNamespace(), locationPath.substring(targetPathLength, locationPath.length() - JSON_EXTENSION_LENGTH));

            final ResourceLocation name = new ResourceLocation(resourceLocationWithoutExtension.getNamespace(), resourceLocationWithoutExtension.getPath().replace(targetPath, ""));

            try (
              Resource iresource = resourceManager.getResource(resourceLocation);
              InputStream inputstream = iresource.getInputStream();
              Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
            )
            {
                GenericRecipeData data = gson.fromJson(reader, GenericRecipeDataSerializer.HANDLED_TYPE);
                if (data != null)
                {
                    if (data.getConditions().size() != 1 || data.getConditions().iterator().next().test())
                    {
                        collectedData.add(new GenericRecipeEquivalencyRecipe(name, data.getInputs(), data.getRequiredKnownOutputs(), data.getOutputs()));
                    }
                    else
                    {
                        LOGGER.info("Skipping the load of file {} from {} its conditions indicate it is disabled.", resourceLocationWithoutExtension, resourceLocation);
                    }
                }
                else
                {
                    LOGGER.error("Couldn't load data file {} from {} as it's null or empty", resourceLocationWithoutExtension, resourceLocation);
                    throw new IllegalStateException("Empty recipe file.");
                }
            }
            catch (IllegalArgumentException | IOException | JsonParseException ex)
            {
                LOGGER.error("Couldn't parse data file {} from {}", resourceLocationWithoutExtension, resourceLocation, ex);
                throw new IllegalStateException("Parsing failure.", ex);
            }
        });

        return collectedData;
    }

    @NotNull
    private static List<CompletableFuture<?>> buildBlacklistedAnalysisFutures(
      final List<ServerLevel> worlds,
      final ExecutorService aequivaleoReloadExecutor)
    {
        return worlds.stream()
          .map(world -> CompletableFuture.runAsync(() -> LOGGER.debug(String.format("Skipping analysis of: %s", world.dimension().location())), aequivaleoReloadExecutor))
          .collect(Collectors.toList());
    }

    @NotNull
    private static List<CompletableFuture<?>> buildRunnableAnalysisFutures(
      final boolean forceReload,
      final Map<ResourceLocation, List<CompoundInstanceData>> valueData,
      final Map<ResourceLocation, List<CompoundInstanceData>> lockedData,
      final Map<ResourceLocation, List<IEquivalencyRecipe>> additionalRecipes,
      final List<ServerLevel> worlds, final ExecutorService aequivaleoReloadExecutor)
    {
        final List<ServerLevel> runnableWorlds = worlds.stream().filter(world -> !AnalysisStateManager.getState(world.dimension()).isErrored()).collect(Collectors.toList());

        if (containsOnlyGenericData(valueData) &&
              containsOnlyGenericData(lockedData) &&
              containsOnlyGenericData(additionalRecipes))
        {
            return Lists.newArrayList(CompletableFuture.runAsync(
              new AequivaleoWorldAnalysisRunner(
                runnableWorlds,
                valueData.get(GENERAL_DATA_NAME),
                Collections.emptyList(),
                lockedData.get(GENERAL_DATA_NAME),
                Collections.emptyList(),
                additionalRecipes.get(GENERAL_DATA_NAME),
                Collections.emptyList(),
                forceReload
              ),
              aequivaleoReloadExecutor
            ));
        }

        final Collection<Collection<ServerLevel>> groups = GroupingUtils.groupByUsingSet(
          runnableWorlds,
          world -> Triple.of(
            valueData.getOrDefault(world.dimension().location(), Collections.emptyList()),
            lockedData.getOrDefault(world.dimension().location(), Collections.emptyList()),
            additionalRecipes.getOrDefault(world.dimension().location(), Collections.emptyList())
          )
        );

        return groups.stream()
          .map(Lists::newArrayList)
          .filter(groupWorlds -> groupWorlds.size() > 0)
          .map(groupWorlds -> CompletableFuture.runAsync(
            new AequivaleoWorldAnalysisRunner(
              runnableWorlds,
              valueData.get(GENERAL_DATA_NAME),
              valueData.get(groupWorlds.get(0).dimension().location()),
              lockedData.get(GENERAL_DATA_NAME),
              lockedData.get(groupWorlds.get(0).dimension().location()),
              additionalRecipes.get(GENERAL_DATA_NAME),
              additionalRecipes.get(groupWorlds.get(0).dimension().location()),
              forceReload
            ),
            aequivaleoReloadExecutor
          ))
          .collect(Collectors.toList());
    }

    private static <L extends List<?>> boolean containsOnlyGenericData(final Map<ResourceLocation, L> dataMap)
    {
        if (dataMap.size() == 1 && dataMap.containsKey(GENERAL_DATA_NAME))
        {
            return true;
        }

        for (final Map.Entry<ResourceLocation, L> worldDataEntry : dataMap.entrySet())
        {
            if (worldDataEntry.getKey() != GENERAL_DATA_NAME)
            {
                if (worldDataEntry.getValue() != null && worldDataEntry.getValue().size() != 0)
                {
                    return false;
                }
            }
        }

        return true;
    }

    public final @NotNull CompletableFuture<Void> reload(
      @NotNull PreparableReloadListener.PreparationBarrier barrier,
      @NotNull ResourceManager resourceManager,
      @NotNull ProfilerFiller backgroundProfiler,
      @NotNull ProfilerFiller foregroundProfiler,
      @NotNull Executor backgroundExecutor,
      @NotNull Executor foregroundExecutor
    )
    {
        return CompletableFuture
          .runAsync(this::resetSyncedRegistries, backgroundExecutor)
          .thenComposeAsync(unused -> loadSyncRegistries(resourceManager, backgroundExecutor, backgroundProfiler), backgroundExecutor)
          .thenApplyAsync((syncRegistryResult) -> this.prepare(resourceManager, backgroundProfiler), backgroundExecutor)
          .thenCompose(barrier::wait)
          .thenAcceptAsync((data) -> this.apply(data, resourceManager, foregroundProfiler), foregroundExecutor);
    }

    private void resetSyncedRegistries()
    {
        SYNCED_REGISTRIES.stream()
          .map(Supplier::get)
          .forEach(ISyncedRegistry::clear);
    }

    private CompletableFuture<Unit> loadSyncRegistries(
      @NotNull final ResourceManager resourceManager,
      @NotNull final Executor backgroundExecutor,
      @NotNull final ProfilerFiller profiler)
    {
        return CompletableFuture.allOf(
          SYNCED_REGISTRIES.stream()
            .map(Supplier::get)
            .map(registry -> loadSyncedRegistry(resourceManager, backgroundExecutor, profiler, registry))
            .toArray(CompletableFuture[]::new)
        )
         .thenApply(unused -> Unit.INSTANCE);
    }

    private static void synchronizeSyncedRegistries()
    {
        SYNCED_REGISTRIES.stream()
          .map(Supplier::get)
          .forEach(ISyncedRegistry::synchronizeAll);
    }

    @NotNull
    private DataDrivenData prepare(@NotNull final ResourceManager resourceManagerIn, @NotNull final ProfilerFiller profilerIn)
    {
        return parseData(resourceManagerIn);
    }

    protected void apply(@NotNull final DataDrivenData objectIn, @NotNull final ResourceManager resourceManagerIn, @NotNull final ProfilerFiller profilerIn)
    {
        LOGGER.info("Reloading resources has been triggered, recalculating graph.");
        reloadResources(objectIn, true, resourceManagerIn.getClass().getClassLoader());
    }

    private <T extends ISyncedRegistryEntry<T>> CompletableFuture<Unit> loadSyncedRegistry(
      @NotNull final ResourceManager resourceManager, @NotNull final Executor executor, @NotNull final ProfilerFiller profiler,
      @NotNull final ISyncedRegistry<T> registry)
    {
        return CompletableFuture.allOf(
            registry.getTypes().stream().map(
                type -> loadSyncedRegistryEntriesOfType(resourceManager, executor, profiler, registry, type)
              )
              .toArray(CompletableFuture[]::new)
          )
          .thenApplyAsync(unused -> Unit.INSTANCE);
    }

    private <T extends ISyncedRegistryEntry<T>> CompletableFuture<Unit> loadSyncedRegistryEntriesOfType(
      @NotNull final ResourceManager resourceManager,
      @NotNull final Executor backgroundExecutor,
      @NotNull final ProfilerFiller profiler,
      @NotNull final ISyncedRegistry<T> registry,
      @NotNull final ISyncedRegistryEntryType<T> type
    ) {
        return CompletableFuture.supplyAsync(() -> {
            final String targetPath = type.getDirectoryName();
            final int targetPathLength = targetPath.length();

            resourceManager.listResources(type.getDirectoryName(), s -> s.endsWith(JSON_EXTENSION))
              .forEach(entryLocation -> {
                  String locationPath = entryLocation.getPath();
                  ResourceLocation resourceLocationWithoutExtension =
                    new ResourceLocation(entryLocation.getNamespace(), locationPath.substring(targetPathLength, locationPath.length() - JSON_EXTENSION_LENGTH));

                  final ResourceLocation name = new ResourceLocation(resourceLocationWithoutExtension.getNamespace(), resourceLocationWithoutExtension.getPath().replace(targetPath, ""));

                  try (
                    Resource resource = resourceManager.getResource(entryLocation);
                    InputStream inputstream = resource.getInputStream();
                    Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
                  )
                  {
                      if (type.getEntryCodec() == null) {
                          throw new IllegalStateException("Tried to load a registry entry for a type without a codec");
                      }

                      final DataResult<Pair<T, JsonElement>> entry = type.getEntryCodec().decode(JsonOps.INSTANCE, GSON.fromJson(reader, JsonElement.class));
                      if (entry.error().isPresent()) {
                          LOGGER.error("Failed to load {}: {}", name, entry.error().get());
                      }
                      else if (entry.result().isPresent()) {
                          final Pair<T, JsonElement> resultData = entry.result().get();
                          final T result = resultData.getFirst();

                          registry.add(result);
                      }
                  }
                  catch (IllegalArgumentException | IOException | JsonParseException ex)
                  {
                      LOGGER.error("Couldn't parse data file {} from {}", resourceLocationWithoutExtension, entryLocation, ex);
                      throw new IllegalStateException("Parsing failure.", ex);
                  }
              });

            return Unit.INSTANCE;
        });
    }

    private static class AequivaleoWorldAnalysisRunner implements Runnable
    {
        private final List<ServerLevel>          worlds;
        private final List<CompoundInstanceData> valueGeneralData;
        private final List<CompoundInstanceData> valueWorldData;
        private final List<CompoundInstanceData> lockedGeneralData;
        private final List<CompoundInstanceData> lockedWorldData;
        private final List<IEquivalencyRecipe>   genericAdditionalRecipes;
        private final List<IEquivalencyRecipe>   worldAdditionalRecipes;
        private final boolean                    forceReload;

        private AequivaleoWorldAnalysisRunner(
          final List<ServerLevel> worlds,
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
            this.worlds = worlds;
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
            LOGGER.info(String.format("Starting aequivaleo data reload for world: %s", WorldUtils.formatWorldNames(getWorlds())));
            try
            {
                if (getWorlds().stream().anyMatch(world -> AnalysisStateManager.getState(world.dimension()).isErrored()))
                {
                    throw new IllegalStateException("Tried to run an analysis for an error dimension!");
                }

                getWorlds().forEach(WorldBootstrapper::onWorldReload);

                final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> valueGeneralGroupedData = groupDataByContainer(valueGeneralData);
                final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> valueWorldGroupedData = groupDataByContainer(valueWorldData);

                final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> lockedGeneralGroupedData = groupDataByContainer(lockedGeneralData);
                final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> lockedWorldGroupedData = groupDataByContainer(lockedWorldData);

                final Map<ICompoundContainer<?>, Set<CompoundInstance>> valueTargetMap = Maps.newHashMap();
                final Map<ICompoundContainer<?>, Set<CompoundInstance>> lockedTargetMap = Maps.newHashMap();

                processCompoundInstanceData(valueGeneralGroupedData, valueWorldGroupedData, valueTargetMap);
                processCompoundInstanceData(lockedGeneralGroupedData, lockedWorldGroupedData, lockedTargetMap);

                getWorlds().forEach(world -> {
                    valueTargetMap.forEach(ICompoundInformationRegistry.getInstance(world.dimension())
                                             ::registerValue);

                    lockedTargetMap.forEach(ICompoundInformationRegistry.getInstance(world.dimension())
                                              ::registerLocking);

                    genericAdditionalRecipes.forEach(IEquivalencyRecipeRegistry.getInstance(world.dimension())::register);
                    worldAdditionalRecipes.forEach(IEquivalencyRecipeRegistry.getInstance(world.dimension())::register);
                });


                AnalysisStateManager.setStateIfNotError(getWorlds(), AnalysisState.PROCESSING);

                JGraphTBasedCompoundAnalyzer analyzer = new JGraphTBasedCompoundAnalyzer(getWorlds(), forceReload, true);

                final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

                getWorlds().forEach(world -> EquivalencyResults.getInstance(world.dimension()).set(result));
            }
            catch (Throwable t)
            {
                LOGGER.fatal(String.format("Failed to analyze: %s", WorldUtils.formatWorldNames(getWorlds())), t);
                AnalysisStateManager.setState(getWorlds(), AnalysisState.ERRORED);
            }
            LOGGER.info(String.format("Finished aequivaleo data reload for world: %s", WorldUtils.formatWorldNames(getWorlds())));
        }

        public List<ServerLevel> getWorlds()
        {
            return worlds;
        }

        private static Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> groupDataByContainer(final List<CompoundInstanceData> data)
        {
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

        private void processCompoundInstanceData(
          final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> generalData,
          final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> worldData,
          final Map<ICompoundContainer<?>, Set<CompoundInstance>> target)
        {
            final Set<Set<ICompoundContainer<?>>> keys = ImmutableSet.<Set<ICompoundContainer<?>>>builder()
              .addAll(generalData.keySet())
              .addAll(worldData.keySet())
              .build();

            keys.forEach(container -> {
                generalData.getOrDefault(container, Sets.newHashSet()).stream().sorted(Comparator.comparingInt(compoundInstanceData -> compoundInstanceData.getMode()
                  .ordinal())).forEachOrdered(
                  compoundInstanceData -> compoundInstanceData.handle(target)
                );

                worldData.getOrDefault(container, Sets.newHashSet()).stream().sorted(Comparator.comparingInt(compoundInstanceData -> compoundInstanceData.getMode()
                  .ordinal())).forEachOrdered(
                  compoundInstanceData -> compoundInstanceData.handle(target)
                );
            });
        }
    }

    public static class DataDrivenData
    {
        final Map<ResourceLocation, List<CompoundInstanceData>> valueData         = new HashMap<>();
        final Map<ResourceLocation, List<CompoundInstanceData>> lockedData        = new HashMap<>();
        final Map<ResourceLocation, List<IEquivalencyRecipe>>   dataDrivenRecipes = new HashMap<>();
    }
}
