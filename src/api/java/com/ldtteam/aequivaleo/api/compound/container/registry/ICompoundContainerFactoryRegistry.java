package com.ldtteam.aequivaleo.api.compound.container.registry;

import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import org.jetbrains.annotations.NotNull;

/**
 * A Registry type for factories that handle wrapping compound containers, like ItemStacks.
 * Making this a registry instead of hardcoding allows for easier expansion of the compound system into new and several types, like entities and power.
 */
public interface ICompoundContainerFactoryRegistry
{
    /**
     * Registers a new container factory to this registry.
     *
     * @param factory The compound container factory to register.
     * @param <T> The type of game object that is consumed by the factory.
     * @param <R> The type of compound container (ItemStack, FluidStack, IBlockState etc).
     * @return The registry with the factory added.
     */
    <T, R> ICompoundContainerFactoryRegistry register(@NotNull ICompoundContainerFactory<T, R> factory);
}