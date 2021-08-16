package com.ldtteam.aequivaleo.analysis.jgrapht.node;

import com.ldtteam.aequivaleo.analysis.StatCollector;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.*;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ContainerNode extends AbstractNode implements IContainerNode, IRecipeResidueNode, IRecipeOutputNode, IStartAnalysisNode
{
    @NotNull
    private final ICompoundContainer<?> wrapper;
    private final int hashCode;

    public ContainerNode(@NotNull final ICompoundContainer<?> wrapper) {
        this.wrapper = wrapper;
        this.hashCode = getWrapper().map(Object::hashCode).orElse(0);
    }

    @NotNull
    public Optional<ICompoundContainer<?>> getWrapper()
    {
        return Optional.of(wrapper);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ContainerNode))
        {
            return false;
        }

        final ContainerNode that = (ContainerNode) o;

        return getWrapper().equals(that.getWrapper());
    }

    @Override
    public void addCandidateResult(final INode neighbor, final IEdge sourceEdge, final Optional<Set<CompoundInstance>> instances)
    {
        super.addCandidateResult(neighbor, sourceEdge, instances.map(innerInstances -> {
            Set<CompoundInstance> set = new HashSet<>();
            for (CompoundInstance i : innerInstances)
            {
                if (i.getType().getGroup().isValidFor(wrapper, i))
                {
                    set.add(i);
                }
            }
            return set;
        }));
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    @Override
    public String toString()
    {
        return "ContainerNode{" +
                  wrapper +
                 '}';
    }

    @Override
    public void collectStats(final StatCollector statCollector)
    {
        statCollector.onVisitContainerNode();
    }

    @Override
    public Set<CompoundInstance> getResidueInstances(final IRecipeNode recipeNode)
    {
        return getResultingValue().orElse(Collections.emptySet());
    }
}
