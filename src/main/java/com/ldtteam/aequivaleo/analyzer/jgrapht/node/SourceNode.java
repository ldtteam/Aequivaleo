package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.ldtteam.aequivaleo.analyzer.StatCollector;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IGraph;

public class SourceNode extends AbstractNode
{
    @Override
    public int hashCode()
    {
        return 1;
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj instanceof SourceNode;
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
    public void onReached(final IGraph graph)
    {
        //Noop
    }
}
