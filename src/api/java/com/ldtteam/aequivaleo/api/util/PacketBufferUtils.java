package com.ldtteam.aequivaleo.api.util;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for PacketBuffer.
 */
public class PacketBufferUtils
{

    private PacketBufferUtils()
    {
        throw new IllegalStateException("Tried to initialize: PacketBufferUtils but this is a Utility class.");
    }

    /**
     * Writes a compound instance to a buffer.
     *
     * @param instance The instance to write.
     * @param buffer   The buffer to write to.
     */
    public static void writeCompoundInstance(@NotNull final CompoundInstance instance, @NotNull final FriendlyByteBuf buffer) {
        buffer.writeId(ModRegistries.COMPOUND_TYPE, instance.getType());
        buffer.writeDouble(instance.getAmount());
    }

    /**
     * Reads a compound instance from a buffer.
     *
     * @param buffer The buffer to read from.
     * @return The read instance.
     */
    public static CompoundInstance readCompoundInstance(@NotNull final FriendlyByteBuf buffer) {
        return new CompoundInstance(
          buffer.readById(ModRegistries.COMPOUND_TYPE),
          buffer.readDouble()
        );
    }
}
