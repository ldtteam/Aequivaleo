package com.ldtteam.aequivaleo.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IInstancedEquivalency;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import org.apache.commons.lang3.Validate;

import java.util.*;

public class InstancedEquivalency implements IInstancedEquivalency
{
    private final SortedSet<IRecipeIngredient> source;
    private final SortedSet<ICompoundContainer<?>> target;

    public InstancedEquivalency(final ICompoundContainer<?> source, final ICompoundContainer<?> target) {
        this.source = new TreeSet<>();
        this.target = new TreeSet<>();

        this.source.add(new SimpleIngredientBuilder().from(Validate.notNull(source)).createSimpleIngredient());
        this.target.add(Validate.notNull(target));
    }

    @Override
    public ICompoundContainer<?> getSource()
    {
        return source.first().getCandidates().first();
    }

    @Override
    public ICompoundContainer<?> getTarget()
    {
        return target.first();
    }

    @Override
    public SortedSet<IRecipeIngredient> getInputs()
    {
        return source;
    }

    @Override
    public SortedSet<ICompoundContainer<?>> getRequiredKnownOutputs()
    {
        return Collections.emptySortedSet();
    }

    @Override
    public SortedSet<ICompoundContainer<?>> getOutputs()
    {
        return target;
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
        final InstancedEquivalency that = (InstancedEquivalency) o;
        return Objects.equals(getSource(), that.getSource()) &&
                 Objects.equals(getTarget(), that.getTarget());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getSource(), getTarget());
    }

    @Override
    public String toString()
    {
        return String.format("Equivalency via Instance: %s to: %s", getSource().getContents(), getTarget().getContents());
    }
}
