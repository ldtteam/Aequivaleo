package com.ldtteam.aequivaleo.vanilla.recipe.equivalency;

import com.ldtteam.aequivaleo.api.recipe.equivalency.BaseEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.calculator.RecipeVariant;
import com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency.IStoneCuttingEquivalencyRecipe;

public class StoneCuttingEquivalencyRecipe extends BaseEquivalencyRecipe implements IStoneCuttingEquivalencyRecipe
{
    public StoneCuttingEquivalencyRecipe(final RecipeVariant variant)
    {
        super(variant);
    }
}
