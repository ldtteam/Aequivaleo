package com.ldtteam.aequivaleo.results;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.results.IResultsInformationCache;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import com.ldtteam.aequivaleo.network.messages.PartialSyncResultsMessage;
import com.ldtteam.aequivaleo.network.messages.SyncCompletedMessage;
import com.ldtteam.aequivaleo.network.splitting.NetworkSplittingManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ResultsInformationCache implements IResultsInformationCache
{

    private static final Logger LOGGER = LogManager.getLogger();


    private static final Map<RegistryKey<World>, ResultsInformationCache> WORLD_INSTANCES = Maps.newConcurrentMap();

    private        Map<ICompoundContainer<?>, Set<CompoundInstance>>      rawData         = Maps.newConcurrentMap();
    private        Table<ICompoundContainer<?>, ICompoundTypeGroup, Optional<?>> processedData = Tables.newCustomTable(
      new ConcurrentHashMap<>(),
      ConcurrentHashMap::new
    );

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

    @NotNull
    @Override
    public Set<CompoundInstance> getFor(@NotNull final ICompoundContainer<?> container){
        final ICompoundContainer<?> unitContainer = container.getContentsCount() == 1d ? container :
                                                                                                     IAequivaleoAPI.Holder.getInstance().getCompoundContainerFactoryManager().wrapInContainer(container.getContents(), 1d);

        if (!rawData.containsKey(unitContainer)) {
            final Set<?> alternatives = ResultsAdapterHandlerRegistry.getInstance().produceAlternatives(container.getContents());
            for (final Object alternative : alternatives)
            {
                final Set<CompoundInstance> result = this.getFor(alternative);
                if (!result.isEmpty())
                {
                    return result;
                }
            }

            return Collections.emptySet();
        }

        return rawData.get(unitContainer);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <R> Optional<R> getCacheFor(@NotNull final ICompoundTypeGroup group, @NotNull final ICompoundContainer<?> container)
    {
        final ICompoundContainer<?> unitContainer = container.getContentsCount() == 1d ? container :
                                                                                                     IAequivaleoAPI.Holder.getInstance().getCompoundContainerFactoryManager().wrapInContainer(container.getContents(), 1d);

        if (!processedData.contains(unitContainer, group)) {
            final Set<?> alternatives = ResultsAdapterHandlerRegistry.getInstance().produceAlternatives(container.getContents());
            for (final Object alternative : alternatives)
            {
                final Optional<R> result = this.getCacheFor(group, alternative);
                if (result.isPresent())
                {
                    return result;
                }
            }

            return Optional.empty();
        }

        final Optional<?> storedEntry = processedData.get(unitContainer, group);
        try {
            return (Optional<R>) storedEntry;
        }
        catch (ClassCastException classCastException) {
            return Optional.empty();
        }
    }

    public void set(@NotNull final Map<ICompoundContainer<?>, Set<CompoundInstance>> data)
    {
        this.rawData = new ConcurrentHashMap<>(data);
        this.processedData.clear();
        this.rawData.entrySet().parallelStream().forEach(e -> {
            final Map<ICompoundTypeGroup, Collection<CompoundInstance>> instancesGroupedByGroup =
              GroupingUtils.groupByUsingSetToMap(e.getValue(), i -> i.getType().getGroup());

            instancesGroupedByGroup.forEach((group, instances) -> processedData.put(
              e.getKey(),
              group,
              group.convertToCacheEntry(new HashSet<>(instances))
            ));
        });
    }

    public static void updateAllPlayers() {
        WORLD_INSTANCES.forEach((key, data) -> NetworkSplittingManager.getInstance().sendSplit(
          Lists.newArrayList(data.rawData.entrySet()),
          PartialSyncResultsMessage::new,
          integer -> new SyncCompletedMessage(integer, key.getLocation()),
          message -> Aequivaleo.getInstance().getNetworkChannel().sendToEveryone(message)
        ));
    }

    public static void updatePlayer(@NotNull final ServerPlayerEntity player) {
        WORLD_INSTANCES.forEach((key, data) -> NetworkSplittingManager.getInstance().sendSplit(
          Lists.newArrayList(data.rawData.entrySet()),
          PartialSyncResultsMessage::new,
          integer -> new SyncCompletedMessage(integer, key.getLocation()),
          message -> Aequivaleo.getInstance().getNetworkChannel().sendToPlayer(message, player)
        ));
    }
}
