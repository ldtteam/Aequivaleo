package com.ldtteam.aequivaleo.api.gameobject.loottable;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * The registry which is used to register handlers which handle loottable based equivalencies.
 */
public interface ILootTableAnalyserRegistry
{
    /**
     * Gives access to the current instance of the analyzer registry.
     *
     * @return The analyzer registry.
     */
    static ILootTableAnalyserRegistry getInstance() {
        return IAequivaleoAPI.getInstance().getLootTableAnalyserRegistry();
    }

    /**
     * Allows for the registration for a loot table handler.
     * @param canHandlePredicate Invoked by the system to figure out if the given callback can handle the block.
     * @param handlerCallback The callback invoked by the system to get the loot drops of a given blockstate in a given world.
     * @return The registry.
     */
    @NotNull
    ILootTableAnalyserRegistry register(
      @NotNull Predicate<Block> canHandlePredicate,
      @NotNull BiFunction<BlockState, ServerWorld, Set<ICompoundContainer<?>>> handlerCallback
    );
}
