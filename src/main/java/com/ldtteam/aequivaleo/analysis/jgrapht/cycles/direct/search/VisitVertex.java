package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search;

import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.Context;
import org.jgrapht.Graph;

import java.util.Comparator;
import java.util.Set;

public record VisitVertex<G extends Graph<V, E>, V, E>(V vertex, G graph) implements ISearchAction<G, V, E> {

    @Override
    public void perform(Context<G, V, E> context) {
        context.openVertex(vertex);

        graph.outgoingEdgesOf(vertex).stream().sorted(
                Comparator.comparingInt(value -> {
                            final V target = graph.getEdgeTarget(value);
                            return context.getPath().indexOfOr(target, 0);
                        })
        ).forEach(edge -> context.offerAction(new VisitEdge<>(edge, graph)));
    }

    @Override
    public boolean actionTouchesAnyOf(Set<V> vertices) {
        return vertices.contains(vertex);
    }

    @Override
    public String toString() {
        return "{ Visit Vertex: " + vertex + "}";
    }
}
