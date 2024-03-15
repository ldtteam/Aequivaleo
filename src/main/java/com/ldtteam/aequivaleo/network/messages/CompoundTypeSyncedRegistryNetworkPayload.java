package com.ldtteam.aequivaleo.network.messages;

import com.google.common.collect.BiMap;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public record CompoundTypeSyncedRegistryNetworkPayload(BiMap<ResourceLocation, ICompoundType> payLoad) implements CustomPacketPayload
{
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "compound_types");

    private static final Logger LOGGER = LogUtils.getLogger();

    public CompoundTypeSyncedRegistryNetworkPayload(final FriendlyByteBuf friendlyByteBuf)
    {
        this(friendlyByteBuf.readJsonWithCodec(ModRegistries.COMPOUND_TYPE.getCodec()));
    }

    public void onExecute(final ConfigurationPayloadContext context)
    {
        context.workHandler().submitAsync(() -> {
            ModRegistries.COMPOUND_TYPE.forceLoad(payLoad);

            PluginManger.getInstance().getPlugins().forEach(
                    IAequivaleoPlugin::onCompoundTypeRegistrySync
            );
        })
        .exceptionally(e -> {
            LOGGER.error("Failed to process compound types", e);
            context.packetHandler().disconnect(Component.translatable(
                "aequivaleo.network.failure.compound_types",
                    e.getMessage()
            ));
            return null;
        });
    }

    @Override
    public void write(@NotNull FriendlyByteBuf friendlyByteBuf) {
        final Codec<BiMap<ResourceLocation, ICompoundType>> registryCodec = ModRegistries.COMPOUND_TYPE.getCodec();
        friendlyByteBuf.writeJsonWithCodec(registryCodec, payLoad);
    }

    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}
