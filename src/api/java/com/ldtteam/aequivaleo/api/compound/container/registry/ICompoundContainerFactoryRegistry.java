package com.ldtteam.aequivaleo.api.compound.container.registry;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.Suppression;
import org.jetbrains.annotations.NotNull;

/**
 * A Registry type for factories that handle wrapping compound containers, like ItemStacks.
 * Making this a registry instead of hardcoding allows for easier expansion of the compound system into new and several types, like entities and power.
 */
public interface ICompoundContainerFactoryRegistry
{
    /**
     * Gives access to the current instance of the factory registry.
     *
     * @return The factory registry.
     */
    static ICompoundContainerFactoryRegistry getInstance() {
        return IAequivaleoAPI.getInstance().getCompoundContainerFactoryRegistry();
    }

    /**
     * Registers a new container factory to this registry.
     *
     * @param factory The compound container factory to register.
     * @param <T> The type of game object that is consumed by the factory.
     * @param <R> The type of compound container (ItemStack, FluidStack, IBlockState etc).
     * @return The registry with the factory added.
     */
    <T, R> ICompoundContainerFactoryRegistry register(@NotNull ICompoundContainerFactory<T, R> factory);

    /**
     * Utility method to check if a given class of a compound container can be wrapped properly.
     *
     * @param inputType The class to check
     * @param <T>   The type of the compound container to check.
     * @return True when a factory for type T is registered false when not.
     */
    <T> boolean canBeWrapped(@NotNull Class<T> inputType);

    /**
     * Utility method to check if a given instance of a possible compound container can be wrapped properly.
     *
     * @param gameObject The instance to check
     * @param <T>       The type of the compound container to check.
     * @return True when a factory for type T is registered false when not.
     */
    <T> boolean canBeWrapped(@NotNull T gameObject);

    /**
     * Utility method to check if a given class of a compound container can be wrapped properly.
     *
     * @param inputType The class to check
     * @param outputType The class of the output to check
     * @param <T>   The type of the compound container to check.
     * @param <R>   The type of the factory output to check.
     * @return True when a factory for type T is registered false when not.
     */
    <T, R> boolean canBeWrapped(@NotNull Class<T> inputType, @NotNull Class<R> outputType);

    /**
     * Utility method to check if a given instance of a possible compound container can be wrapped properly.
     *
     * @param gameObject The instance to check
     * @param outputType The class of the output to check
     * @param <T>   The type of the compound container to check.
     * @param <R>   The type of the factory output to check.
     * @return True when a factory for type T is registered false when not.
     */
    <T, R> boolean canBeWrapped(@NotNull T gameObject, @NotNull Class<R> outputType);

    /**
     * Wraps the given compound container.
     *
     * @param gameObject The instance of T to wrap. Will be brought to unit length by the factory (ItemStacks will be copied and have stack size 1).
     * @param count     The count to store in the container. For ItemStacks this will be the stacks size.
     * @param <T>       The type of the compound container to create.
     * @return The wrapped instance.
     * @throws IllegalArgumentException When T can not be wrapped properly {@code canBeWrapped(tInstance) == false;}
     */
    @NotNull
    <T> ICompoundContainer<?> wrapInContainer(@NotNull T gameObject, @NotNull double count) throws IllegalArgumentException;



    /**
     * Wraps the given compound container.
     *
     * @param gameObject The instance of T to wrap. Will be brought to unit length by the factory (ItemStacks will be copied and have stack size 1).
     * @param count     The count to store in the container. For ItemStacks this will be the stacks size.
     * @param <T>       The type of the compound container to create.
     * @return The wrapped instance.
     * @throws IllegalArgumentException When T can not be wrapped properly {@code canBeWrapped(tInstance) == false;}
     */
    @NotNull
    <T, R> ICompoundContainer<R> wrapInContainer(@NotNull T gameObject, @NotNull double count, @NotNull Class<R> outputType) throws IllegalArgumentException;
}