package com.ldtteam.aequivaleo.gameobject.loottable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.gameobject.loottable.ILootTableAnalyserRegistry;
import com.ldtteam.aequivaleo.api.util.TypeUtils;
import net.minecraft.block.BlockState;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;

public class LootTableAnalyserRegistry implements ILootTableAnalyserRegistry
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final LootTableAnalyserRegistry INSTANCE = new LootTableAnalyserRegistry();

    public static LootTableAnalyserRegistry getInstance()
    {
        return INSTANCE;
    }

    private final Set<LootTableAnalyserEntry<?>> handlers = Sets.newConcurrentHashSet();

    private LootTableAnalyserRegistry()
    {
    }

    @Override
    @NotNull
    public <T> ILootTableAnalyserRegistry register(
      @NotNull final Class<T> lootTableType,
      @NotNull final BiFunction<BlockState, ServerWorld, Set<ICompoundContainer<?>>> handlerCallback
    )
    {
        if(handlers.stream().anyMatch(l -> l.getInputType() == lootTableType))
        {
            LOGGER.info("Attempting to register duplicate loot table analyzer. Skipping");
            return this;
        }

        handlers.add(new LootTableAnalyserEntry<>(lootTableType, handlerCallback));
        return this;
    }

    @NotNull
    public <T> Set<ICompoundContainer<?>> calculateOutputs(@NotNull final BlockState input, @NotNull final ServerWorld world)
    {
        final Set<Class<?>> superTypes = TypeUtils.getAllSuperTypesExcludingObject(input.getBlock().getClass());
        final List<Class<?>> indexedSuperTypes = Lists.newLinkedList(superTypes);

        return handlers
                 .stream()
                 .filter(e -> superTypes.contains(e.getInputType()))
                 .sorted(Comparator.comparingInt(o -> indexedSuperTypes.indexOf(o.getInputType())))
                 .map(e -> (LootTableAnalyserEntry<? super T>) e)
                 .map(handler -> handler.calculateOutputs(input, world))
                 .findFirst()
                 .orElse(Sets.newHashSet());
    }

    private static final class LootTableAnalyserEntry<T>
    {
        @NotNull
        private final Class<T>                                                                inputType;
        @NotNull
        private final BiFunction<BlockState, ServerWorld, Set<ICompoundContainer<?>>> lootTableHandler;

        private LootTableAnalyserEntry(
          @NotNull final Class<T> inputType,
          @NotNull final BiFunction<BlockState, ServerWorld, Set<ICompoundContainer<?>>> lootTableHandler
        ) {
            this.inputType = inputType;
            this.lootTableHandler = lootTableHandler;
        }

        @NotNull
        public Class<T> getInputType()
        {
            return inputType;
        }

        @NotNull
        public Set<ICompoundContainer<?>> calculateOutputs(@NotNull final BlockState input, @NotNull final ServerWorld world)
        {
            return lootTableHandler.apply(input, world);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof LootTableAnalyserEntry))
            {
                return false;
            }

            final LootTableAnalyserEntry<?> that = (LootTableAnalyserEntry<?>) o;

            return getInputType().equals(that.getInputType());
        }

        @Override
        public int hashCode()
        {
            return getInputType().hashCode();
        }

        @Override
        public String toString()
        {
            return "LootTableAnalyserEntry{" +
                     "inputType=" + inputType +
                     '}';
        }
    }
}
