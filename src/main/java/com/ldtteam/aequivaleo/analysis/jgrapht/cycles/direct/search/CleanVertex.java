package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search;

import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.Context;
import org.apache.commons.lang3.Validate;
import org.jgrapht.Graph;

import java.util.Objects;
import java.util.Set;

public final class CleanVertex<G extends Graph<V, E>, V, E> implements ISearchAction<G, V, E> {

    private V vertex;

    public CleanVertex() {
    }

    @Override
    public void perform(
            Context<G, V, E> context
    ) {
        context.closeVertex(vertex());
    }

    @Override
    public boolean actionTouchesAnyOf(Set<V> vertices) {
        return vertices.contains(vertex());
    }

    @Override
    public String toString() {
        return "{ Clean Vertex: " + vertex + "}";
    }

    public V vertex() {
        return Validate.notNull(vertex);
    }

    public CleanVertex<G, V, E> vertex(V vertex) {
        this.vertex = vertex;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CleanVertex) obj;
        return Objects.equals(this.vertex, that.vertex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertex);
    }

}
