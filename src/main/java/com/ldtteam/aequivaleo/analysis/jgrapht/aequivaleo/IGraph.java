package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo;

import org.jgrapht.Graph;

public interface IGraph extends Graph<INode, IEdge>
{
    void addEdgeOrUpdateWeight(INode source, INode target, double weight);

    void clearIncomingEdgesOf(INode node);
}
