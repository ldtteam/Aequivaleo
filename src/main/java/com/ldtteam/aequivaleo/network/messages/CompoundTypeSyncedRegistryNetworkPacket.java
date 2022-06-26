package com.ldtteam.aequivaleo.network.messages;

import com.google.common.collect.Lists;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.handler.codec.EncoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

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
        final Codec<List<ICompoundType>> registryCodec = ModRegistries.COMPOUND_TYPE.get().getCodec();

        DataResult<Tag> dataresult = registryCodec.encodeStart(NbtOps.INSTANCE, ModRegistries.COMPOUND_TYPE.get().getSyncableEntries());
        dataresult.error().ifPresent((p_178349_) -> {
            throw new EncoderException("Failed to encode: " + p_178349_.message() + " " + ModRegistries.COMPOUND_TYPE.get().getSyncableEntries());
        });

        final CompoundTag dataCarrier = new CompoundTag();
        dataCarrier.put("data", dataresult.result().get());

        buf.writeNbt(dataCarrier);
    }

    public void fromBytes(final FriendlyByteBuf buf)
    {
        final Codec<List<ICompoundType>> registryCodec = ModRegistries.COMPOUND_TYPE.get().getCodec();
        final CompoundTag dataCarrier = buf.readAnySizeNbt();
        final ListTag listTag = Objects.requireNonNull(dataCarrier).getList("data", 10);


        DataResult<List<ICompoundType>> dataresult = registryCodec.parse(NbtOps.INSTANCE, listTag);
        dataresult.error().ifPresent((p_178380_) -> {
            throw new EncoderException("Failed to decode: " + p_178380_.message() + " " + listTag);
        });

        payLoad = dataresult.result().get();
    }

    @Override
    public @Nullable LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        ModRegistries.COMPOUND_TYPE.get().forceLoad(payLoad);

        PluginManger.getInstance().getPlugins().forEach(
                IAequivaleoPlugin::onCompoundTypeRegistrySync
        );
    }
}
