package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search;

import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.Context;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;

import java.util.Objects;
import java.util.Set;

public final class VisitEdge<G extends Graph<V, E>, V, E> implements ISearchAction<G, V, E> {
    private final G graph;

    @Nullable
    private E edge;

    public VisitEdge(G graph) {
        this.graph = graph;
    }

    @Override
    public void perform(Context<G, V, E> context) {
        final V target = graph().getEdgeTarget(edge());
        if (context.getPath().contains(target)) {
            context.onCycleFound(target);
        } else {
            context.offerAction(context.claimVisitVertex(target));
        }
    }

    @Override
    public boolean actionTouchesAnyOf(Set<V> vertices) {
        return vertices.contains(graph().getEdgeSource(edge())) || vertices.contains(graph().getEdgeTarget(edge()));
    }

    @Override
    public String toString() {
        if (edge ==  null) {
            return "{ Visit Edge: null }";
        }

        return "{ Visit Edge: " + graph().getEdgeSource(edge()) + " -> " + graph().getEdgeTarget(edge()) + "}";
    }

    public E edge() {
        Validate.notNull(edge);
        return edge;
    }

    public VisitEdge<G, V, E> edge(E edge) {
        this.edge = edge;
        return this;
    }

    public G graph() {
        return graph;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (VisitEdge) obj;
        return Objects.equals(this.edge, that.edge) &&
                Objects.equals(this.graph, that.graph);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edge, graph);
    }

}
