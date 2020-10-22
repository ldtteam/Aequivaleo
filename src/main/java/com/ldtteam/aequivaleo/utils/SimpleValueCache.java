package com.ldtteam.aequivaleo.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class SimpleValueCache<V>
{
    @NotNull
    private final Supplier<V> initializer;

    private boolean initialized = false;

    @Nullable
    private V           value;

    public SimpleValueCache()
    {
        this(() -> null);
    }

    public SimpleValueCache(final V value)
    {
        this(() -> value);
    }

    public SimpleValueCache(@NotNull final Supplier<V> initializer) {
        this.initializer = initializer;
    }

    public void clear() {
        this.initialized = false;
    }

    public void set(V value) {
        this.initialized = true;
        this.value = value;
    }

    public V get() {
        if (!this.initialized) {
            this.value = this.initializer.get();
            this.initialized = true;
        }

        return value;
    }
}
