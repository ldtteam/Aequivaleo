package com.ldtteam.aequivaleo.api.util;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class PacketBufferUtils
{

    private PacketBufferUtils()
    {
        throw new IllegalStateException("Tried to initialize: PacketBufferUtils but this is a Utility class.");
    }

    public static void writeCompoundInstance(@NotNull final CompoundInstance instance, @NotNull final FriendlyByteBuf buffer) {
        buffer.writeId(ModRegistries.COMPOUND_TYPE, instance.getType());
        buffer.writeDouble(instance.getAmount());
    }

    public static CompoundInstance readCompoundInstance(@NotNull final FriendlyByteBuf buffer) {
        return new CompoundInstance(
          buffer.readById(ModRegistries.COMPOUND_TYPE),
          buffer.readDouble()
        );
    }
}
