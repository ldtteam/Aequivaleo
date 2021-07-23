package com.ldtteam.aequivaleo.compound.information;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.information.ICompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class CompoundInformationRegistry implements ICompoundInformationRegistry
{
    private static final Map<ResourceKey<Level>, CompoundInformationRegistry> INSTANCES = Maps.newConcurrentMap();

    public static CompoundInformationRegistry getInstance(@NotNull final ResourceKey<Level> worldKey)
    {
        return INSTANCES.computeIfAbsent(worldKey, (dimType) -> new CompoundInformationRegistry());
    }

    private CompoundInformationRegistry()
    {
    }

    private final Map<ICompoundContainer<?>, ImmutableSet<CompoundInstance>> valueInformation = Maps.newConcurrentMap();
    private final Map<ICompoundContainer<?>, ImmutableSet<CompoundInstance>> lockedInformation = Maps.newConcurrentMap();

    @Override
    public ICompoundInformationRegistry registerValue(
      @NotNull final ICompoundContainer<?> wrapper, @NotNull final Set<CompoundInstance> compounds)
    {
        if (wrapper.getContentsCount() != 1)
            throw new IllegalArgumentException("Can not set locked information with none unit stack.");

        valueInformation.put(wrapper, ImmutableSet.copyOf(compounds));

        return this;
    }

    @Override
    public <T> ICompoundInformationRegistry registerValue(
      @NotNull final T gameObjectInstanceToAssign, @NotNull final Set<CompoundInstance> compounds)
    {
        return registerValue(
          CompoundContainerFactoryManager.getInstance().wrapInContainer(gameObjectInstanceToAssign, 1d),
          compounds
        );
    }

    @Override
    public ICompoundInformationRegistry registerLocking(
      @NotNull final ICompoundContainer<?> wrapper, @NotNull final Set<CompoundInstance> instances)
    {
        if (wrapper.getContentsCount() != 1)
            throw new IllegalArgumentException("Can not set locked information with none unit stack.");

        lockedInformation.put(wrapper, ImmutableSet.copyOf(instances));

        return this;
    }

    @Override
    public <T> ICompoundInformationRegistry registerLocking(
      @NotNull final T tInstance, @NotNull final Set<CompoundInstance> instances)
    {
        return registerLocking(
          CompoundContainerFactoryManager.getInstance().wrapInContainer(tInstance, 1d),
          instances
        );
    }

    public void reset()
    {
        valueInformation.clear();
        lockedInformation.clear();
    }

    public ImmutableMap<ICompoundContainer<?>, ImmutableSet<CompoundInstance>> getValueInformation()
    {
        return ImmutableMap.copyOf(valueInformation);
    }

    public ImmutableMap<ICompoundContainer<?>, ImmutableSet<CompoundInstance>> getLockingInformation()
    {
        return ImmutableMap.copyOf(lockedInformation);
    }
}
