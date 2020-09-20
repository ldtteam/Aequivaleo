package com.ldtteam.aequivaleo.api.compound.information.locked;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.compound.information.CompoundInstanceData;
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

public abstract class LockedInformationDataGenerator implements IDataProvider
{

    private final String modName;
    private final DataGenerator dataGenerator;
    private final WorldData generalData = new WorldData(new ResourceLocation(Constants.MOD_ID, "general")) {
        @Override
        public String getPath()
        {
            return "general";
        }
    };
    private final Map<ResourceLocation, WorldData> worldDataMap = Maps.newHashMap();


    protected LockedInformationDataGenerator(final String modName, final DataGenerator dataGenerator) {
        this.modName = modName;
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
        final Path dataSavePath = dataGenerator.getOutputFolder().resolve("aequivaleo/locked/" + worldData.getPath());
        for (Map.Entry<ICompoundContainer<?>, Pair<Boolean, Set<CompoundInstance>>> entry : worldData.getDataToWrite().entrySet())
        {
            ICompoundContainer<?> container = entry.getKey();
            Pair<Boolean, Set<CompoundInstance>> instancesAndReplacing = entry.getValue();
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
        return StringUtils.capitalize(modName) + " locked information data generator.";
    }

    public abstract void calculateDataToSave();

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

    private static class WorldData {
        private final ResourceLocation worldId;
        private final Map<ICompoundContainer<?>, Pair<Boolean, Set<CompoundInstance>>> dataToWrite = Maps.newHashMap();

        private WorldData(final ResourceLocation worldId) {
            this.worldId = worldId;
        }

        public ResourceLocation getWorldId()
        {
            return worldId;
        }

        public Map<ICompoundContainer<?>, Pair<Boolean, Set<CompoundInstance>>> getDataToWrite()
        {
            return dataToWrite;
        }

        public String getPath() {
            return worldId.getNamespace() + "/" + worldId.getPath();
        }
    }
}
