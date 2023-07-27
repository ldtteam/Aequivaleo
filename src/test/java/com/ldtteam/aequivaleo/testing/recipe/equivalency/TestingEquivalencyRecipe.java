package com.ldtteam.aequivaleo.testing.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import org.apache.commons.lang3.Validate;

import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class TestingEquivalencyRecipe implements IEquivalencyRecipe
{
    private final String name;
    private final SortedSet<IRecipeIngredient>     inputs;
    private final SortedSet<ICompoundContainer<?>> requiredKnownOutputs;
    private final SortedSet<ICompoundContainer<?>> outputs;

    public TestingEquivalencyRecipe(
      final String name, final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> requiredKnownOutputs,
      final Set<ICompoundContainer<?>> outputs
    ) {
        this.name = name;
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

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final TestingEquivalencyRecipe that = (TestingEquivalencyRecipe) o;
        return getInputs().equals(that.getInputs()) &&
                 getRequiredKnownOutputs().equals(that.getRequiredKnownOutputs()) &&
                 getOutputs().equals(that.getOutputs());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getInputs(), getRequiredKnownOutputs(), getOutputs());
    }

    @Override
    public String toString()
    {
        return "TestingEquivalencyRecipe{" +
                 "name='" + name + '\'' +
                 '}';
    }
}
