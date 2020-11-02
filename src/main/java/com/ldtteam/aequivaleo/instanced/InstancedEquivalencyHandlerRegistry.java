package com.ldtteam.aequivaleo.instanced;

import com.google.common.collect.Maps;
import com.ldtteam.aequivaleo.api.instanced.IInstancedEquivalencyHandlerRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

public class InstancedEquivalencyHandlerRegistry implements IInstancedEquivalencyHandlerRegistry
{
    private static final InstancedEquivalencyHandlerRegistry INSTANCE = new InstancedEquivalencyHandlerRegistry();

    public static InstancedEquivalencyHandlerRegistry getInstance()
    {
        return INSTANCE;
    }

    private final Map<Object, Consumer<Consumer<Object>>> handlers = Maps.newConcurrentMap();

    private InstancedEquivalencyHandlerRegistry()
    {
    }

    @Override
    public <T> IInstancedEquivalencyHandlerRegistry registerHandler(
      @NotNull final T source, final Consumer<Consumer<Object>> handler)
    {
        this.handlers.put(source, handler);
        return this;
    }

    public void process(final Object source, final Consumer<Object> resultsHandler, final Consumer<Consumer<Object>> alternativeExecutor) {
        final Consumer<Consumer<Object>> handler = this.handlers.getOrDefault(source, alternativeExecutor);
        handler.accept(resultsHandler);
    }

}
