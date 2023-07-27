package com.ldtteam.aequivaleo.utils;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.util.PacketBufferUtils;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IOUtils
{

    private IOUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: IOUtils. This is a utility class");
    }

    public static void writeCompoundDataEntries(final FriendlyByteBuf buf, final List<Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>>> compoundData)
    {
        buf.writeVarInt(compoundData.size());
        for (final Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>> entry : compoundData)
        {
            CompoundContainerFactoryManager.getInstance().write(entry.getKey(), buf);
            buf.writeVarInt(entry.getValue().size());
            for (final CompoundInstance compoundInstance : entry.getValue())
            {
                PacketBufferUtils.writeCompoundInstance(compoundInstance, buf);
            }
        }
    }

    public static void readCompoundData(final FriendlyByteBuf buffer, final List<Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>>> compoundData)
    {
        final int containerCount = buffer.readVarInt();
        for (int i = 0; i < containerCount; i++)
        {
            final ICompoundContainer<?> container = CompoundContainerFactoryManager.getInstance().read(buffer);
            final int compoundCount = buffer.readVarInt();
            final Set<CompoundInstance> instances = Sets.newHashSet();
            for (int j = 0; j < compoundCount; j++)
            {
                instances.add(
                  PacketBufferUtils.readCompoundInstance(
                    buffer
                  )
                );
            }

            compoundData.add(new AbstractMap.SimpleEntry<>(container, instances));
        }
    }
}
