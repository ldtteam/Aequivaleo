package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.compound.information.datagen.data.CompoundInstanceData;
import com.ldtteam.aequivaleo.api.compound.information.datagen.data.CompoundInstanceRef;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * An abstract data provider for compound information.
 * <p>
 *     This base class does not define the type or kind of compound information, just how it is supposed to be saved.
 *     See its implementations for more information.
 * </p>
 * @see BaseInformationProvider
 * @see ForcedInformationProvider
 * @see LockedInformationProvider
 * @see ValueInformationProvider
 */
@SuppressWarnings("unused")
public abstract class AbstractInformationProvider implements DataProvider
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Codec<Optional<WithConditions<CompoundInstanceData>>> INSTANCE_CODEC =
        ConditionalOps.createConditionalCodecWithConditions(CompoundInstanceData.CODEC);

    private final WorldData generalData = new WorldData(new ResourceLocation(Constants.MOD_ID, "general")) {
        @Override
        public String getPath()
        {
            return "general";
        }
    };
    private final Map<ResourceLocation, WorldData> worldDataMap = Maps.newHashMap();

    private final CompletableFuture<HolderLookup.Provider> holderLookupProvider;

    /**
     * Creates a new abstract information provider.
     *
     * @param holderLookupProvider The holder lookup provider.
     */
    protected AbstractInformationProvider(CompletableFuture<HolderLookup.Provider> holderLookupProvider) {
        this.holderLookupProvider = holderLookupProvider;
    }

    /**
     * Runs the data generation.
     *
     * @param cache The cache to save the data to.
     * @return A future that completes when the data generation is done.
     */
    @Override
    public @NotNull CompletableFuture<?> run(@NotNull final CachedOutput cache) {
        return holderLookupProvider.thenCompose(holderLookup -> runInternal(cache, holderLookup));
    }

    private @NotNull CompletableFuture<?> runInternal(@NotNull final CachedOutput cache, HolderLookup.Provider holderLookupProvider) {
        this.calculateDataToSave();

        final List<CompletableFuture<?>> futures = new ArrayList<>();
        
        final ConditionalOps<JsonElement> ops = ConditionalOps.create(
                RegistryOps.create(
                        JsonOps.INSTANCE,
                        holderLookupProvider
                ),
                ICondition.IContext.EMPTY
        );
        
        futures.add(this.writeData(
          cache,
          ops,
          generalData
        ));

        for (WorldData worldData : this.worldDataMap.values())
        {
            futures.add(this.writeData(
              cache,
              ops,
              worldData
            ));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Gets the path prefixes to write the data to.
     *
     * @param worldPath The path of the world to write the data to.
     * @return The path prefixes to write the data to.
     */
    protected abstract Set<Path> getPathsToWrite(String worldPath);

    @NotNull
    private CompletableFuture<?> writeData(
      final CachedOutput cache,
      final ConditionalOps<JsonElement> gson,
      final WorldData worldData
    )
    {
        final List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Path dataSavePath : getPathsToWrite(worldData.getPath()))
        {
            for (WithConditions<CompoundInstanceData> dataToWrite : worldData.getDataToWrite()) {
                final String fileName = dataToWrite.carrier().containers()
                                                .stream()
                                                .filter(ICompoundContainer::canBeLoadedFromDisk)
                                                .sorted(Comparator.comparing(ICompoundContainer::getContentAsFileName))
                                                .map(ICompoundContainer::getContentAsFileName)
                                                .findFirst()
                                                .orElseThrow(() -> new IllegalStateException("Could not find disk loadable container, even though previous check passed!"));
                
                final Path itemPath = dataSavePath.resolve(String.format("%s.json", fileName));
                
                futures.add(CompletableFuture.supplyAsync(() -> INSTANCE_CODEC.encodeStart(gson, Optional.of(dataToWrite)).getOrThrow(false, msg -> LOGGER.error("Failed to encode some components for {}: {}", itemPath.toFile().getAbsolutePath(), msg)))
                                    .thenCompose(json -> DataProvider.saveStable(cache, json, itemPath)));
            }
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Invoked to calculate the data to save.
     */
    protected abstract void calculateDataToSave();

    /**
     * Creates a new specification builder that can be used to give a given tag a specific compound instance data set.
     *
     * @param tag The tag to create the specification for.
     * @return The specification builder.
     */
    protected SpecBuilder specFor(final TagKey<?> tag) {
        return new SpecBuilder(tag);
    }

    /**
     * Creates a new specification builder that can be used to give a given set of targets a specific compound instance data set.
     *
     * @param targets The targets to create the specification for.
     * @return The specification builder.
     */
    protected SpecBuilder specFor(final Object... targets)
    {
        return new SpecBuilder(targets);
    }

    /**
     * Creates a new specification builder that can be used to give a given set of targets a specific compound instance data set.
     *
     * @param targets The targets to create the specification for.
     * @return The specification builder.
     */
    protected SpecBuilder specFor(final Iterable<Object> targets) {
        return new SpecBuilder(targets);
    }

    /**
     * Adds the information defined in the given specification builder to the general data.
     *
     * @param specBuilder The specification builder.
     */
    protected final void addInformation(
      final SpecBuilder specBuilder
    ) {
        specBuilder.process(this.generalData.getDataToWrite());
    }

    /**
     * Adds the information defined in the given specification builder to the data of the given world.
     *
     * @param worldId The id of the world to add the information to.
     * @param specBuilder The specification builder.
     */
    protected final void addInformation(
      final ResourceLocation worldId,
      final SpecBuilder specBuilder
    ) {
        specBuilder.process(this.worldDataMap
          .computeIfAbsent(worldId, WorldData::new)
          .getDataToWrite());
    }

    private static class WorldData {
        private final ResourceLocation worldId;
        private final List<WithConditions<CompoundInstanceData>> dataToWrite = Lists.newArrayList();

        private WorldData(final ResourceLocation worldId) {
            this.worldId = worldId;
        }

        public ResourceLocation getWorldId()
        {
            return worldId;
        }

        public List<WithConditions<CompoundInstanceData>> getDataToWrite()
        {
            return dataToWrite;
        }

        public String getPath() {
            return worldId.getNamespace() + "/" + worldId.getPath();
        }
    }

    /**
     * A specification builder for compound instance data.
     * <p>
     *     Its base implementation is a mutable builder that can be used to create a compound instance data specification.
     * </p>
     */
    protected static class SpecBuilder {
        private final Set<Object> targets = Sets.newLinkedHashSet();
        private CompoundInstanceData.Mode mode = CompoundInstanceData.Mode.ADDITIVE;
        private final Set<CompoundInstanceRef> instanceRefs = Sets.newLinkedHashSet();
        private final List<ICondition> conditions = Lists.newArrayList();

        private SpecBuilder(final TagKey<?> tag) {
            this.targets.add(tag);
        }

        private SpecBuilder(final Object... targets)
        {
            this.targets.addAll(Arrays.asList(targets));
        }

        private SpecBuilder(final Iterable<Object> targets) {
            for (final Object target : targets)
            {
                this.targets.add(target);
            }
        }

        /**
         * Sets the mode of the compound instance data specification.
         *
         * @param mode The mode to use for combination of data when multiple specifications for it are found.
         * @return A specification builder with the value set.
         */
        public SpecBuilder withMode(final CompoundInstanceData.Mode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Sets the mode of the compound instance data specification to replacing.
         *
         * @return A specification builder with the value set.
         */
        public SpecBuilder replaces() {
            return this.withMode(CompoundInstanceData.Mode.REPLACING);
        }

        /**
         * Sets the mode of the compound instance data specification to additive.
         *
         * @return A specification builder with the value set.
         */
        public SpecBuilder additive() {
            return this.withMode(CompoundInstanceData.Mode.ADDITIVE);
        }

        /**
         * Sets the mode of the compound instance data specification to replacing or additive.
         *
         * @param replaces If true, the mode is set to replacing, otherwise to additive.
         * @return A specification builder with the value set.
         */
        public SpecBuilder replaces(final boolean replaces) {
            return this.withMode(replaces ? CompoundInstanceData.Mode.REPLACING : CompoundInstanceData.Mode.ADDITIVE);
        }

        /**
         * Adds the given compound instances to the compound instance data specification.
         *
         * @param instances The compound instances to add.
         * @return A specification builder with the values set.
         */
        public SpecBuilder withCompounds(final CompoundInstance... instances) {
            return this.withCompounds(Arrays.asList(instances));
        }

        /**
         * Adds the given compound instances to the compound instance data specification.
         *
         * @param instances The compound instances to add.
         * @return A specification builder with the values set.
         */
        public SpecBuilder withCompounds(final Iterable<CompoundInstance> instances) {
            for (final CompoundInstance instance : instances)
            {
                this.instanceRefs.add(instance.asRef());
            }

            return this;
        }

        /**
         * Adds the given compound instance references to the compound instance data specification.
         *
         * @param refs The compound instance references to add.
         * @return A specification builder with the values set.
         */
        public SpecBuilder withCompoundRefs(final CompoundInstanceRef... refs) {
            return this.withCompoundRefs(Arrays.asList(refs));
        }

        /**
         * Adds the given compound instance references to the compound instance data specification.
         *
         * @param refs The compound instance references to add.
         * @return A specification builder with the values set.
         */
        public SpecBuilder withCompoundRefs(final Iterable<CompoundInstanceRef> refs) {
            for (final CompoundInstanceRef ref : refs)
            {
                this.instanceRefs.add(ref);
            }

            return this;
        }

        /**
         * Adds the given conditions to the compound instance data specification.
         *
         * @param conditions The conditions to add.
         * @return A specification builder with the values set.
         */
        public SpecBuilder withConditions(final ICondition... conditions) {
            return this.withConditions(Arrays.asList(conditions));
        }

        /**
         * Adds the given conditions to the compound instance data specification.
         *
         * @param conditions The conditions to add.
         * @return A specification builder with the values set.
         */
        public SpecBuilder withConditions(final Iterable<ICondition> conditions) {
            for (final ICondition condition : conditions)
            {
                this.conditions.add(condition);
            }

            return this;
        }

        private void process(List<WithConditions<CompoundInstanceData>> specs) {
            final Set<ICompoundContainer<?>> containers = this.targets.stream().map(gameObject -> ICompoundContainerFactoryManager
                                                                                                   .getInstance()
                                                                                                   .wrapInContainer(
                                                                                                     gameObject,
                                                                                                     1d
                                                                                                   )).collect(Collectors.toCollection(LinkedHashSet::new));

            final CompoundInstanceData dataSpec = new CompoundInstanceData(
              this.mode,
              containers,
              this.instanceRefs
            );

            specs.add(new WithConditions<>(this.conditions, dataSpec));
        }
    }
}
