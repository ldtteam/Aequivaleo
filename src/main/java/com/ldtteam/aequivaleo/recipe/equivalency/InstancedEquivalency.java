package com.ldtteam.aequivaleo.recipe.equivalency;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;

import java.util.Objects;
import java.util.Set;

public class InstancedEquivalency implements IInstancedEquivalency
{
    private final boolean isBlock;
    private final ICompoundContainer<?> source;
    private final ICompoundContainer<?> target;

    public InstancedEquivalency(final boolean isBlock, final ICompoundContainer<?> source, final ICompoundContainer<?> target) {
        this.isBlock = isBlock;
        this.source = source;
        this.target = target;
    }

    @Override
    public boolean isBlock()
    {
        return isBlock;
    }

    @Override
    public ICompoundContainer<?> getSource()
    {
        return source;
    }

    @Override
    public ICompoundContainer<?> getTarget()
    {
        return target;
    }

    @Override
    public Set<ICompoundContainer<?>> getInputs()
    {
        return Sets.newHashSet(getSource());
    }

    @Override
    public Set<ICompoundContainer<?>> getOutputs()
    {
        return Sets.newHashSet(getTarget());
    }

    @Override
    public Double getOffsetFactor()
    {
        return 1d;
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
