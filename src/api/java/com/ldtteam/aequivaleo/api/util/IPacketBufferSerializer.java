package com.ldtteam.aequivaleo.api.util;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

/**
 * Represents an object that serialize the {@code T} into a PacketBuffer.
 * @param <T> The type to handle.
 */
public interface IPacketBufferSerializer<T>
{
    /**
     * Writes the object into the buffer.
     *
     * @param object The object to write.
     * @param buffer The buffer.
     */
    void write(final T object, final PacketBuffer buffer) throws IOException;

    /**
     * Reads the object from the buffer.
     *
     * @param buffer The buffer to read from.
     * @return The object.
     */
    T read(final PacketBuffer buffer) throws IOException;
}