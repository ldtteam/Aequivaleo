package com.ldtteam.aequivaleo.api.recipe.equivalency.calculator;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;

import java.util.Collection;

public record RecipeVariant(Collection<IRecipeIngredient> ingredients, Collection<IRecipeIngredient> remainders, ICompoundContainer<?> output) {

}
