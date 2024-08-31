package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search;

import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.Context;
import org.jgrapht.Graph;

import java.util.Set;

public record CleanVertex<G extends Graph<V, E>, V, E>(V vertex) implements ISearchAction<G, V, E> {

    @Override
    public void perform(
            Context<G, V, E> context
    ) {
        context.closeVertex(vertex);
    }

    @Override
    public boolean actionTouchesAnyOf(Set<V> vertices) {
        return vertices.contains(vertex);
    }

    @Override
    public String toString() {
        return "{ Clean Vertex: " + vertex + "}";
    }
}
