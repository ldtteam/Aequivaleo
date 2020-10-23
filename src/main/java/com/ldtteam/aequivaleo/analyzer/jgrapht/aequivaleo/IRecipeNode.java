package com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import org.jetbrains.annotations.NotNull;

/**
 * A marker interface for nodes which represent a recipe.
 */
public interface IRecipeNode extends INode
{

    /**
     * Gives access to the recipe that this node represents.
     *
     * @return The recipe.
     */
    @NotNull
    IEquivalencyRecipe getRecipe();
}
