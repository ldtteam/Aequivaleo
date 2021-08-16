package com.ldtteam.aequivaleo.analysis.jgrapht.graph;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class SimpleAnalysisGraph<V, E> extends DirectedWeightedMultigraph<V, E>
{
    public SimpleAnalysisGraph(final Supplier<E> edgeSupplier)
    {
        super(null, edgeSupplier);
    }

    @Override
    protected String toStringFromSets(final Collection<? extends V> vertexSet, final Collection<? extends E> edgeSet, final boolean directed)
    {
        List<String> renderedEdges = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        for (E e : edgeSet) {
            if (directed) {
                sb.append("(");
            } else {
                sb.append("{");
            }
            sb.append(getEdgeSource(e));
            sb.append(",");
            sb.append(getEdgeTarget(e));
            if (directed) {
                sb.append(")");
            } else {
                sb.append("}");
            }
            renderedEdges.add(sb.toString());
            sb.setLength(0);
        }

        return "(" + vertexSet + ", " + renderedEdges + ")";
    }
}