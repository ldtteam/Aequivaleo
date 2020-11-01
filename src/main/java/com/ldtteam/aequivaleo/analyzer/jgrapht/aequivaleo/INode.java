package com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisGraphNode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;

import java.util.Set;

/**
 * The base node spec for aequivaleo analysis.
 */
public interface INode extends IAnalysisGraphNode<IGraph, Set<CompoundInstance>, INode, IEdge>
{
    @Override
    default INode getSelf() {
        return this;
    }

    @Override
    default boolean hasUncalculatedChildren(final IGraph graph) {
        if (!graph.containsVertex(this))
        {
            return false;
        }

        for (IEdge iEdge : graph.incomingEdgesOf(this))
        {
            INode node = graph.getEdgeSource(iEdge);
            if (!node.getResultingValue().isPresent() || node.getResultingValue().get().isEmpty())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    default boolean hasMissingData(IGraph graph, ICompoundTypeGroup group) {
        if (!getResultingValue().isPresent() || getResultingValue().get().isEmpty())
        {
            return true;
        }
        for (CompoundInstance i : getResultingValue().get())
        {
            if (i.getType().getGroup() == group)
            {
                return false;
            }
        }
        if (!graph.containsVertex(this))
        {
            return false;
        }
        for (IEdge iEdge : graph.incomingEdgesOf(this))
        {
            INode node = graph.getEdgeSource(iEdge);
            if (node.hasMissingData(graph, group))
            {
                return true;
            }
        }
        return false;
    }
}
