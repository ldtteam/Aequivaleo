package com.ldtteam.aequivaleo.api.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;

/**
 * Represents an equivalency recipe that represents the drops of a game object that has a loot table.
 */
public interface ILootTableEquivalencyRecipe extends IEquivalencyRecipe
{
    /**
     * The source of the loot table.
     *
     * @return The source.
     */
    ICompoundContainer<?> getLootTableSource();

    /**
     * Indicates if this is the drops of the source or if this is required to make the source.
     * When: {@code true} is returned, then the output is considered to be the result of the destroying of the {@link #getLootTableSource()}.
     * When: {@code false} is returned, then the output is considered to be the result of the using of the inputs to create {@link #getLootTableSource()} in the world.
     *
     * @return True when the source is the input, false when not.
     */
    boolean isInput();
}
