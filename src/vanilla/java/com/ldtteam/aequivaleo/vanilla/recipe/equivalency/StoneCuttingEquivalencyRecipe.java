package com.ldtteam.aequivaleo.vanilla.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.GenericRecipeEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency.IStoneCuttingEquivalencyRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class StoneCuttingEquivalencyRecipe extends GenericRecipeEquivalencyRecipe implements IStoneCuttingEquivalencyRecipe
{
    public StoneCuttingEquivalencyRecipe(
      final ResourceLocation recipeName,
      final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> requiredKnownOutputs,
      final Set<ICompoundContainer<?>> outputs)
    {
        super(recipeName, inputs, requiredKnownOutputs, outputs);
    }

    @Override
    public String toString()
    {
        return String.format("Equivalent via stone cutting: %s", recipeName);
    }
}
