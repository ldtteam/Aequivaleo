package com.ldtteam.aequivaleo.compound.information.locked;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.ldtteam.aequivaleo.api.compound.ICompoundInstance;
import com.ldtteam.aequivaleo.api.compound.information.locked.ILockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryRegistry;
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

    private final Map<ICompoundContainer<?>, ImmutableSet<ICompoundInstance>> lockedInformation = Maps.newConcurrentMap();

    @Override
    public ILockedCompoundInformationRegistry registerLocking(
      @NotNull final ICompoundContainer<?> wrapper, @NotNull final Set<ICompoundInstance> instances)
    {
        if (wrapper.getContentsCount() != 1)
            throw new IllegalArgumentException("Can not set locked information with none unit stack.");

        lockedInformation.put(wrapper, ImmutableSet.copyOf(instances));

        return this;
    }

    @Override
    public <T> ILockedCompoundInformationRegistry registerLocking(
      @NotNull final T tInstance, @NotNull final Set<ICompoundInstance> instances)
    {
        return registerLocking(
          CompoundContainerFactoryRegistry.getInstance().wrapInContainer(tInstance, 1d),
          instances
        );
    }

    public void reset()
    {
        lockedInformation.clear();
    }

    public ImmutableMap<ICompoundContainer<?>, ImmutableSet<ICompoundInstance>> get()
    {
        return ImmutableMap.copyOf(lockedInformation);
    }
}
