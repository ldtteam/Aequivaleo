package com.ldtteam.aequivaleo.vanilla.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency.ISimpleEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class SimpleEquivalencyRecipe extends AbstractEquivalencyRecipe implements ISimpleEquivalencyRecipe
{

    public SimpleEquivalencyRecipe(
      final ResourceLocation recipeName,
      final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> requiredKnownOutputs,
      final Set<ICompoundContainer<?>> outputs) {
        super(recipeName, inputs, requiredKnownOutputs, outputs);
    }

    @Override
    public String toString()
    {
        return String.format("Equivalent via vanilla crafting: %s", recipeName);
    }
}
