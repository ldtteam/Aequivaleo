package com.ldtteam.aequivaleo.api.compound.information.locked;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.compound.information.CompoundInstanceData;
import com.ldtteam.aequivaleo.api.compound.information.CompoundInstanceRef;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class LockedInformationProvider implements IDataProvider
{

    private final String        modId;
    private final DataGenerator dataGenerator;
    private final WorldData generalData = new WorldData(new ResourceLocation(Constants.MOD_ID, "general")) {
        @Override
        public String getPath()
        {
            return "general";
        }
    };
    private final Map<ResourceLocation, WorldData> worldDataMap = Maps.newHashMap();


    protected LockedInformationProvider(final String modId, final DataGenerator dataGenerator) {
        this.modId = modId;
        this.dataGenerator = dataGenerator;
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

    private void writeData(
      final DirectoryCache cache,
      final Gson gson,
      final WorldData worldData
    ) throws IOException
    {
        final Path dataSavePath = dataGenerator.getOutputFolder().resolve(String.format("data/%s/aequivaleo/locked/%s", modId, worldData.getPath()));
        for (Map.Entry<ICompoundContainer<?>, Pair<Boolean, Set<CompoundInstanceRef>>> entry : worldData.getDataToWrite().entrySet())
        {
            ICompoundContainer<?> container = entry.getKey();
            Pair<Boolean, Set<CompoundInstanceRef>> instancesAndReplacing = entry.getValue();
            final CompoundInstanceData data =
              new CompoundInstanceData(
                instancesAndReplacing.getLeft() ? CompoundInstanceData.Mode.REPLACING : CompoundInstanceData.Mode.ADDITIVE,
                container,
                instancesAndReplacing.getRight()
              );

            final Path itemPath = dataSavePath.resolve(data.getContainer().getContentAsFileName() + ".json");

            IDataProvider.save(
              gson,
              cache,
              gson.toJsonTree(data),
              itemPath
            );
        }
    }

    @Override
    public String getName()
    {
        return StringUtils.capitalize(modId) + " locked information data generator.";
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
        final ICompoundContainer<?> container = ICompoundContainerFactoryManager
          .getInstance()
          .wrapInContainer(
            gameObject,
            1d
          );

        if (!container.canBeLoadedFromDisk())
            throw new IllegalArgumentException("The given game object can not be saved to disk.");

        this.generalData
          .getDataToWrite()
          .put(
            container,
            Pair.of(replacing, instances)
          );
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
        final ICompoundContainer<?> container = ICompoundContainerFactoryManager
                                                  .getInstance()
                                                  .wrapInContainer(
                                                    gameObject,
                                                    1d
                                                  );

        if (!container.canBeLoadedFromDisk())
            throw new IllegalArgumentException("The given game object can not be saved to disk.");

        this.worldDataMap
          .computeIfAbsent(worldId, WorldData::new)
          .getDataToWrite()
          .put(
            container,
            Pair.of(replacing, instances)
          );
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

    private static class WorldData {
        private final ResourceLocation worldId;
        private final Map<ICompoundContainer<?>, Pair<Boolean, Set<CompoundInstanceRef>>> dataToWrite = Maps.newHashMap();

        private WorldData(final ResourceLocation worldId) {
            this.worldId = worldId;
        }

        public ResourceLocation getWorldId()
        {
            return worldId;
        }

        public Map<ICompoundContainer<?>, Pair<Boolean, Set<CompoundInstanceRef>>> getDataToWrite()
        {
            return dataToWrite;
        }

        public String getPath() {
            return worldId.getNamespace() + "/" + worldId.getPath();
        }
    }
}