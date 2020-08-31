package com.ldtteam.aequivaleo.network.messages;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.util.PacketBufferUtils;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.network.splitting.NetworkSplittingManager;
import com.ldtteam.aequivaleo.results.ResultsInformationCache;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PartialSyncResultsMessage implements IMessage
{
    private int communicationId = -1;
    private List<Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>>> compoundData = new ArrayList<>();

    public PartialSyncResultsMessage(
      @NotNull final PacketBuffer buffer
    ) {
        this.fromBytes(buffer);
    }

    public PartialSyncResultsMessage(
      final int communicationId,
      final List<Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>>> compoundData)
    {
        this.communicationId = communicationId;
        this.compoundData = compoundData;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeVarInt(this.communicationId);
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

    private void fromBytes(final PacketBuffer buffer) {
        communicationId = buffer.readVarInt();
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

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        NetworkSplittingManager.getInstance().receivedPartialMessage(
          this.communicationId,
          this
        );
    }

    public List<Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>>> getCompoundData()
    {
        return compoundData;
    }
}
