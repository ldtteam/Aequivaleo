package com.ldtteam.aequivaleo.api.instanced;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a registry that is responsible for handling instanced equivalencies
 * when it comes to determine what two instances are equivalent to one another.
 */
public interface IInstancedEquivalencyHandlerRegistry
{

    static IInstancedEquivalencyHandlerRegistry getInstance() {
        return IAequivaleoAPI.getInstance().getInstancedEquivalencyHandlerRegistry();
    }

    /**
     * Endpoint to add an instanced equivalency handler.
     * The handler is invoked when the instanced equivalency for the source object is determined.
     *
     * @param source The source object.
     * @param handler The target handler. Gets a callback to invoke to add an instanced equivalency between the source and the object passed to the callback.
     * @param <T> The type of the source object.
     *
     * @return The registry.
     */
    <T> IInstancedEquivalencyHandlerRegistry registerHandler(@NotNull final T source, final Consumer<Consumer<Object>> handler);

}
