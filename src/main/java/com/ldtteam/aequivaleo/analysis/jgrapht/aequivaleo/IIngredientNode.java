package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;

/**
 * Represents a node that represents an ingredient in a recipe in a graph;
 */
public interface IIngredientNode extends IResultsOwningNode
{
    IRecipeIngredient ingredient();
}
