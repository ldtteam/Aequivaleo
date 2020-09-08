package com.ldtteam.aequivaleo.recipe.equivalency;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import org.apache.commons.lang3.Validate;

import java.util.*;

public class InstancedEquivalency implements IInstancedEquivalency
{
    private final boolean isBlock;
    private final SortedSet<IRecipeIngredient> source;
    private final SortedSet<ICompoundContainer<?>> target;

    public InstancedEquivalency(final boolean isBlock, final ICompoundContainer<?> source, final ICompoundContainer<?> target) {
        this.isBlock = isBlock;
        this.source = new TreeSet<>();
        this.target = new TreeSet<>();

        this.source.add(new SimpleIngredientBuilder().from(Validate.notNull(source)).createSimpleIngredient());
        this.target.add(Validate.notNull(target));
    }

    @Override
    public boolean isBlock()
    {
        return isBlock;
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
        return isBlock() == that.isBlock() &&
                 Objects.equals(getSource(), that.getSource()) &&
                 Objects.equals(getTarget(), that.getTarget());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(isBlock(), getSource(), getTarget());
    }

    @Override
    public String toString()
    {
        if (isBlock)
            return String.format("Equivalency via Block(State): %s to: %s", getSource().getContents(), getTarget().getContents());

        return String.format("Equivalency via Item(Stack): %s to: %s", getSource().getContents(), getTarget().getContents());
    }
}
