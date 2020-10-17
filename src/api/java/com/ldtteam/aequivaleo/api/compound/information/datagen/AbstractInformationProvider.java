package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractInformationProvider implements IDataProvider
{

    private static final Logger LOGGER = LogManager.getLogger();

    private final WorldData generalData = new WorldData(new ResourceLocation(Constants.MOD_ID, "general")) {
        @Override
        public String getPath()
        {
            return "general";
        }
    };
    private final Map<ResourceLocation, WorldData> worldDataMap = Maps.newHashMap();


    protected AbstractInformationProvider() {
    }

    @Override
    public final void act(final DirectoryCache cache) throws IOException
    {
        this.calculateDataToSave();

        final Gson gson = IAequivaleoAPI.getInstance().getGson();

        this.writeData(
          cache,
          gson,
          generalData
        );

        for (WorldData worldData : this.worldDataMap.values())
        {
            this.writeData(
              cache,
              gson,
              worldData
            );
        }
    }

    protected abstract Set<Path> getPathsToWrite(String worldPath);

    private void writeData(
      final DirectoryCache cache,
      final Gson gson,
      final WorldData worldData
    ) throws IOException
    {
        for (Path dataSavePath : getPathsToWrite(worldData.getPath()))
        {
            for (Map.Entry<Set<ICompoundContainer<?>>, Pair<Boolean, Set<CompoundInstanceRef>>> entry : worldData.getDataToWrite().entrySet())
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

                Pair<Boolean, Set<CompoundInstanceRef>> instancesAndReplacing = entry.getValue();
                final CompoundInstanceData data =
                  new CompoundInstanceData(
                    instancesAndReplacing.getLeft() ? CompoundInstanceData.Mode.REPLACING : CompoundInstanceData.Mode.ADDITIVE,
                    containers,
                    instancesAndReplacing.getRight()
                  );

                final String fileName = data.getContainers()
                                          .stream()
                                          .filter(ICompoundContainer::canBeLoadedFromDisk)
                                          .map(ICompoundContainer::getContentAsFileName)
                                          .findFirst()
                                          .orElseThrow(() -> new IllegalStateException("Could not find disk loadable container, even though previous check passed!"));

                final Path itemPath = dataSavePath.resolve(String.format("%s.json", fileName));

                IDataProvider.save(
                  gson,
                  cache,
                  gson.toJsonTree(data),
                  itemPath
                );
            }
        }
    }

    public abstract void calculateDataToSave();

    public final void saveDataRefs(
      final Object gameObject,
      final CompoundInstanceRef... instances
    ) {
        this.saveDataRefs(
          gameObject,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final Object gameObject,
      final boolean replacing,
      final CompoundInstanceRef... instances
    ) {
        this.saveDataRefs(
          gameObject,
          replacing,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final Object gameObject,
      final Set<CompoundInstanceRef> instances
    ) {
        this.saveDataRefs(
          gameObject,
          false,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final Object gameObject,
      final boolean replacing,
      final Set<CompoundInstanceRef> instances
    ) {
        this.saveDataRefs(Sets.newHashSet(gameObject), replacing, instances);
    }

    public final void saveDataRefs(
      final ResourceLocation worldId,
      final Object gameObject,
      final CompoundInstanceRef... instances
    ) {
        this.saveDataRefs(
          worldId,
          gameObject,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final ResourceLocation worldId,
      final Object gameObject,
      final boolean replacing,
      final CompoundInstanceRef... instances
    ) {
        this.saveDataRefs(
          worldId,
          gameObject,
          replacing,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final ResourceLocation worldId,
      final Object gameObject,
      final Set<CompoundInstanceRef> instances
    ) {
        this.saveDataRefs(
          worldId,
          gameObject,
          false,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final ResourceLocation worldId,
      final Object gameObject,
      final boolean replacing,
      final Set<CompoundInstanceRef> instances
    ) {
        this.saveDataRefs(worldId, Sets.newHashSet(gameObject), replacing, instances);
    }

    public final void saveData(
      final Object gameObject,
      final CompoundInstance... instances
    ) {
        this.saveData(
          gameObject,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final Object gameObject,
      final boolean replacing,
      final CompoundInstance... instances
    ) {
        this.saveData(
          gameObject,
          replacing,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final Object gameObject,
      final Set<CompoundInstance> instances
    ) {
        this.saveData(
          gameObject,
          false,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final Object gameObject,
      final boolean replacing,
      final Set<CompoundInstance> instances
    ) {
        this.saveDataRefs(
          gameObject,
          replacing,
          instances.stream().map(CompoundInstance::asRef).collect(Collectors.toSet())
        );
    }

    public final void saveData(
      final ResourceLocation worldId,
      final Object gameObject,
      final CompoundInstance... instances
    ) {
        this.saveData(
          worldId,
          gameObject,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final ResourceLocation worldId,
      final Object gameObject,
      final boolean replacing,
      final CompoundInstance... instances
    ) {
        this.saveData(
          worldId,
          gameObject,
          replacing,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final ResourceLocation worldId,
      final Object gameObject,
      final Set<CompoundInstance> instances
    ) {
        this.saveData(
          worldId,
          gameObject,
          false,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final ResourceLocation worldId,
      final Object gameObject,
      final boolean replacing,
      final Set<CompoundInstance> instances
    ) {
        this.saveDataRefs(
          worldId,
          gameObject,
          replacing,
          instances.stream().map(CompoundInstance::asRef).collect(Collectors.toSet())
        );
    }

    public final void saveDataRefs(
      final Set<Object> gameObjects,
      final CompoundInstanceRef... instances
    ) {
        this.saveDataRefs(
          gameObjects,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final Set<Object> gameObjects,
      final boolean replacing,
      final CompoundInstanceRef... instances
    ) {
        this.saveDataRefs(
          gameObjects,
          replacing,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final Set<Object> gameObjects,
      final Set<CompoundInstanceRef> instances
    ) {
        this.saveDataRefs(
          gameObjects,
          false,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final Set<Object> gameObjects,
      final boolean replacing,
      final Set<CompoundInstanceRef> instances
    ) {
        final Set<ICompoundContainer<?>> containers = gameObjects.stream().map(gameObject -> ICompoundContainerFactoryManager
                                                  .getInstance()
                                                  .wrapInContainer(
                                                    gameObject,
                                                    1d
                                                  )).collect(Collectors.toSet());

        this.generalData
          .getDataToWrite()
          .put(
            containers,
            Pair.of(replacing, instances)
          );
    }

    public final void saveDataRefs(
      final ResourceLocation worldId,
      final Set<Object> gameObjects,
      final CompoundInstanceRef... instances
    ) {
        this.saveDataRefs(
          worldId,
          gameObjects,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final ResourceLocation worldId,
      final Set<Object> gameObjects,
      final boolean replacing,
      final CompoundInstanceRef... instances
    ) {
        this.saveDataRefs(
          worldId,
          gameObjects,
          replacing,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final ResourceLocation worldId,
      final Set<Object> gameObjects,
      final Set<CompoundInstanceRef> instances
    ) {
        this.saveDataRefs(
          worldId,
          gameObjects,
          false,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final ResourceLocation worldId,
      final Set<Object> gameObjects,
      final boolean replacing,
      final Set<CompoundInstanceRef> instances
    ) {
        final Set<ICompoundContainer<?>> containers = gameObjects.stream().map(gameObject -> ICompoundContainerFactoryManager
                                                                                               .getInstance()
                                                                                               .wrapInContainer(
                                                                                                 gameObject,
                                                                                                 1d
                                                                                               )).collect(Collectors.toSet());

        this.worldDataMap
          .computeIfAbsent(worldId, WorldData::new)
          .getDataToWrite()
          .put(
            containers,
            Pair.of(replacing, instances)
          );
    }

    public final void saveData(
      final Set<Object> gameObjects,
      final CompoundInstance... instances
    ) {
        this.saveData(
          gameObjects,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final Set<Object> gameObjects,
      final boolean replacing,
      final CompoundInstance... instances
    ) {
        this.saveData(
          gameObjects,
          replacing,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final Set<Object> gameObjects,
      final Set<CompoundInstance> instances
    ) {
        this.saveData(
          gameObjects,
          false,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final Set<Object> gameObjects,
      final boolean replacing,
      final Set<CompoundInstance> instances
    ) {
        this.saveDataRefs(
          gameObjects,
          replacing,
          instances.stream().map(CompoundInstance::asRef).collect(Collectors.toSet())
        );
    }

    public final void saveData(
      final ResourceLocation worldId,
      final Set<Object> gameObjects,
      final CompoundInstance... instances
    ) {
        this.saveData(
          worldId,
          gameObjects,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final ResourceLocation worldId,
      final Set<Object> gameObjects,
      final boolean replacing,
      final CompoundInstance... instances
    ) {
        this.saveData(
          worldId,
          gameObjects,
          replacing,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final ResourceLocation worldId,
      final Set<Object> gameObjects,
      final Set<CompoundInstance> instances
    ) {
        this.saveData(
          worldId,
          gameObjects,
          false,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final ResourceLocation worldId,
      final Set<Object> gameObjects,
      final boolean replacing,
      final Set<CompoundInstance> instances
    ) {
        this.saveDataRefs(
          worldId,
          gameObjects,
          replacing,
          instances.stream().map(CompoundInstance::asRef).collect(Collectors.toSet())
        );
    }

    public final void saveDataRefs(
      final ITag<?> tag,
      final CompoundInstanceRef... instances
    ) {
        this.saveDataRefs(
          tag,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final ITag<?> tag,
      final boolean replacing,
      final CompoundInstanceRef... instances
    ) {
        this.saveDataRefs(
          tag,
          replacing,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final ITag<?> tag,
      final Set<CompoundInstanceRef> instances
    ) {
        this.saveDataRefs(
          tag,
          false,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final ITag<?> tag,
      final boolean replacing,
      final Set<CompoundInstanceRef> instances
    ) {
        this.saveDataRefs(Sets.newHashSet(tag.getAllElements()), replacing, instances);
    }

    public final void saveDataRefs(
      final ResourceLocation worldId,
      final ITag<?> tag,
      final CompoundInstanceRef... instances
    ) {
        this.saveDataRefs(
          worldId,
          tag,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final ResourceLocation worldId,
      final ITag<?> tag,
      final boolean replacing,
      final CompoundInstanceRef... instances
    ) {
        this.saveDataRefs(
          worldId,
          tag,
          replacing,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final ResourceLocation worldId,
      final ITag<?> tag,
      final Set<CompoundInstanceRef> instances
    ) {
        this.saveDataRefs(
          worldId,
          tag,
          false,
          Sets.newHashSet(instances)
        );
    }

    public final void saveDataRefs(
      final ResourceLocation worldId,
      final ITag<?> tag,
      final boolean replacing,
      final Set<CompoundInstanceRef> instances
    ) {
        this.saveDataRefs(worldId, Sets.newHashSet(tag.getAllElements()), replacing, instances);
    }

    public final void saveData(
      final ITag<?> tag,
      final CompoundInstance... instances
    ) {
        this.saveData(
          tag,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final ITag<?> tag,
      final boolean replacing,
      final CompoundInstance... instances
    ) {
        this.saveData(
          tag,
          replacing,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final ITag<?> tag,
      final Set<CompoundInstance> instances
    ) {
        this.saveData(
          tag,
          false,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final ITag<?> tag,
      final boolean replacing,
      final Set<CompoundInstance> instances
    ) {
        this.saveDataRefs(
          tag,
          replacing,
          instances.stream().map(CompoundInstance::asRef).collect(Collectors.toSet())
        );
    }

    public final void saveData(
      final ResourceLocation worldId,
      final ITag<?> tag,
      final CompoundInstance... instances
    ) {
        this.saveData(
          worldId,
          tag,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final ResourceLocation worldId,
      final ITag<?> tag,
      final boolean replacing,
      final CompoundInstance... instances
    ) {
        this.saveData(
          worldId,
          tag,
          replacing,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final ResourceLocation worldId,
      final ITag<?> tag,
      final Set<CompoundInstance> instances
    ) {
        this.saveData(
          worldId,
          tag,
          false,
          Sets.newHashSet(instances)
        );
    }

    public final void saveData(
      final ResourceLocation worldId,
      final ITag<?> tag,
      final boolean replacing,
      final Set<CompoundInstance> instances
    ) {
        this.saveDataRefs(
          worldId,
          tag,
          replacing,
          instances.stream().map(CompoundInstance::asRef).collect(Collectors.toSet())
        );
    }

    private static class WorldData {
        private final ResourceLocation worldId;
        private final Map<Set<ICompoundContainer<?>>, Pair<Boolean, Set<CompoundInstanceRef>>> dataToWrite = Maps.newHashMap();

        private WorldData(final ResourceLocation worldId) {
            this.worldId = worldId;
        }

        public ResourceLocation getWorldId()
        {
            return worldId;
        }

        public Map<Set<ICompoundContainer<?>>, Pair<Boolean, Set<CompoundInstanceRef>>> getDataToWrite()
        {
            return dataToWrite;
        }

        public String getPath() {
            return worldId.getNamespace() + "/" + worldId.getPath();
        }
    }
}
