package com.ldtteam.aequivaleo.analyzer.jgrapht.graph;

import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.Edge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.concurrent.atomic.AtomicLong;
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
