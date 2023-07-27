package com.ldtteam.aequivaleo.vanilla.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency.ICookingEquivalencyRecipe;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class CookingEquivalencyRecipe implements ICookingEquivalencyRecipe
{
    private final ResourceLocation recipeName;
    private final SortedSet<IRecipeIngredient> inputs;
    private final SortedSet<ICompoundContainer<?>> requiredKnownOutputs;
    private final SortedSet<ICompoundContainer<?>> outputs;

    public CookingEquivalencyRecipe(
      final ResourceLocation recipeName, final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> requiredKnownOutputs, final Set<ICompoundContainer<?>> outputs) {
        this.recipeName = recipeName;
        this.inputs = new TreeSet<>(Validate.noNullElements(Validate.notNull(inputs)));
        this.requiredKnownOutputs = new TreeSet<>(Validate.noNullElements(Validate.notNull(requiredKnownOutputs)));
        this.outputs = new TreeSet<>(Validate.noNullElements(Validate.notNull(outputs)));
    }

    @Override
    public SortedSet<IRecipeIngredient> getInputs()
    {
        return inputs;
    }

    @Override
    public SortedSet<ICompoundContainer<?>> getRequiredKnownOutputs()
    {
        return this.requiredKnownOutputs;
    }

    @Override
    public SortedSet<ICompoundContainer<?>> getOutputs()
    {
        return outputs;
    }

    public ResourceLocation getRecipeName()
    {
        return recipeName;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof final CookingEquivalencyRecipe that))
        {
            return false;
        }

        if (getInputs() != null ? !getInputs().equals(that.getInputs()) : that.getInputs() != null)
        {
            return false;
        }
        return getOutputs() != null ? getOutputs().equals(that.getOutputs()) : that.getOutputs() == null;
    }

    @Override
    public int hashCode()
    {
        int result = getInputs() != null ? getInputs().hashCode() : 0;
        result = 31 * result + (getOutputs() != null ? getOutputs().hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return String.format("Equivalent via smelting: %s", recipeName);
    }
}
