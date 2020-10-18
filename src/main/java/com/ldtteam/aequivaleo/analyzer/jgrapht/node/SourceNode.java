package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.ldtteam.aequivaleo.analyzer.StatCollector;

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
        return "SourceNode{}";
    }

    @Override
    public void collectStats(final StatCollector statCollector)
    {
        statCollector.onVisitSourceNode();
    }
}
