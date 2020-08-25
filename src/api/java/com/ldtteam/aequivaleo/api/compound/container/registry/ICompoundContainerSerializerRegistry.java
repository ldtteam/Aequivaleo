package com.ldtteam.aequivaleo.api.compound.container.registry;

import com.ldtteam.aequivaleo.api.compound.container.serialization.ICompoundContainerSerializer;
import org.jetbrains.annotations.NotNull;

public interface ICompoundContainerSerializerRegistry
{

    /**
     * Registers a serializer to the registry.
     * For each compound wrapper type a serializer needs to exists.
     *
     * @param serializer The serializer to register.
     * @param <T> The type contained in a compound wrapper that the given serializer can read and write to disk.
     * @return The registry with the serializer added.
     */
    <T> ICompoundContainerSerializerRegistry register(@NotNull final ICompoundContainerSerializer<T> serializer);
}
