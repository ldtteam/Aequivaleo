package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search;

import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.Context;
import org.jgrapht.Graph;

import java.util.Set;

public record VisitEdge<G extends Graph<V, E>, V, E>(E edge, G graph) implements ISearchAction<G, V, E> {

    @Override
    public void perform(Context<G, V, E> context) {
        final V target = graph().getEdgeTarget(edge());
        if (context.getPath().contains(target)) {
            context.onCycleFound(target);
        } else {
            context.offerAction(new VisitVertex<>(target, graph()));
        }
    }

    @Override
    public boolean actionTouchesAnyOf(Set<V> vertices) {
        return vertices.contains(graph().getEdgeSource(edge())) || vertices.contains(graph().getEdgeTarget(edge()));
    }

    @Override
    public String toString() {
        return "{ Visit Edge: " + graph().getEdgeSource(edge()) + " -> " + graph().getEdgeTarget(edge()) + "}";
    }
}
