package com.ldtteam.aequivaleo.api.results;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This registry allows for the registration of adapter handlers.
 * These handlers are invoked by a {@link IResultsInformationCache} when no value is found
 * and are allowed to return one or more different objects to which they consider themselves
 * to be identical, which are then in turn used to lookup the requested value.
 */
public interface IResultsAdapterHandlerRegistry
{

    /**
     * The instance of the registry.
     * Identical to {@link IAequivaleoAPI#getResultsAdapterHandlerRegistry()}
     *
     * @return The registry.
     */
    static IResultsAdapterHandlerRegistry getInstance() {
        return IAequivaleoAPI.getInstance().getResultsAdapterHandlerRegistry();
    }

    /**
     * Method used to register a handler which looks up alternatives for a given object if the objects
     * results are not found in the results cache.
     *
     * @param canHandlePredicate Predicate asked to check if the given handler can handle the object.
     * @param alternativesProducer Producer which handles the calculation of alternatives from the given source object.
     * @param <T> The type of the source object. Has to be accurate and can not be a super type of an object.
     *
     * @return The registry.
     */
    <T> IResultsAdapterHandlerRegistry registerHandler(final Predicate<Object> canHandlePredicate, final Function<T, Set<?>> alternativesProducer);
}
