package com.ldtteam.aequivaleo.api.compound.container.factory;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Represents an object that converts a certain game object into a wrapped counterpart which can
 * then carry compound information.
 */
public interface ICompoundContainerType<T>
{

    /**
     * A predicate that indicates to the system what kind of objects this factory can wrap.
     * @return A testable predicate to verify if a given factory can wrap a given object.
     */
    @NotNull
    default Predicate<Object> getCanHandlePredicate() {
        return getContainedType()::isInstance;
    }

    /**
     * The contained type, used in {@link #getCanHandlePredicate()} to create an instance of check.
     * Feel free to return {@link Object#getClass()} when overriding {@link #getCanHandlePredicate()}.
     *
     * @return The base class contained in the container this factory makes.
     */
    @NotNull
    Class<T> getContainedType();

    /**
     * Method used to wrap an instance of the input type and a given count of that instance into a given container.
     * The instance should be normalized if needed by this factory to ensure that the game object contained in the given
     * container is of unit size.
     *
     * @param inputInstance The instance that needs to be put into the container.
     * @param count The count to contain in the container.
     * @return A compound container that will contain the given instance of the game object in unit size with the given count.
     */
    @NotNull
    ICompoundContainer<T> create(@NotNull final T inputInstance, final double count);

    /**
     * Method used to get the innate count of an input instance that can be wrapped in a container by this factory.
     *
     * @param inputInstance The input instance to get the innate count from.
     * @return The innate count within the instance.
     */
    default double getInnateCount(@NotNull final T inputInstance) {
        return 1d;
    }
    
    /**
     * Method used to get the codec for the containers represented by this type.
     *
     * @return The codec for the containers represented by this type.
     */
    Codec<? extends ICompoundContainer<T>> codec();
}
