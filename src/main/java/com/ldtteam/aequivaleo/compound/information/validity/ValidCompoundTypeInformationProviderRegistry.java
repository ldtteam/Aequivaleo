package com.ldtteam.aequivaleo.compound.information.validity;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.ICompoundInstance;
import com.ldtteam.aequivaleo.api.compound.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.information.validity.IValidCompoundTypeInformationProvider;
import com.ldtteam.aequivaleo.api.compound.information.validity.IValidCompoundTypeInformationProviderRegistry;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.util.Suppression;
import com.ldtteam.aequivaleo.compound.information.locked.LockedCompoundInformationRegistry;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ValidCompoundTypeInformationProviderRegistry implements IValidCompoundTypeInformationProviderRegistry
{

    private static final Map<RegistryKey<World>, ValidCompoundTypeInformationProviderRegistry> INSTANCES = Maps.newConcurrentMap();

    public static ValidCompoundTypeInformationProviderRegistry getInstance(@NotNull final RegistryKey<World> worldKey)
    {
        return INSTANCES.computeIfAbsent(worldKey, ValidCompoundTypeInformationProviderRegistry::new);
    }

    private final Map<Class<?>, Set<IValidCompoundTypeInformationProvider<?>>> providers = Maps.newConcurrentMap();
    private final RegistryKey<World>                                           worldKey;

    private ValidCompoundTypeInformationProviderRegistry(final RegistryKey<World> worldKey)
    {
        this.worldKey = worldKey;
    }

    @Override
    public IValidCompoundTypeInformationProviderRegistry registerNewProvider(@NotNull final IValidCompoundTypeInformationProvider<?> provider)
    {
        providers.computeIfAbsent(provider.getWrappedContentType(), (type) -> Sets.newConcurrentHashSet()).add(provider);
        return this;
    }

    public void reset()
    {
        providers.clear();
    }

    @SuppressWarnings(Suppression.UNCHECKED)
    public <T> boolean isCompoundTypeValidForWrapper(
      @NotNull final ICompoundContainer<T> wrapper,
      @NotNull final ICompoundType type
    )
    {
        final Set<ICompoundInstance> lockedInformation = LockedCompoundInformationRegistry.getInstance(worldKey).get().get(wrapper);
        if (lockedInformation != null)
        {
            return lockedInformation.stream().anyMatch(compoundInstance -> compoundInstance.getType().equals(type));
        }

        if (!providers.containsKey(wrapper.getContents().getClass()))
        {
            return true;
        }

        return providers
                 .get(wrapper.getContents().getClass())
                 .stream()
                 .map(provider -> (IValidCompoundTypeInformationProvider<T>) provider)
                 .map(provider -> provider.canWrapperHaveCompound(wrapper, type))
                 .filter(Optional::isPresent)
                 .findFirst()
                 .orElse(Optional.of(true))
                 .orElse(true);
    }
}
