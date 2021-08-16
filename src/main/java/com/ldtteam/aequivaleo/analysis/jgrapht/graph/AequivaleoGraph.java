package com.ldtteam.aequivaleo.analysis.jgrapht.graph;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.edge.Edge;

import java.util.function.Supplier;

public class AequivaleoGraph extends SimpleAnalysisGraph<INode, IEdge> implements IGraph
{
    public AequivaleoGraph()
    {
        super(createEdgeSupplier());
    }

    private static Supplier<IEdge> createEdgeSupplier() {
        return Edge::new;
    }
}
