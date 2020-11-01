package com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public interface INodeWithoutResult extends INode
{

    @NotNull
    @Override
    default Optional<Set<CompoundInstance>> getResultingValue() {
        return Optional.empty();
    }

    @Override
    default boolean hasMissingData(IGraph graph, ICompoundTypeGroup group)
    {
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
