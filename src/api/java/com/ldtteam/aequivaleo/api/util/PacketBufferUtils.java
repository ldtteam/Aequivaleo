package com.ldtteam.aequivaleo.api.util;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.ICompoundType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ForgeRegistry;
import org.jetbrains.annotations.NotNull;

public class PacketBufferUtils
{

    private PacketBufferUtils()
    {
        throw new IllegalStateException("Tried to initialize: PacketBufferUtils but this is a Utility class.");
    }

    public static void writeCompoundInstance(@NotNull final CompoundInstance instance, @NotNull final PacketBuffer buffer) {
        final ForgeRegistry<ICompoundType> registry = RegistryUtils.getFull(ICompoundType.class);
        buffer.writeVarInt(registry.getID(instance.getType()));
        buffer.writeDouble(instance.getAmount());
    }

    public static CompoundInstance readCompoundInstance(@NotNull final PacketBuffer buffer) {
        final ForgeRegistry<ICompoundType> registry = RegistryUtils.getFull(ICompoundType.class);
        return new CompoundInstance(
          registry.getValue(buffer.readVarInt()),
          buffer.readDouble()
        );
    }
}
