package com.ldtteam.aequivaleo.gameobject.loottable;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.gameobject.loottable.ILootTableAnalyserRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class LootTableAnalyserRegistry implements ILootTableAnalyserRegistry
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final LootTableAnalyserRegistry INSTANCE = new LootTableAnalyserRegistry();

    public static LootTableAnalyserRegistry getInstance()
    {
        return INSTANCE;
    }

    private final LinkedList<LootTableAnalyserEntry> handlers = new LinkedList<>();

    private LootTableAnalyserRegistry()
    {
    }

    @NotNull
    @Override
    public ILootTableAnalyserRegistry register(
      @NotNull final Predicate<Block> canHandlePredicate, @NotNull final BiFunction<BlockState, ServerWorld, Set<ICompoundContainer<?>>> handlerCallback)
    {
        handlers.add(new LootTableAnalyserEntry(canHandlePredicate, handlerCallback));
        return this;
    }

    @NotNull
    public <T> Set<ICompoundContainer<?>> calculateOutputs(@NotNull final BlockState input, @NotNull final ServerWorld world)
    {
        for (Iterator<LootTableAnalyserEntry> iterator = handlers.descendingIterator(); iterator.hasNext(); )
        {
            final LootTableAnalyserEntry e = iterator.next();
            if (e.getCanHandlePredicate().test(input.getBlock()))
            {
                return e.getLootTableHandler().apply(input, world);
            }
        }
        return Sets.newHashSet();
    }

    private static final class LootTableAnalyserEntry
    {
        @NotNull
        private final Predicate<Block>                                                    canHandlePredicate;
        @NotNull
        private final BiFunction<BlockState, ServerWorld, Set<ICompoundContainer<?>>> lootTableHandler;

        private LootTableAnalyserEntry(
          @NotNull final Predicate<Block> canHandlePredicate,
          @NotNull final BiFunction<BlockState, ServerWorld, Set<ICompoundContainer<?>>> lootTableHandler) {
            this.canHandlePredicate = canHandlePredicate;
            this.lootTableHandler = lootTableHandler;
        }

        @NotNull
        public Predicate<Block> getCanHandlePredicate()
        {
            return canHandlePredicate;
        }

        @NotNull
        public BiFunction<BlockState, ServerWorld, Set<ICompoundContainer<?>>> getLootTableHandler()
        {
            return lootTableHandler;
        }
    }
}
