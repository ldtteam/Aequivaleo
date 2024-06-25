package com.ldtteam.aequivaleo.vanilla.recipe.equivalency;

import com.ldtteam.aequivaleo.api.recipe.equivalency.BaseEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.calculator.RecipeVariant;
import com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency.ISmithingEquivalencyRecipe;

public class SmithingEquivalencyRecipe extends BaseEquivalencyRecipe implements ISmithingEquivalencyRecipe
{
    public SmithingEquivalencyRecipe(final RecipeVariant variant)
    {
        super(variant);
    }
}
