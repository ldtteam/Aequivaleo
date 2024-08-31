package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A marker interface for nodes which represent a recipe.
 * Recipes are always free nodes, 
 */
public interface IRecipeNode extends ICoreNode, IFreeNode
{
    /**
     * Gives access to the recipe that this node represents.
     *
     * @return The recipe.
     */
    @NotNull
    IEquivalencyRecipe recipe();

    /**
     * Gets the inputs of this node.
     *
     * @return The inputs.
     */
    @Override
    Collection<? extends IResultsOwningNode> inputs();
}
