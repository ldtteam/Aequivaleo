package com.ldtteam.aequivaleo.analysis;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
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
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.ldtteam.aequivaleo.recipe.equivalency.RecipeCalculator;
import com.ldtteam.aequivaleo.results.EquivalencyResults;
import com.ldtteam.aequivaleo.utils.WorldUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class AequivaleoReloadListener implements PreparableReloadListener {
    private static final String JSON_EXTENSION = ".json";
    private static final int JSON_EXTENSION_LENGTH = JSON_EXTENSION.length();
    
    private static final Logger LOGGER = LogManager.getLogger(AequivaleoReloadListener.class);
    private static final ResourceLocation GENERAL_DATA_NAME = new ResourceLocation(Constants.MOD_ID, "general");
    private static final List<ISyncedRegistry<?, ?>> SYNCED_REGISTRIES = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final LevelResource AEQUIVALEO_DATA = new LevelResource("aequivaleo");
    
    static {
        SYNCED_REGISTRIES.add(ModRegistries.COMPOUND_TYPE);
    }
    
    @SuppressWarnings("unused")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAddReloadListener(final AddReloadListenerEvent reloadListenerEvent) {
        LOGGER.info("Registering reload listener for graph rebuilding.");
        reloadListenerEvent.addListener(new AequivaleoReloadListener(reloadListenerEvent.getServerResources()));
    }
    
    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onServerStarted(final ServerStartedEvent serverStartedEvent) {
        LOGGER.info("Building initial equivalency graph.");
        
        final AequivaleoReloadListener listener = new AequivaleoReloadListener(serverStartedEvent.getServer().getServerResources().managers());
        listener.reload(
                new PreparationBarrier() {
                    @Override
                    public <T> @NotNull CompletableFuture<T> wait(final @NotNull T preparedValue) {
                        //We complete immediately in a prepared state.
                        return CompletableFuture.completedFuture(preparedValue);
                    }
                },
                serverStartedEvent.getServer().getResourceManager(),
                Util.backgroundExecutor(),
                Runnable::run,
                false
        );
    }
    
    private final ConditionalOps<JsonElement> ops;
    
    public AequivaleoReloadListener(final ReloadableServerResources serverResources) {
        this.ops = ConditionalOps.create(
                RegistryOps.create(JsonOps.INSTANCE, serverResources.getRegistryAccess()),
                serverResources.getConditionContext()
        );
    }
    
    private static void reloadResources(final DataDrivenData data, final boolean forceReload, final ClassLoader classLoader) {
        final Map<ResourceLocation, List<CompoundInstanceData>> valueData = data.valueData;
        final Map<ResourceLocation, List<CompoundInstanceData>> lockedData = data.lockedData;
        final Map<ResourceLocation, List<CompoundInstanceData>> baseData = data.baseData;
        final Map<ResourceLocation, List<IEquivalencyRecipe>> additionalRecipes = data.dataDrivenRecipes;
        
        if ((lockedData.isEmpty() && valueData.isEmpty() && baseData.isEmpty()) || ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }
        
        final List<ServerLevel> worlds = Lists.newArrayList(ServerLifecycleHooks.getCurrentServer().getAllLevels());
        
        LOGGER.info("Analyzing information");
        try {
            final AtomicInteger genericThreadCounter = new AtomicInteger();
            final int maxThreadCount = Math.max(1, Math.max(4, Runtime.getRuntime().availableProcessors() - 2));
            final ExecutorService aequivaleoReloadExecutor = Executors.newFixedThreadPool(maxThreadCount, runnable -> {
                final Thread thread = new Thread(runnable);
                thread.setContextClassLoader(classLoader);
                thread.setName(String.format("Aequivaleo analysis runner: %s", genericThreadCounter.incrementAndGet()));
                return thread;
            });
            
            RecipeCalculator.IngredientHandler.getInstance().reset();
            
            CompletableFuture.allOf(buildAnalysisFutures(forceReload, valueData, lockedData, baseData, additionalRecipes, worlds, aequivaleoReloadExecutor))
                    .thenRunAsync(() -> worlds.forEach(world -> AnalysisStateManager.setStateIfNotError(world.dimension(), AnalysisState.SYNCING)), aequivaleoReloadExecutor)
                    .thenRunAsync(AequivaleoReloadListener::synchronizeSyncedRegistries, aequivaleoReloadExecutor)
                    .thenRunAsync(EquivalencyResults::updateAllPlayers, aequivaleoReloadExecutor)
                    .thenRunAsync(() -> worlds.forEach(world -> AnalysisStateManager.setStateIfNotError(world.dimension(), AnalysisState.POST_PROCESSING)), aequivaleoReloadExecutor)
                    .thenRunAsync(() -> worlds.forEach(world -> PluginManger.getInstance().run(plugin -> plugin.onReloadFinishedFor(world))), aequivaleoReloadExecutor)
                    .thenRunAsync(() -> RecipeCalculator.IngredientHandler.getInstance().logErrors(), aequivaleoReloadExecutor)
                    .thenRunAsync(() -> worlds.forEach(world -> AnalysisStateManager.setStateIfNotError(world.dimension(), AnalysisState.COMPLETED)), aequivaleoReloadExecutor)
                    .thenRunAsync(aequivaleoReloadExecutor::shutdown, aequivaleoReloadExecutor);
        } catch (Exception ex) {
            LOGGER.error("General failure during setup of the async analysis engine", ex);
            worlds.forEach(world -> AnalysisStateManager.setState(world.dimension(), AnalysisState.ERRORED));
        }
    }
    
    private DataDrivenData parseData(final ResourceManager resourceManager) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return new DataDrivenData();
        }
        
        final Map<ResourceLocation, List<CompoundInstanceData>> valueData = new HashMap<>();
        final Map<ResourceLocation, List<CompoundInstanceData>> lockedData = new HashMap<>();
        final Map<ResourceLocation, List<CompoundInstanceData>> baseData = new HashMap<>();
        final Map<ResourceLocation, List<IEquivalencyRecipe>> additionalRecipes = new HashMap<>();
        final List<ServerLevel> worlds = Lists.newArrayList(ServerLifecycleHooks.getCurrentServer().getAllLevels());
        
        try {
            worlds.forEach(world -> AnalysisStateManager.setState(world.dimension(), AnalysisState.LOADING_DATA));
            
            valueData.put(
                    GENERAL_DATA_NAME,
                    readInstanceData(
                            resourceManager,
                            "aequivaleo/value/general"
                    )
            );
            
            worlds.forEach(world -> {
                try {
                    final String path = "aequivaleo/value/" + world.dimension().location().getNamespace() + "/" + world.dimension().location().getPath();
                    
                    valueData.put(
                            world.dimension().location(),
                            readInstanceData(
                                    resourceManager,
                                    path
                            )
                    );
                } catch (Exception ex) {
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
                try {
                    final String path = "aequivaleo/locked/" + world.dimension().location().getNamespace() + "/" + world.dimension().location().getPath();
                    
                    lockedData.put(
                            world.dimension().location(),
                            readInstanceData(
                                    resourceManager,
                                    path
                            )
                    );
                } catch (Exception ex) {
                    LOGGER.error("Failed to load locking data for: " + world.dimension(), ex);
                    AnalysisStateManager.setState(world.dimension(), AnalysisState.ERRORED);
                }
            });
            
            baseData.put(
                    GENERAL_DATA_NAME,
                    readInstanceData(
                            resourceManager,
                            "aequivaleo/base/general"
                    )
            );
            
            worlds.forEach(world -> {
                try {
                    final String path = "aequivaleo/base/" + world.dimension().location().getNamespace() + "/" + world.dimension().location().getPath();
                    
                    baseData.put(
                            world.dimension().location(),
                            readInstanceData(
                                    resourceManager,
                                    path
                            )
                    );
                } catch (Exception ex) {
                    LOGGER.error("Failed to load base data for: " + world.dimension(), ex);
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
                try {
                    final String path = "aequivaleo/recipes/" + world.dimension().location().getNamespace() + "/" + world.dimension().location().getPath();
                    
                    additionalRecipes.put(
                            world.dimension().location(),
                            readAdditionalRecipeData(
                                    resourceManager,
                                    path
                            )
                    );
                } catch (Exception ex) {
                    LOGGER.error("Failed to load additional recipe data for: " + world.dimension(), ex);
                    AnalysisStateManager.setState(world.dimension(), AnalysisState.ERRORED);
                }
            });
            
            final DataDrivenData dataDrivenData = new DataDrivenData();
            dataDrivenData.valueData.putAll(valueData);
            dataDrivenData.lockedData.putAll(lockedData);
            dataDrivenData.baseData.putAll(baseData);
            dataDrivenData.dataDrivenRecipes.putAll(additionalRecipes);
            return dataDrivenData;
        } catch (Exception ex) {
            LOGGER.error("General failure occurred during loading of data.", ex);
            worlds.forEach(world -> AnalysisStateManager.setState(world.dimension(), AnalysisState.ERRORED));
            return new DataDrivenData();
        }
    }
    
    private static CompletableFuture<?>[] buildAnalysisFutures(
            final boolean forceReload,
            final Map<ResourceLocation, List<CompoundInstanceData>> valueData,
            final Map<ResourceLocation, List<CompoundInstanceData>> lockedData,
            final Map<ResourceLocation, List<CompoundInstanceData>> baseData,
            final Map<ResourceLocation, List<IEquivalencyRecipe>> additionalRecipes,
            final List<ServerLevel> worlds, final ExecutorService aequivaleoReloadExecutor) {
        return GroupingUtils.groupByUsingSetToMap(worlds, (world) -> IBlacklistDimensionManager.getInstance().isBlacklisted(world.dimension()))
                       .entrySet()
                       .stream()
                       .map(e -> e.getKey() ? buildBlacklistedAnalysisFutures(Lists.newArrayList(e.getValue()), aequivaleoReloadExecutor) : buildRunnableAnalysisFutures(
                               forceReload,
                               valueData,
                               lockedData,
                               baseData,
                               additionalRecipes,
                               Lists.newArrayList(e.getValue()),
                               aequivaleoReloadExecutor
                       ))
                       .flatMap(Collection::stream)
                       .toArray(CompletableFuture[]::new);
    }
    
    private <T, E extends T> Optional<T> parse(ResourceLocation location, Resource resource, Codec<E> codec) {
        try (
                InputStream inputstream = resource.open();
                Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
        ) {
            final Optional<E> result = ICondition.getConditionally(
                    codec,
                    ops,
                    GSON.fromJson(reader, JsonElement.class)
            );
            
            return result.map(Function.identity());
        } catch (IllegalArgumentException | IOException | JsonParseException ex) {
            LOGGER.error("Couldn't parse data file {}", location, ex);
            throw new IllegalStateException("Parsing failure.", ex);
        }
    }
    
    @NotNull
    private List<CompoundInstanceData> readInstanceData(@NotNull final ResourceManager resourceManager, @NotNull final String targetPath) {
        final List<CompoundInstanceData> collectedData = Lists.newArrayList();
        final int targetPathLength = targetPath.length() + 1; //Account for the separator.
        
        resourceManager.listResources(targetPath, s -> s.getPath().endsWith(JSON_EXTENSION)).forEach((ResourceLocation resourceLocation, Resource resource) -> {
            String locationPath = resourceLocation.getPath();
            ResourceLocation name =
                    new ResourceLocation(resourceLocation.getNamespace(), locationPath.substring(targetPathLength, locationPath.length() - JSON_EXTENSION_LENGTH));
            
            final Optional<CompoundInstanceData> data = parse(name, resource, CompoundInstanceData.CODEC);
            data.ifPresent(collectedData::add);
        });
        
        return collectedData;
    }
    
    @NotNull
    private List<IEquivalencyRecipe> readAdditionalRecipeData(@NotNull final ResourceManager resourceManager, @NotNull final String targetPath) {
        final List<IEquivalencyRecipe> collectedData = Lists.newArrayList();
        final int targetPathLength = targetPath.length() + 1; //Account for the separator.
        
        resourceManager.listResources(targetPath, s -> s.getPath().endsWith(JSON_EXTENSION)).forEach((ResourceLocation resourceLocation, Resource resource) -> {
            String locationPath = resourceLocation.getPath();
            ResourceLocation resourceLocationWithoutExtension =
                    new ResourceLocation(resourceLocation.getNamespace(), locationPath.substring(targetPathLength, locationPath.length() - JSON_EXTENSION_LENGTH));
            
            final ResourceLocation name = new ResourceLocation(resourceLocationWithoutExtension.getNamespace(), resourceLocationWithoutExtension.getPath().replace(targetPath, ""));
            final Optional<GenericRecipeData> data = parse(name, resource, GenericRecipeData.CODEC);
            data.ifPresent(d -> collectedData.add(new GenericRecipeEquivalencyRecipe(name, d.getInputs(), d.getRequiredKnownOutputs(), d.getOutputs())));
        });
        
        return collectedData;
    }
    
    @NotNull
    private static List<CompletableFuture<?>> buildBlacklistedAnalysisFutures(
            final List<ServerLevel> worlds,
            final ExecutorService aequivaleoReloadExecutor) {
        return worlds.stream()
                       .map(world -> CompletableFuture.runAsync(() -> LOGGER.debug(String.format("Skipping analysis of: %s", world.dimension().location())), aequivaleoReloadExecutor))
                       .collect(Collectors.toList());
    }
    
    @NotNull
    private static List<CompletableFuture<?>> buildRunnableAnalysisFutures(
            final boolean forceReload,
            final Map<ResourceLocation, List<CompoundInstanceData>> valueData,
            final Map<ResourceLocation, List<CompoundInstanceData>> lockedData,
            final Map<ResourceLocation, List<CompoundInstanceData>> baseData,
            final Map<ResourceLocation, List<IEquivalencyRecipe>> additionalRecipes,
            final List<ServerLevel> worlds, final ExecutorService aequivaleoReloadExecutor) {
        final List<ServerLevel> runnableWorlds = worlds.stream().filter(world -> !AnalysisStateManager.getState(world.dimension()).isErrored()).collect(Collectors.toList());
        final List<LevelAnalysisOwner> analysisOwners = runnableWorlds.stream().map(LevelAnalysisOwner::new).collect(Collectors.toList());
        
        if (containsOnlyGenericData(valueData) &&
                    containsOnlyGenericData(lockedData) &&
                    containsOnlyGenericData(baseData) &&
                    containsOnlyGenericData(additionalRecipes)) {
            return Lists.newArrayList(CompletableFuture.runAsync(
                    new AequivaleoWorldAnalysisRunner(
                            analysisOwners,
                            valueData.get(GENERAL_DATA_NAME),
                            Collections.emptyList(),
                            lockedData.get(GENERAL_DATA_NAME),
                            Collections.emptyList(),
                            baseData.get(GENERAL_DATA_NAME),
                            Collections.emptyList(),
                            additionalRecipes.get(GENERAL_DATA_NAME),
                            Collections.emptyList(),
                            forceReload
                    ),
                    aequivaleoReloadExecutor
            ));
        }
        
        record GroupingData(List<CompoundInstanceData> values, List<CompoundInstanceData> locks,
                            List<CompoundInstanceData> bases, List<IEquivalencyRecipe> recipes) {
        }
        
        final Collection<Collection<ServerLevel>> groups = GroupingUtils.groupByUsingSet(
                runnableWorlds,
                world -> new GroupingData(
                        valueData.getOrDefault(world.dimension().location(), Collections.emptyList()),
                        lockedData.getOrDefault(world.dimension().location(), Collections.emptyList()),
                        baseData.getOrDefault(world.dimension().location(), Collections.emptyList()),
                        additionalRecipes.getOrDefault(world.dimension().location(), Collections.emptyList())
                )
        );
        
        return groups.stream()
                       .map(Lists::newArrayList)
                       .filter(groupWorlds -> !groupWorlds.isEmpty())
                       .map(groupWorlds -> CompletableFuture.runAsync(
                               new AequivaleoWorldAnalysisRunner(
                                       analysisOwners,
                                       valueData.get(GENERAL_DATA_NAME),
                                       valueData.get(groupWorlds.get(0).dimension().location()),
                                       lockedData.get(GENERAL_DATA_NAME),
                                       lockedData.get(groupWorlds.get(0).dimension().location()),
                                       baseData.get(GENERAL_DATA_NAME),
                                       baseData.get(groupWorlds.get(0).dimension().location()),
                                       additionalRecipes.get(GENERAL_DATA_NAME),
                                       additionalRecipes.get(groupWorlds.get(0).dimension().location()),
                                       forceReload
                               ),
                               aequivaleoReloadExecutor
                       ))
                       .collect(Collectors.toList());
    }
    
    private static <L extends List<?>> boolean containsOnlyGenericData(final Map<ResourceLocation, L> dataMap) {
        if (dataMap.size() == 1 && dataMap.containsKey(GENERAL_DATA_NAME)) {
            return true;
        }
        
        for (final Map.Entry<ResourceLocation, L> worldDataEntry : dataMap.entrySet()) {
            if (worldDataEntry.getKey() != GENERAL_DATA_NAME) {
                if (worldDataEntry.getValue() != null && !worldDataEntry.getValue().isEmpty()) {
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
    ) {
        return reload(barrier, resourceManager, backgroundExecutor, foregroundExecutor, true);
    }
    
    public final @NotNull CompletableFuture<Void> reload(
            @NotNull PreparableReloadListener.PreparationBarrier barrier,
            @NotNull ResourceManager resourceManager,
            @NotNull Executor backgroundExecutor,
            @NotNull Executor foregroundExecutor,
            final boolean forceReload
    ) {
        return CompletableFuture
                       .runAsync(this::resetSyncedRegistries, backgroundExecutor)
                       .thenComposeAsync(unused -> loadSyncRegistries(resourceManager), backgroundExecutor)
                       .thenApplyAsync((syncRegistryResult) -> this.prepare(resourceManager), backgroundExecutor)
                       .thenCompose(barrier::wait)
                       .thenAcceptAsync((data) -> this.apply(data, resourceManager, forceReload), foregroundExecutor);
    }
    
    private void resetSyncedRegistries() {
        SYNCED_REGISTRIES
                .forEach(ISyncedRegistry::clear);
    }
    
    private CompletableFuture<Unit> loadSyncRegistries(
            @NotNull final ResourceManager resourceManager) {
        return CompletableFuture.allOf(
                        SYNCED_REGISTRIES.stream()
                                .map(registry -> loadSyncedRegistry(resourceManager, registry))
                                .toArray(CompletableFuture[]::new)
                )
                       .thenApply(unused -> Unit.INSTANCE);
    }
    
    private static void synchronizeSyncedRegistries() {
        SYNCED_REGISTRIES
                .forEach(ISyncedRegistry::synchronizeAll);
    }
    
    @NotNull
    private DataDrivenData prepare(@NotNull final ResourceManager resourceManagerIn) {
        return parseData(resourceManagerIn);
    }
    
    protected void apply(@NotNull final DataDrivenData objectIn, @NotNull final ResourceManager resourceManagerIn, final boolean forcedReload) {
        LOGGER.info("Reloading resources has been triggered, recalculating graph.");
        reloadResources(objectIn, forcedReload, resourceManagerIn.getClass().getClassLoader());
    }
    
    private <T extends ISyncedRegistryEntry<T, G>, G extends ISyncedRegistryEntryType<T, G>> CompletableFuture<Unit> loadSyncedRegistry(
            @NotNull final ResourceManager resourceManager,
            @NotNull final ISyncedRegistry<T, G> registry) {
        return CompletableFuture.allOf(
                        registry.getTypes().stream().map(
                                        type -> loadSyncedRegistryEntriesOfType(resourceManager, registry, type)
                                )
                                .toArray(CompletableFuture[]::new)
                )
                       .thenApplyAsync(unused -> Unit.INSTANCE);
    }
    
    private <T extends ISyncedRegistryEntry<T, G>, G extends ISyncedRegistryEntryType<T, G>> CompletableFuture<Unit> loadSyncedRegistryEntriesOfType(
            @NotNull final ResourceManager resourceManager,
            @NotNull final ISyncedRegistry<T, G> registry,
            @NotNull final ISyncedRegistryEntryType<T, G> type
    ) {
        return CompletableFuture.supplyAsync(() -> {
            final String targetPath = type.getDirectoryName();
            final int targetPathLength = targetPath.length();
            
            resourceManager.listResources(type.getDirectoryName(), s -> s.getPath().endsWith(JSON_EXTENSION))
                    .forEach((ResourceLocation entryLocation, Resource resource) -> {
                        String locationPath = entryLocation.getPath();
                        ResourceLocation resourceLocationWithoutExtension =
                                new ResourceLocation(entryLocation.getNamespace(), locationPath.substring(targetPathLength, locationPath.length() - JSON_EXTENSION_LENGTH));
                        
                        final ResourceLocation name = new ResourceLocation(resourceLocationWithoutExtension.getNamespace(), resourceLocationWithoutExtension.getPath().replace(targetPath, ""));
                        
                        final Optional<T> data = parse(
                                resourceLocationWithoutExtension,
                                resource,
                                type.getEntryCodec()
                        );
                        
                        data.ifPresent(d -> registry.add(name, d));
                    });
            
            return Unit.INSTANCE;
        });
    }
    
    private record AequivaleoWorldAnalysisRunner(List<LevelAnalysisOwner> analysisOwners,
                                                 List<CompoundInstanceData> valueGeneralData,
                                                 List<CompoundInstanceData> valueWorldData,
                                                 List<CompoundInstanceData> lockedGeneralData,
                                                 List<CompoundInstanceData> lockedWorldData,
                                                 List<CompoundInstanceData> baseGeneralData,
                                                 List<CompoundInstanceData> baseWorldData,
                                                 List<IEquivalencyRecipe> genericAdditionalRecipes,
                                                 List<IEquivalencyRecipe> worldAdditionalRecipes,
                                                 boolean forceReload) implements Runnable {
        
        @Override
            public void run() {
                reloadEquivalencyData();
            }
            
            private void reloadEquivalencyData() {
                LOGGER.info(String.format("Starting aequivaleo data reload for world: %s", WorldUtils.formatWorldNames(analysisOwners())));
                try {
                    if (analysisOwners().stream().anyMatch(world -> AnalysisStateManager.getState(world.getIdentifier()).isErrored())) {
                        throw new IllegalStateException("Tried to run an analysis for an error dimension!");
                    }
                    
                    analysisOwners()
                            .stream()
                            .map(LevelAnalysisOwner::serverLevel)
                            .forEach(WorldBootstrapper::onWorldReload);
                    
                    final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> valueGeneralGroupedData = groupDataByContainer(valueGeneralData);
                    final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> valueWorldGroupedData = groupDataByContainer(valueWorldData);
                    
                    final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> lockedGeneralGroupedData = groupDataByContainer(lockedGeneralData);
                    final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> lockedWorldGroupedData = groupDataByContainer(lockedWorldData);
                    
                    final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> baseGeneralGroupedData = groupDataByContainer(baseGeneralData);
                    final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> baseWorldGroupedData = groupDataByContainer(baseWorldData);
                    
                    final Map<ICompoundContainer<?>, Set<CompoundInstance>> valueTargetMap = Maps.newHashMap();
                    final Map<ICompoundContainer<?>, Set<CompoundInstance>> lockedTargetMap = Maps.newHashMap();
                    final Map<ICompoundContainer<?>, Set<CompoundInstance>> baseTargetMap = Maps.newHashMap();
                    
                    processCompoundInstanceData(valueGeneralGroupedData, valueWorldGroupedData, valueTargetMap);
                    processCompoundInstanceData(lockedGeneralGroupedData, lockedWorldGroupedData, lockedTargetMap);
                    processCompoundInstanceData(baseGeneralGroupedData, baseWorldGroupedData, baseTargetMap);
                    
                    analysisOwners().forEach(analysisOwner -> {
                        valueTargetMap.forEach(ICompoundInformationRegistry.getInstance(analysisOwner.getIdentifier())
                                                       ::registerValue);
                        
                        lockedTargetMap.forEach(ICompoundInformationRegistry.getInstance(analysisOwner.getIdentifier())
                                                        ::registerLocking);
                        
                        baseTargetMap.forEach(ICompoundInformationRegistry.getInstance(analysisOwner.getIdentifier())
                                                      ::registerBase);
                        
                        genericAdditionalRecipes.forEach(IEquivalencyRecipeRegistry.getInstance(analysisOwner.getIdentifier())::register);
                        worldAdditionalRecipes.forEach(IEquivalencyRecipeRegistry.getInstance(analysisOwner.getIdentifier())::register);
                    });
                    
                    AnalysisStateManager.setStateIfNotError(analysisOwners().stream().map(LevelAnalysisOwner::serverLevel).collect(Collectors.toList()), AnalysisState.PROCESSING);
                    
                    JGraphTBasedCompoundAnalyzer analyzer = new JGraphTBasedCompoundAnalyzer(analysisOwners(), forceReload, true);
                    
                    final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();
                    
                    analysisOwners().forEach(world -> EquivalencyResults.getInstance(world.getIdentifier()).set(result));
                } catch (Throwable t) {
                    LOGGER.fatal(String.format("Failed to analyze: %s", WorldUtils.formatWorldNames(analysisOwners())), t);
                    AnalysisStateManager.setState(analysisOwners().stream().map(LevelAnalysisOwner::serverLevel).collect(Collectors.toList()), AnalysisState.ERRORED);
                }
                LOGGER.info(String.format("Finished aequivaleo data reload for world: %s", WorldUtils.formatWorldNames(analysisOwners())));
            }
            
            private static Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> groupDataByContainer(final List<CompoundInstanceData> data) {
                return GroupingUtils.groupByUsingSet(
                                data,
                                CompoundInstanceData::containers
                        )
                               .stream()
                               .collect(Collectors.toMap(
                                       c -> c.iterator().next().containers(),
                                       Function.identity()
                               ));
            }
            
            private void processCompoundInstanceData(
                    final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> generalData,
                    final Map<Set<ICompoundContainer<?>>, Collection<CompoundInstanceData>> worldData,
                    final Map<ICompoundContainer<?>, Set<CompoundInstance>> target) {
                final Set<Set<ICompoundContainer<?>>> keys = ImmutableSet.<Set<ICompoundContainer<?>>>builder()
                                                                     .addAll(generalData.keySet())
                                                                     .addAll(worldData.keySet())
                                                                     .build();
                
                keys.forEach(container -> {
                    generalData.getOrDefault(container, Sets.newHashSet()).stream().sorted(Comparator.comparingInt(compoundInstanceData -> compoundInstanceData.mode()
                                                                                                                                                   .ordinal())).forEachOrdered(
                            compoundInstanceData -> compoundInstanceData.handle(target)
                    );
                    
                    worldData.getOrDefault(container, Sets.newHashSet()).stream().sorted(Comparator.comparingInt(compoundInstanceData -> compoundInstanceData.mode()
                                                                                                                                                 .ordinal())).forEachOrdered(
                            compoundInstanceData -> compoundInstanceData.handle(target)
                    );
                });
            }
        }
    
    public static class DataDrivenData {
        final Map<ResourceLocation, List<CompoundInstanceData>> valueData = new HashMap<>();
        final Map<ResourceLocation, List<CompoundInstanceData>> lockedData = new HashMap<>();
        final Map<ResourceLocation, List<CompoundInstanceData>> baseData = new HashMap<>();
        final Map<ResourceLocation, List<IEquivalencyRecipe>> dataDrivenRecipes = new HashMap<>();
    }
    
    private record LevelAnalysisOwner(ServerLevel serverLevel) implements IAnalysisOwner {
        @Override
        public ResourceKey<Level> getIdentifier() {
            return serverLevel.dimension();
        }
        
        @Override
        public File getCacheDirectory() {
            final File aequivaleoDirectory = this.serverLevel().getServer().getWorldPath(AEQUIVALEO_DATA).toFile();
            final File cacheDirectory = new File(aequivaleoDirectory, "cache");
            return new File(cacheDirectory, String.format("%s_%s", serverLevel.dimension().location().getNamespace(), serverLevel.dimension().location().getPath()));
        }
    }
}
