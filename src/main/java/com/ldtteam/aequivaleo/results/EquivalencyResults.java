package com.ldtteam.aequivaleo.results;

import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.results.IEquivalencyResults;
import com.ldtteam.aequivaleo.api.results.IResultsInformationCache;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import com.ldtteam.aequivaleo.api.util.StreamUtils;
import com.ldtteam.aequivaleo.network.messages.EquivalencyResultsPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings({"deprecation"})
@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class EquivalencyResults implements IResultsInformationCache, IEquivalencyResults {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<ResourceKey<Level>, EquivalencyResults> WORLD_INSTANCES = Maps.newConcurrentMap();

    private Map<ICompoundContainer<?>, Set<CompoundInstance>> rawData = Maps.newConcurrentMap();
    private final Table<ICompoundContainer<?>, ICompoundTypeGroup, Object> processedData = Tables.newCustomTable(
            new ConcurrentHashMap<>(),
            ConcurrentHashMap::new
    );
    private final Table<ICompoundTypeGroup, ICompoundContainer<?>, Set<CompoundInstance>> groupedInstances = Tables.newCustomTable(
            new ConcurrentHashMap<>(),
            ConcurrentHashMap::new
    );

    private EquivalencyResults() {

    }

    public static EquivalencyResults getInstance(@NotNull final ResourceKey<Level> world) {
        return WORLD_INSTANCES.computeIfAbsent(world, (dimType) -> new EquivalencyResults());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent playerLoggedInEvent) {
        if (playerLoggedInEvent.getEntity() instanceof ServerPlayer) {
            LOGGER.info("Sending results data to player: " + playerLoggedInEvent.getEntity().getScoreboardName());
            EquivalencyResults.updatePlayer((ServerPlayer) playerLoggedInEvent.getEntity());
        }
    }

    @NotNull
    @Override
    public Set<CompoundInstance> dataFor(@NotNull final ICompoundContainer<?> container) {
        final ICompoundContainer<?> unitContainer = container.contentsCount() == 1d ? container :
                IAequivaleoAPI.Holder.getInstance().getCompoundContainerFactoryManager().wrapInContainer(container.contents(), 1d);

        if (!rawData.containsKey(unitContainer)) {
            final Set<?> alternatives = ResultsAdapterHandlerRegistry.getInstance().produceAlternatives(container.contents());
            for (final Object alternative : alternatives) {
                final Set<CompoundInstance> result = this.dataFor(alternative);
                if (!result.isEmpty()) {
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
    public <R> Optional<R> mappedDataFor(@NotNull final ICompoundTypeGroup group, @NotNull final ICompoundContainer<?> container) {
        final ICompoundContainer<?> unitContainer = container.contentsCount() == 1d ? container :
                IAequivaleoAPI.Holder.getInstance().getCompoundContainerFactoryManager().wrapInContainer(container.contents(), 1d);

        if (!processedData.contains(unitContainer, group)) {
            final Set<?> alternatives = ResultsAdapterHandlerRegistry.getInstance().produceAlternatives(container.contents());
            for (final Object alternative : alternatives) {
                final Optional<R> result = this.mappedDataFor(group, alternative);
                if (result.isPresent()) {
                    return result;
                }
            }

            return Optional.empty();
        }

        final Object storedEntry = processedData.get(unitContainer, group);
        if (storedEntry == null)
            return Optional.empty();

        try {
            final R targetObject = (R) storedEntry;
            return Optional.of(targetObject);
        } catch (ClassCastException classCastException) {
            return Optional.empty();
        }
    }

    @Override
    public Map<ICompoundContainer<?>, Set<CompoundInstance>> getAllDataOf(final ICompoundTypeGroup group) {
        return Collections.unmodifiableMap(this.groupedInstances.row(group));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Map<ICompoundContainer<?>, R> getAllMappedDataOf(final ICompoundTypeGroup group) {
        final Map<ICompoundContainer<?>, Object> targetMap = this.processedData.column(group);

        final Map<ICompoundContainer<?>, R> resultsMap = targetMap
                .entrySet()
                .stream()
                .filter(e -> {
                    try {
                        final R resultObject = (R) e.getValue();
                        return resultObject != null;
                    } catch (ClassCastException ignore) {
                        return false;
                    } catch (Exception ex) {
                        LOGGER.error("Failed to check if a given cache object: " + e.getValue() + " is compatible.", ex);
                        return false;
                    }
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (R) e.getValue()
                ));

        return Collections.unmodifiableMap(resultsMap);
    }

    public void set(@NotNull final Map<ICompoundContainer<?>, Set<CompoundInstance>> data) {
        this.rawData = new ConcurrentHashMap<>(data);
        this.processedData.clear();
        this.groupedInstances.clear();

        StreamUtils.execute(
                () -> this.rawData.entrySet().parallelStream().forEach(e -> {
                    final Map<ICompoundTypeGroup, Collection<CompoundInstance>> instancesGroupedByGroup =
                            GroupingUtils.groupByUsingSetToMap(e.getValue(), i -> i.getType().getGroup());

                    instancesGroupedByGroup.forEach((group, instances) -> {
                        final Set<CompoundInstance> instanceSet = Set.copyOf(instances);

                        Object groupCacheObject = null;
                        try {
                            final Optional<?> optionalWithConvertedData = group.mapEntry(
                                    e.getKey(),
                                    instanceSet
                            );
                            if (optionalWithConvertedData.isPresent())
                                groupCacheObject = optionalWithConvertedData.get();
                        } catch (Exception ex) {
                            LOGGER.error("Failed to convert container instance data of: " + e.getKey() + " to cache data for group: " + group, ex);
                        }

                        if (groupCacheObject != null) {
                            processedData.put(
                                    e.getKey(),
                                    group,
                                    groupCacheObject
                            );
                        }

                        groupedInstances.put(
                                group,
                                e.getKey(),
                                instanceSet
                        );
                    });
                })
        );
    }

    public static void updateAllPlayers() {
        WORLD_INSTANCES.forEach((key, data) -> PacketDistributor.ALL
                .noArg()
                .send(new EquivalencyResultsPayload(key.location(), data.rawData)));
    }

    public static void updatePlayer(@NotNull final ServerPlayer player) {
        WORLD_INSTANCES.forEach((key, data) -> PacketDistributor.PLAYER.with(player)
                .send(new EquivalencyResultsPayload(key.location(), data.rawData)));
    }
}
