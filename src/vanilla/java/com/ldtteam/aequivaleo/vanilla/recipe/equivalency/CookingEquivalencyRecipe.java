package com.ldtteam.aequivaleo.vanilla.recipe.equivalency;

import com.ldtteam.aequivaleo.api.recipe.equivalency.BaseEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.calculator.RecipeVariant;
import com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency.ICookingEquivalencyRecipe;

public class CookingEquivalencyRecipe extends BaseEquivalencyRecipe implements ICookingEquivalencyRecipe
{
    public CookingEquivalencyRecipe(final RecipeVariant variant) {
        super(variant);
    }
}
