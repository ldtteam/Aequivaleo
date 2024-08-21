package com.ldtteam.aequivaleo.api.recipe.equivalency.calculator;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;

import java.util.Collection;

/**
 * Represents a recipe variant.
 *
 * @param ingredients The ingredients needed to complete the recipe.
 * @param remainders The remainders of the recipe, that are left after the recipe is completed (bucket from water bucket etc)
 * @param output The output of the recipe.
 */
public record RecipeVariant(Collection<IRecipeIngredient> ingredients, Collection<IRecipeIngredient> remainders, ICompoundContainer<?> output) {
}
