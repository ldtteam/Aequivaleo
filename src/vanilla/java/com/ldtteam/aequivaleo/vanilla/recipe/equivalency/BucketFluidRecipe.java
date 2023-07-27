package com.ldtteam.aequivaleo.vanilla.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency.IBucketFluidEquivalencyRecipe;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class BucketFluidRecipe implements IBucketFluidEquivalencyRecipe
{
    private final SortedSet<IRecipeIngredient>     inputs;
    private final SortedSet<ICompoundContainer<?>> outputs;

    public BucketFluidRecipe(final Set<IRecipeIngredient> inputs, final Set<ICompoundContainer<?>> outputs)
    {
        this.inputs = new TreeSet<>(inputs);
        this.outputs = new TreeSet<>(outputs);
    }

    @Override
    public SortedSet<IRecipeIngredient> getInputs()
    {
        return inputs;
    }

    @Override
    public SortedSet<ICompoundContainer<?>> getRequiredKnownOutputs()
    {
        return new TreeSet<>();
    }

    @Override
    public SortedSet<ICompoundContainer<?>> getOutputs()
    {
        return outputs;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof BucketFluidRecipe))
        {
            return false;
        }

        final BucketFluidRecipe that = (BucketFluidRecipe) o;

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
        return "Equivalent via Bucket and Fluid{" +
                 "inputs=" + inputs +
                 ", outputs=" + outputs +
                 '}';
    }
}
