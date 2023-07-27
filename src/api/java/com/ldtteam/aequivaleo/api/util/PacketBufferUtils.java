package com.ldtteam.aequivaleo.api.util;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistry;
import org.jetbrains.annotations.NotNull;

public class PacketBufferUtils
{

    private PacketBufferUtils()
    {
        throw new IllegalStateException("Tried to initialize: PacketBufferUtils but this is a Utility class.");
    }

    public static void writeCompoundInstance(@NotNull final CompoundInstance instance, @NotNull final FriendlyByteBuf buffer) {
        buffer.writeVarInt(ModRegistries.COMPOUND_TYPE.get().getSynchronizationIdOf(instance.getType()));
        buffer.writeDouble(instance.getAmount());
    }

    public static CompoundInstance readCompoundInstance(@NotNull final FriendlyByteBuf buffer) {
        final ForgeRegistry<ICompoundType> registry = RegistryUtils.getFull(ModRegistries.COMPOUND_TYPE.get().getBackingRegistryKey());
        return new CompoundInstance(
          registry.getValue(buffer.readVarInt()),
          buffer.readDouble()
        );
    }
}
