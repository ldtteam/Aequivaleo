package com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.analyzer.jgrapht.node.RecipeNode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;

import java.util.Set;

/**
 * Marker interface to recognize all input related nodes for a recipe.
 */
public interface IRecipeInputNode extends INode
{
    /**
     * Returns the compound instances which contribute as input to a given recipe.
     *
     * @param recipeNode The node of the recipe in question.
     *
     * @return The compound instances with which this node contributes to the recipe as an input.
     */
    Set<CompoundInstance> getInputInstances(final IRecipeNode recipeNode);
}
