package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search;

import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.Context;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;

import java.util.*;

public final class VisitVertex<G extends Graph<V, E>, V, E> implements ISearchAction<G, V, E> {

    private final G graph;

    @Nullable
    private V vertex;

    public VisitVertex(G graph) {
        this.graph = graph;
    }

    @Override
    public void perform(Context<G, V, E> context) {
        context.openVertex(vertex());

        List<E> toSort = new ArrayList<>(graph.outgoingEdgesOf(vertex()));
        toSort.sort(Comparator.comparingInt(value -> {
            final V target = graph.getEdgeTarget(value);
            return context.getPath().indexOfOr(target, 0);
        }));
        for (E edge : toSort) {
            context.offerAction(context.claimVisitEdge(edge));
        }
    }

    @Override
    public boolean actionTouchesAnyOf(Set<V> vertices) {
        return vertices.contains(vertex());
    }

    @Override
    public String toString() {
        return "{ Visit Vertex: " + vertex + "}";
    }

    @NotNull
    public V vertex() {
        return Validate.notNull(vertex);
    }

    public VisitVertex<G, V, E> vertex(V vertex) {
        this.vertex = vertex;
        return this;
    }

    public G graph() {
        return graph;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (VisitVertex) obj;
        return Objects.equals(this.vertex, that.vertex) &&
                Objects.equals(this.graph, that.graph);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertex, graph);
    }

}
