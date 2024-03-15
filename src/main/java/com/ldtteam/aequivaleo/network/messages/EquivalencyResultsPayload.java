package com.ldtteam.aequivaleo.network.messages;

import com.google.common.collect.BiMap;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.PacketBufferUtils;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.ldtteam.aequivaleo.results.EquivalencyResults;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public record EquivalencyResultsPayload(ResourceLocation level, Map<ICompoundContainer<?>, Set<CompoundInstance>> results) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "equivalency_results");

    private static final Logger LOGGER = LogUtils.getLogger();

    public EquivalencyResultsPayload(FriendlyByteBuf friendlyByteBuf) {
        this(
                friendlyByteBuf.readResourceLocation(),
                friendlyByteBuf.readMap(
                        buf -> CompoundContainerFactoryManager.getInstance().read(buf),
                        buf -> buf.readCollection(
                                HashSet::new,
                                PacketBufferUtils::readCompoundInstance
                        )
                )
        );
    }

    public void onExecute(IPayloadContext iPayloadContext) {
        final ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, level());

        iPayloadContext.workHandler().submitAsync(() -> {
            EquivalencyResults.getInstance(levelKey)
                    .set(results());

            PluginManger.getInstance().run(iAequivaleoPlugin -> iAequivaleoPlugin.onDataSynced(levelKey));
        })
        .exceptionally(e -> {
            LOGGER.error("Failed to synchronize equivalency data for level: %s".formatted(level()), e);
            iPayloadContext.packetHandler().disconnect(
                    Component.translatable(
                            "aequivaleo.network.failure.level_sync",
                            level(),
                            e.getMessage()
                    )
            );
            return null;
        });
    }

    @Override
    public void write(@NotNull FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(level());
        friendlyByteBuf.writeMap(
                results(),
                (buf, container) -> CompoundContainerFactoryManager.getInstance().write(container, buf),
                (buf, instances) -> buf.writeObjectCollection(instances, PacketBufferUtils::writeCompoundInstance)
        );
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
