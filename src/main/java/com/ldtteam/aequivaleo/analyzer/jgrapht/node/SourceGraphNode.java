package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.Set;

public class SourceGraphNode extends AbstractAnalysisGraphNode
{

    @Override
    public int hashCode()
    {
        return 1;
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj instanceof SourceGraphNode;
    }

    @Override
    public String toString()
    {
        return "SourceGraphNode{}";
    }

    @Override
    public void onReached(final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> graph)
    {
        for (AccessibleWeightEdge accessibleWeightEdge : graph.outgoingEdgesOf(this))
        {
            IAnalysisGraphNode<Set<CompoundInstance>> v = graph.getEdgeTarget(accessibleWeightEdge);
            v.getAnalyzedNeighbors().add(this);
            v.determineResult();
            v.onNeighboringSource();
        }
    }
}
