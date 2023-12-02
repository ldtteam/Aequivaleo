package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.google.common.annotations.VisibleForTesting;
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

@SuppressWarnings("unused")
public abstract class AbstractInformationProvider implements DataProvider
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Codec<Optional<WithConditions<CompoundInstanceData>>> INSTANCE_CODEC =
        ConditionalOps.createConditionalCodecWithConditions(CompoundInstanceData.CODEC);

    @VisibleForTesting
    final WorldData generalData = new WorldData(new ResourceLocation(Constants.MOD_ID, "general")) {
        @Override
        public String getPath()
        {
            return "general";
        }
    };
    @VisibleForTesting
    final Map<ResourceLocation, WorldData> worldDataMap = Maps.newHashMap();

    private final CompletableFuture<HolderLookup.Provider> holderLookupProvider;
    
    protected AbstractInformationProvider(CompletableFuture<HolderLookup.Provider> holderLookupProvider) {
        this.holderLookupProvider = holderLookupProvider;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull final CachedOutput cache) {
        return holderLookupProvider.thenCompose(holderLookup -> runInternal(cache, holderLookup));
    }
    
    
    public @NotNull CompletableFuture<?> runInternal(@NotNull final CachedOutput cache, HolderLookup.Provider holderLookupProvider) {
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
          getGeneralData()
        ));

        for (WorldData worldData : this.getWorldDataMap().values())
        {
            futures.add(this.writeData(
              cache,
              ops,
              worldData
            ));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    protected abstract Set<Path> getPathsToWrite(String worldPath);

    @VisibleForTesting
    WorldData getGeneralData()
    {
        return generalData;
    }

    @VisibleForTesting
    Map<ResourceLocation, WorldData> getWorldDataMap()
    {
        return worldDataMap;
    }

    @VisibleForTesting
    @NotNull
    CompletableFuture<?> writeData(
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

    public abstract void calculateDataToSave();
    
    protected SpecBuilder specFor(final TagKey<?> tag) {
        return new SpecBuilder(tag);
    }
    
    protected SpecBuilder specFor(final Object... targets)
    {
        return new SpecBuilder(targets);
    }
    
    protected SpecBuilder specFor(final Iterable<Object> targets) {
        return new SpecBuilder(targets);
    }

    protected final void save(
      final SpecBuilder specBuilder
    ) {
        specBuilder.process(this.getGeneralData().getDataToWrite());
    }

    protected final void save(
      final ResourceLocation worldId,
      final SpecBuilder specBuilder
    ) {
        specBuilder.process(this.getWorldDataMap()
          .computeIfAbsent(worldId, WorldData::new)
          .getDataToWrite());
    }

    @VisibleForTesting
    static class WorldData {
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

    @VisibleForTesting
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

        public SpecBuilder withMode(final CompoundInstanceData.Mode mode) {
            this.mode = mode;
            return this;
        }

        public SpecBuilder replaces() {
            return this.withMode(CompoundInstanceData.Mode.REPLACING);
        }

        public SpecBuilder additive() {
            return this.withMode(CompoundInstanceData.Mode.ADDITIVE);
        }

        public SpecBuilder replaces(final boolean replaces) {
            return this.withMode(replaces ? CompoundInstanceData.Mode.REPLACING : CompoundInstanceData.Mode.ADDITIVE);
        }

        public SpecBuilder withCompounds(final CompoundInstance... instances) {
            return this.withCompounds(Arrays.asList(instances));
        }

        public SpecBuilder withCompounds(final Iterable<CompoundInstance> instances) {
            for (final CompoundInstance instance : instances)
            {
                this.instanceRefs.add(instance.asRef());
            }

            return this;
        }

        public SpecBuilder withCompoundRefs(final CompoundInstanceRef... refs) {
            return this.withCompoundRefs(Arrays.asList(refs));
        }

        public SpecBuilder withCompoundRefs(final Iterable<CompoundInstanceRef> refs) {
            for (final CompoundInstanceRef ref : refs)
            {
                this.instanceRefs.add(ref);
            }

            return this;
        }

        public SpecBuilder withConditions(final ICondition... conditions) {
            return this.withConditions(Arrays.asList(conditions));
        }

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
