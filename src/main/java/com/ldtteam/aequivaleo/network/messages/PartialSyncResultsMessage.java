package com.ldtteam.aequivaleo.network.messages;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.network.splitting.NetworkSplittingManager;
import com.ldtteam.aequivaleo.utils.IOUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PartialSyncResultsMessage implements IMessage
{
    private int communicationId = -1;
    private List<Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>>> compoundData = new ArrayList<>();

    public PartialSyncResultsMessage(
      @NotNull final FriendlyByteBuf buffer
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
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeVarInt(this.communicationId);
        IOUtils.writeCompoundDataEntries(buf, compoundData);
    }

    private void fromBytes(final FriendlyByteBuf buffer) {
        communicationId = buffer.readVarInt();
        IOUtils.readCompoundData(buffer, compoundData);
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
