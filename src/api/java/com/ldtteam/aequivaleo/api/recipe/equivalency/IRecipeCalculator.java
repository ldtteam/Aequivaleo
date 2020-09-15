package com.ldtteam.aequivaleo.api.recipe.equivalency;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;

/**
 * Represents a calcua
 */
public interface IRecipeCalculator
{

    static IRecipeCalculator getInstance() {
        return IAequivaleoAPI.getInstance().getRecipeCalculator();
    }
}
