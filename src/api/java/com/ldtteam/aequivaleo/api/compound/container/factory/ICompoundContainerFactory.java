package com.ldtteam.aequivaleo.api.compound.container.factory;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a object that converts a certain game object into a wrapped counter part which can
 * then carry compound information.
 */
public interface ICompoundContainerFactory<T, R>
{

    /**
     * The class of the type that this factory can use as an input to produce a given compound container that contains the output type.
     *
     * @return The input type of the factory.
     */
    @NotNull
    Class<T> getInputType();

    /**
     * The class of the type that this factory produces from a given instance of the given input type.
     *
     * @return The output type of the factory.
     */
    @NotNull
    Class<R> getOutputType();

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
    ICompoundContainer<R> create(@NotNull final T inputInstance, final double count);

}
