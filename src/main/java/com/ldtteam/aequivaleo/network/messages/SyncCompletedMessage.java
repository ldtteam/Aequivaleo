package com.ldtteam.aequivaleo.network.messages;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.network.splitting.NetworkSplittingManager;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.ldtteam.aequivaleo.results.ResultsInformationCache;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SyncCompletedMessage implements IMessage
{

    private int communicationId = -1;
    private ResourceLocation worldKeyName;

    public SyncCompletedMessage(@NotNull final PacketBuffer buffer)
    {
        this.fromBytes(buffer);
    }

    public SyncCompletedMessage(final int communicationId, final ResourceLocation worldKeyName)
    {
        this.communicationId = communicationId;
        this.worldKeyName = worldKeyName;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeVarInt(communicationId);
        buf.writeResourceLocation(worldKeyName);
    }

    public void fromBytes(final PacketBuffer buf)
    {
        communicationId = buf.readVarInt();
        worldKeyName = buf.readResourceLocation();
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
        final List<Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>>> partialPackets = NetworkSplittingManager.getInstance()
          .onMessageFinalized(
            communicationId,
            PartialSyncResultsMessage.class,
            PartialSyncResultsMessage::getCompoundData
          );

        ResultsInformationCache.getInstance(
          RegistryKey.func_240903_a_(Registry.WORLD_KEY, worldKeyName)
        ).set(partialPackets.stream().collect(
          Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue
          )
        ));

        PluginManger.getInstance().run(IAequivaleoPlugin::onDataSynced);
    }
}
