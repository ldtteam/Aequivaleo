package com.ldtteam.aequivaleo.vanilla.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.BaseEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.GenericRecipeEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency.IBucketFluidEquivalencyRecipe;

import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class BucketFluidRecipe extends BaseEquivalencyRecipe implements IBucketFluidEquivalencyRecipe
{
    public BucketFluidRecipe(final Set<IRecipeIngredient> inputs, final Set<IRecipeIngredient> containers, final Set<ICompoundContainer<?>> outputs)
    {
        super(inputs, containers, outputs);
    }
}
