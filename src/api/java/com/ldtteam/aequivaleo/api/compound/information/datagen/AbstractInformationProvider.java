package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.compound.information.datagen.data.CompoundInstanceData;
import com.ldtteam.aequivaleo.api.compound.information.datagen.data.CompoundInstanceRef;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractInformationProvider implements DataProvider
{

    private static final Logger LOGGER = LogManager.getLogger();

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


    protected AbstractInformationProvider() {
    }

    @Override
    public void run(@NotNull final CachedOutput cache) throws IOException
    {
        this.calculateDataToSave();

        final Gson gson = IAequivaleoAPI.getInstance().getGson(ICondition.IContext.EMPTY);

        this.writeData(
          cache,
          gson,
          getGeneralData()
        );

        for (WorldData worldData : this.getWorldDataMap().values())
        {
            this.writeData(
              cache,
              gson,
              worldData
            );
        }
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
    void writeData(
      final CachedOutput cache,
      final Gson gson,
      final WorldData worldData
    ) throws IOException
    {
        for (Path dataSavePath : getPathsToWrite(worldData.getPath()))
        {
            for (Map.Entry<Set<ICompoundContainer<?>>, DataSpec> entry : worldData.getDataToWrite().entrySet())
            {
                Set<ICompoundContainer<?>> containers = entry.getKey();
                if (containers.isEmpty())
                {
                    LOGGER.error("Can not write data if containers is empty!");
                    continue;
                }

                if (containers.stream().noneMatch(ICompoundContainer::canBeLoadedFromDisk))
                {
                    LOGGER.error("Can not write data if no containers can be loaded from disk!");
                    continue;
                }

                DataSpec spec = entry.getValue();
                final CompoundInstanceData data =
                  new CompoundInstanceData(
                    spec.mode,
                    containers,
                    spec.instanceRefs,
                    spec.conditions
                    );

                final String fileName = data.getContainers()
                                          .stream()
                                          .filter(ICompoundContainer::canBeLoadedFromDisk)
                                          .sorted(Comparator.comparing(ICompoundContainer::getContentAsFileName))
                                          .map(ICompoundContainer::getContentAsFileName)
                                          .findFirst()
                                          .orElseThrow(() -> new IllegalStateException("Could not find disk loadable container, even though previous check passed!"));

                final Path itemPath = dataSavePath.resolve(String.format("%s.json", fileName));

                DataProvider.saveStable(
                  cache,
                  gson.toJsonTree(data),
                  itemPath
                );
            }
        }
    }

    public abstract void calculateDataToSave();

    public SpecBuilder specFor(final TagKey<?> tag) {
        return new SpecBuilder(tag);
    }

    public SpecBuilder specFor(final Object... targets)
    {
        return new SpecBuilder(targets);
    }

    public SpecBuilder specFor(final Iterable<Object> targets) {
        return new SpecBuilder(targets);
    }

    public final void save(
      final SpecBuilder specBuilder
    ) {
        specBuilder.process(this.getGeneralData().getDataToWrite());
    }

    public final void save(
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
        private final Map<Set<ICompoundContainer<?>>, DataSpec> dataToWrite = Maps.newHashMap();

        private WorldData(final ResourceLocation worldId) {
            this.worldId = worldId;
        }

        public ResourceLocation getWorldId()
        {
            return worldId;
        }

        public Map<Set<ICompoundContainer<?>>, DataSpec> getDataToWrite()
        {
            return dataToWrite;
        }

        public String getPath() {
            return worldId.getNamespace() + "/" + worldId.getPath();
        }
    }

    @VisibleForTesting
    static class DataSpec {
        private final CompoundInstanceData.Mode mode;
        private final Set<CompoundInstanceRef> instanceRefs;
        private final Set<ICondition> conditions;

        DataSpec(
          final CompoundInstanceData.Mode mode,
          final Set<CompoundInstanceRef> instanceRefs,
          final Set<ICondition> conditions) {
            this.mode = mode;
            this.instanceRefs = instanceRefs;
            this.conditions = conditions;
        }
    }

    @VisibleForTesting
    protected static class SpecBuilder {
        private final Set<Object> targets = Sets.newLinkedHashSet();
        private CompoundInstanceData.Mode mode = CompoundInstanceData.Mode.ADDITIVE;
        private final Set<CompoundInstanceRef> instanceRefs = Sets.newLinkedHashSet();
        private final Set<ICondition> conditions = Sets.newLinkedHashSet();

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

        private void process(Map<Set<ICompoundContainer<?>>, DataSpec> specs) {
            final Set<ICompoundContainer<?>> containers = this.targets.stream().map(gameObject -> ICompoundContainerFactoryManager
                                                                                                   .getInstance()
                                                                                                   .wrapInContainer(
                                                                                                     gameObject,
                                                                                                     1d
                                                                                                   )).collect(Collectors.toCollection(LinkedHashSet::new));

            final DataSpec dataSpec = new DataSpec(
              this.mode,
              this.instanceRefs,
              this.conditions
            );

            specs.put(containers, dataSpec);
        }
    }
}
