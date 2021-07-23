package com.ldtteam.aequivaleo.api.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class GenericRecipeEquivalencyRecipe implements IGenericRecipeEquivalencyRecipe
{
    protected final ResourceLocation                 recipeName;
    protected final SortedSet<IRecipeIngredient>     inputs;
    protected final SortedSet<ICompoundContainer<?>> requiredKnownOutputs;
    protected final SortedSet<ICompoundContainer<?>> outputs;

    public GenericRecipeEquivalencyRecipe(
      final ResourceLocation recipeName, final Set<IRecipeIngredient> inputs, final Set<ICompoundContainer<?>> requiredKnownOutputs, final Set<ICompoundContainer<?>> outputs)
    {
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
        return requiredKnownOutputs;
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
        if (!(o instanceof GenericRecipeEquivalencyRecipe))
        {
            return false;
        }
        final GenericRecipeEquivalencyRecipe that = (GenericRecipeEquivalencyRecipe) o;
        return getRecipeName().equals(that.getRecipeName()) &&
                 getInputs().equals(that.getInputs()) &&
                 getRequiredKnownOutputs().equals(that.getRequiredKnownOutputs()) &&
                 getOutputs().equals(that.getOutputs());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getRecipeName(), getInputs(), getRequiredKnownOutputs(), getOutputs());
    }
}
