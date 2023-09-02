package com.ldtteam.aequivaleo.vanilla.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency.IBucketFluidEquivalencyRecipe;

import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class BucketFluidRecipe implements IBucketFluidEquivalencyRecipe
{
    private final SortedSet<IRecipeIngredient>     inputs;
    private final SortedSet<ICompoundContainer<?>> containers;
    private final SortedSet<ICompoundContainer<?>> outputs;

    public BucketFluidRecipe(final Set<IRecipeIngredient> inputs, final Set<ICompoundContainer<?>> containers, final Set<ICompoundContainer<?>> outputs)
    {
        this.inputs = new TreeSet<>(inputs);
        this.containers = new TreeSet<>(containers);
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
        return containers;
    }

    @Override
    public SortedSet<ICompoundContainer<?>> getOutputs()
    {
        return outputs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BucketFluidRecipe that = (BucketFluidRecipe) o;
        return Objects.equals(inputs, that.inputs) && Objects.equals(containers, that.containers) && Objects.equals(outputs, that.outputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputs, containers, outputs);
    }

    @Override
    public String toString() {
        return "BucketFluidRecipe{" +
                "inputs=" + inputs +
                ", containers=" + containers +
                ", outputs=" + outputs +
                '}';
    }
}
