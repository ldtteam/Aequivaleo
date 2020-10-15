package com.ldtteam.aequivaleo.compound.information.locked;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.information.locked.ILockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class LockedCompoundInformationRegistry implements ILockedCompoundInformationRegistry
{
    private static final Map<RegistryKey<World>, LockedCompoundInformationRegistry> INSTANCES = Maps.newConcurrentMap();

    public static LockedCompoundInformationRegistry getInstance(@NotNull final RegistryKey<World> worldKey)
    {
        return INSTANCES.computeIfAbsent(worldKey, (dimType) -> new LockedCompoundInformationRegistry());
    }

    private LockedCompoundInformationRegistry()
    {
    }

    private final Map<ICompoundContainer<?>, ImmutableSet<CompoundInstance>> valueInformation = Maps.newConcurrentMap();
    private final Map<ICompoundContainer<?>, ImmutableSet<CompoundInstance>> lockedInformation = Maps.newConcurrentMap();

    @Override
    public ILockedCompoundInformationRegistry registerValue(
      @NotNull final ICompoundContainer<?> wrapper, @NotNull final Set<CompoundInstance> compounds)
    {
        if (wrapper.getContentsCount() != 1)
            throw new IllegalArgumentException("Can not set locked information with none unit stack.");

        valueInformation.put(wrapper, ImmutableSet.copyOf(compounds));

        return this;
    }

    @Override
    public <T> ILockedCompoundInformationRegistry registerValue(
      @NotNull final T gameObjectInstanceToAssign, @NotNull final Set<CompoundInstance> compounds)
    {
        return registerValue(
          CompoundContainerFactoryManager.getInstance().wrapInContainer(gameObjectInstanceToAssign, 1d),
          compounds
        );
    }

    @Override
    public ILockedCompoundInformationRegistry registerLocking(
      @NotNull final ICompoundContainer<?> wrapper, @NotNull final Set<CompoundInstance> instances)
    {
        if (wrapper.getContentsCount() != 1)
            throw new IllegalArgumentException("Can not set locked information with none unit stack.");

        lockedInformation.put(wrapper, ImmutableSet.copyOf(instances));

        return this;
    }

    @Override
    public <T> ILockedCompoundInformationRegistry registerLocking(
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
