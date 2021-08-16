package com.ldtteam.aequivaleo.analysis.jgrapht.node;

import com.ldtteam.aequivaleo.analysis.StatCollector;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;

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
    public void onReached(final IGraph graph)
    {
        //Noop
    }

    @Override
    public void collectStats(final StatCollector statCollector)
    {
        statCollector.onVisitSourceNode();
    }

    @Override
    public boolean hasMissingData(final IGraph graph, ICompoundTypeGroup group)
    {
        //We are always complete.
        return false;
    }
}
