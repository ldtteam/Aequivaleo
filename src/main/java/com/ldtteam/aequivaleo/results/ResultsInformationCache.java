package com.ldtteam.aequivaleo.results;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.results.IResultsInformationCache;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.network.messages.IMessage;
import com.ldtteam.aequivaleo.network.messages.PartialSyncResultsMessage;
import com.ldtteam.aequivaleo.network.messages.SyncCompletedMessage;
import com.ldtteam.aequivaleo.network.splitting.NetworkSplittingManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ResultsInformationCache implements IResultsInformationCache
{

    private static final Logger LOGGER = LogManager.getLogger();

    private final        Map<ICompoundContainer<?>, Set<CompoundInstance>> cacheData       = Maps.newConcurrentMap();
    private static final Map<RegistryKey<World>, ResultsInformationCache>  WORLD_INSTANCES = Maps.newConcurrentMap();

    private ResultsInformationCache()
    {

    }

    public static ResultsInformationCache getInstance(@NotNull final RegistryKey<World> world)
    {
        return WORLD_INSTANCES.computeIfAbsent(world, (dimType) -> new ResultsInformationCache());
    }


    @SubscribeEvent
    public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent playerLoggedInEvent)
    {
        if (playerLoggedInEvent.getPlayer() instanceof ServerPlayerEntity) {
            LOGGER.info("Sending results data to player: " + playerLoggedInEvent.getPlayer().getScoreboardName());
            ResultsInformationCache.updatePlayer((ServerPlayerEntity) playerLoggedInEvent.getPlayer());
        }
    }

    @Override
    public Map<ICompoundContainer<?>, Set<CompoundInstance>> getAll()
    {
        return cacheData;
    }

    public void set(@NotNull final Map<ICompoundContainer<?>, Set<CompoundInstance>> data)
    {
        cacheData.clear();
        cacheData.putAll(data);
    }

    public static void updateAllPlayers() {
        WORLD_INSTANCES.forEach((key, data) -> {
            NetworkSplittingManager.getInstance().sendSplit(
              Lists.newArrayList(data.cacheData.entrySet()),
              PartialSyncResultsMessage::new,
              integer -> new SyncCompletedMessage(integer, key.func_240901_a_()),
              message -> Aequivaleo.getInstance().getNetworkChannel().sendToEveryone(message)
            );
        });
    }

    public static void updatePlayer(@NotNull final ServerPlayerEntity player) {
        WORLD_INSTANCES.forEach((key, data) -> {
            NetworkSplittingManager.getInstance().sendSplit(
              Lists.newArrayList(data.cacheData.entrySet()),
              PartialSyncResultsMessage::new,
              integer -> new SyncCompletedMessage(integer, key.func_240901_a_()),
              message -> Aequivaleo.getInstance().getNetworkChannel().sendToPlayer(message, player)
            );
        });
    }
}
