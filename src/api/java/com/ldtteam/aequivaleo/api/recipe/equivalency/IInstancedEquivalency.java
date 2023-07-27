package com.ldtteam.aequivaleo.api.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;

/**
 * Represents an equivalency recipe that links exactly two instances of the same object together.
 * For example an item with an itemstack.
 */
public interface IInstancedEquivalency extends IEquivalencyRecipe
{
    /**
     * The source of the equivalency.
     *
     * @return The source.
     */
    ICompoundContainer<?> getSource();

    /**
     * The target of the equivalency.
     *
     * @return The target.
     */
    ICompoundContainer<?> getTarget();
}
