package com.ldtteam.aequivaleo.api.compound.container.factory;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.util.IPacketBufferSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Represents a object that converts a certain game object into a wrapped counter part which can
 * then carry compound information.
 */
public interface ICompoundContainerFactory<T> extends JsonSerializer<ICompoundContainer<T>>,
                                                           JsonDeserializer<ICompoundContainer<T>>,
                                                           IPacketBufferSerializer<ICompoundContainer<T>>
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

}
