package com.ldtteam.aequivaleo.network.messages;

import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CompoundTypeSyncedRegistryNetworkPacket implements IMessage
{

    private List<ICompoundType> payLoad = Lists.newArrayList();

    public CompoundTypeSyncedRegistryNetworkPacket()
    {
    }

    public CompoundTypeSyncedRegistryNetworkPacket(final FriendlyByteBuf friendlyByteBuf)
    {
        fromBytes(friendlyByteBuf);
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        final Codec<List<ICompoundType>> registryCodec = ModRegistries.COMPOUND_TYPE.getCodec();
        buf.writeWithCodec(registryCodec, ModRegistries.COMPOUND_TYPE.getSyncableEntries());
    }

    public void fromBytes(final FriendlyByteBuf buf)
    {
        final Codec<List<ICompoundType>> registryCodec = ModRegistries.COMPOUND_TYPE.getCodec();
        payLoad = buf.readWithCodec(registryCodec);
    }

    @Override
    public @Nullable LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        ModRegistries.COMPOUND_TYPE.forceLoad(payLoad);

        PluginManger.getInstance().getPlugins().forEach(
          plugin -> plugin.onCompoundTypeRegistrySync()
        );
    }
}
