package com.ldtteam.aequivaleo.analyzer.jgrapht;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.ICompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ContainerWrapperGraphNode implements IAnalysisGraphNode
{
    @NotNull
    private final ICompoundContainer<?> wrapper;

    @NotNull
    private final Set<ICompoundInstance> compoundInstances = Sets.newConcurrentHashSet();

    public ContainerWrapperGraphNode(@NotNull final ICompoundContainer<?> wrapper) {this.wrapper = wrapper;}

    @NotNull
    public ICompoundContainer<?> getWrapper()
    {
        return wrapper;
    }

    @NotNull
    public Set<ICompoundInstance> getCompoundInstances()
    {
        return compoundInstances;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ContainerWrapperGraphNode))
        {
            return false;
        }

        final ContainerWrapperGraphNode that = (ContainerWrapperGraphNode) o;

        return getWrapper().equals(that.getWrapper());
    }

    @Override
    public int hashCode()
    {
        return getWrapper().hashCode();
    }

    @Override
    public String toString()
    {
        return "ContainerWrapperGraphNode{" +
                 "wrapper=" + wrapper +
                 '}';
    }



}
