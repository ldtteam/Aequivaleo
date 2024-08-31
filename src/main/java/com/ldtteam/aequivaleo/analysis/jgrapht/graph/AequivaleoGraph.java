package com.ldtteam.aequivaleo.analysis.jgrapht.graph;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.ICoreNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.edge.Edge;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class AequivaleoGraph extends SimpleAnalysisGraph<INode, IEdge> implements IGraph
{
    Integer hashCode = null;

    public AequivaleoGraph()
    {
        super(createEdgeSupplier());
    }

    private static Supplier<IEdge> createEdgeSupplier() {
        return Edge::new;
    }

    @Override
    public void addEdgeOrUpdateWeight(INode source, INode target, double weight) {
        final IEdge edge = getEdge(source, target);
        if (edge == null) {
            final IEdge newEdge = addEdge(source, target);
            setEdgeWeight(newEdge, weight);
        } else {
            final double currentWeight = getEdgeWeight(edge);
            setEdgeWeight(edge, currentWeight + weight);
        }
    }

    @Override
    public void clearIncomingEdgesOf(INode node) {
        final Set<IEdge> incomingEdges = new HashSet<>(incomingEdgesOf(node));
        incomingEdges.forEach(this::removeEdge);

        if (node instanceof ICoreNode coreNode) {
            coreNode.inputs()
                    .forEach(input -> input.removeOutput(coreNode));
            coreNode.clearInputs();
        }
    }

    @Override
    public String toString() {
        return "AequivaleoGraph{}";
    }
}
