package com.ldtteam.aequivaleo.network.messages;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

public class CompoundTypeSyncedRegistryNetworkPacket implements IMessage
{

    private BiMap<ResourceLocation, ICompoundType> payLoad = HashBiMap.create();

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
        final Codec<BiMap<ResourceLocation, ICompoundType>> registryCodec = ModRegistries.COMPOUND_TYPE.getCodec();
        buf.writeJsonWithCodec(registryCodec, payLoad);
    }

    public void fromBytes(final FriendlyByteBuf buf)
    {
        final Codec<BiMap<ResourceLocation, ICompoundType>> registryCodec = ModRegistries.COMPOUND_TYPE.getCodec();
        payLoad = buf.readJsonWithCodec(registryCodec);
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
                IAequivaleoPlugin::onCompoundTypeRegistrySync
        );
    }
}
