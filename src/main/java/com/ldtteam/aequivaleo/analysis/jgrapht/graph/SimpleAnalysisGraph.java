package com.ldtteam.aequivaleo.analysis.jgrapht.graph;

import com.ldtteam.aequivaleo.analysis.jgrapht.edge.Edge;
import org.jgrapht.GraphType;
import org.jgrapht.graph.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class SimpleAnalysisGraph<V, E> extends AbstractBaseGraph<V, E>
{
    private static final GraphType GRAPH_TYPE = new DefaultGraphType.Builder()
            .directed().allowMultipleEdges(false).allowSelfLoops(false).weighted(true)
            .build();

    private final BiFunction<V, V, E> edgeBuilder;

    public SimpleAnalysisGraph(final BiFunction<V, V, E> edgeBuilder)
    {
        super(null, () -> edgeBuilder.apply(null, null), GRAPH_TYPE, new AnalysisGraphStrategy<>());
        this.edgeBuilder = edgeBuilder;
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

    @Override
    public E addEdge(V sourceVertex, V targetVertex) {
        final E edge = edgeBuilder.apply(sourceVertex, targetVertex);

        addEdge(sourceVertex, targetVertex, edge);

        return edge;
    }

    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e) {
        if (getEdge(sourceVertex, targetVertex) != null) {
            throw new DuplicateEdgeException(sourceVertex, targetVertex);
        }

        return super.addEdge(sourceVertex, targetVertex, e);
    }
}