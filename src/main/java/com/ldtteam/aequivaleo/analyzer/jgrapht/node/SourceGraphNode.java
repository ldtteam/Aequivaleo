package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.ldtteam.aequivaleo.analyzer.StatCollector;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisGraphNode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import org.jgrapht.Graph;

import java.util.Set;

public class SourceGraphNode extends AbstractNode
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
    public void collectStats(final StatCollector statCollector)
    {
        statCollector.onVisitSourceNode();
    }

    @Override
    public void onReached(final Graph<INode, IEdge> graph)
    {
        //Noop
    }
}
