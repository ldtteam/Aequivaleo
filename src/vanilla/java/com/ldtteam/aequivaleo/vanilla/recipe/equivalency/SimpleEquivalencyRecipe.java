package com.ldtteam.aequivaleo.vanilla.recipe.equivalency;

import com.ldtteam.aequivaleo.api.recipe.equivalency.BaseEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.calculator.RecipeVariant;
import com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency.ISimpleEquivalencyRecipe;

public class SimpleEquivalencyRecipe extends BaseEquivalencyRecipe implements ISimpleEquivalencyRecipe
{

    public SimpleEquivalencyRecipe(final RecipeVariant variant) {
        super(variant);
    }
}
