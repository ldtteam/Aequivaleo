package com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.analyzer.jgrapht.node.RecipeNode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;

import java.util.Set;

/**
 * Marker interface to recognize all input related nodes for a recipe.
 */
public interface IRecipeInputNode extends INode
{
    @Override
    default IRecipeInputNode getSelf()
    {
        return this;
    }

    /**
     * Returns the compound instances which contribute as input to a given recipe.
     *
     * @param recipeNode The node of the recipe in question.
     *
     * @return The compound instances with which this node contributes to the recipe as an input.
     */
    Set<CompoundInstance> getInputInstances(final IRecipeNode recipeNode);

    /**
     * Returns the nodes which are the input for a given recipe node.
     *
     * @param recipeNode The node of the recipe in question.
     *
     * @return The nodes which function as an input for the given recipe node.
     */
    default Set<IRecipeInputNode> getInputNodes(final IRecipeNode recipeNode) {
        return Sets.newHashSet(getSelf());
    }
}
