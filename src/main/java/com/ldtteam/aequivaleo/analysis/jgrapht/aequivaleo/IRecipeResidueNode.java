package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;

import java.util.Set;

/**
 * Marker interface for a node used to recognize residues (left overs) of
 * a recipe.
 */
public interface IRecipeResidueNode extends INode
{
    /**
     * Returns the compound instances which contribute as residue to a given recipe.
     *
     * @param recipeNode The node of the recipe in question.
     *
     * @return The compound instances with which this node contributes to the recipe as a residue.
     */
    Set<CompoundInstance> getResidueInstances(IRecipeNode recipeNode);
}
