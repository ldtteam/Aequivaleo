package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search;

import com.ldtteam.aequivaleo.Aequivaleo;
import org.apache.commons.lang3.Validate;
import org.jgrapht.Graph;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class SearchActionPool<G extends Graph<V, E>, V, E> {

    private static final int MIN_POOL_SIZE = 100;

    private final G graph;

    private final Queue<VisitVertex<G, V, E>> openVisitVertices;
    private final Queue<VisitEdge<G, V, E>> openVisitEdges;
    private final Queue<CleanVertex<G, V, E>> openCleanVertices;

    private final AtomicInteger openVisitVerticesSize;
    private final AtomicInteger openVisitEdgesSize;
    private final AtomicInteger openCleanVerticesSize;

    public SearchActionPool(G graph) {
        this.openVisitVertices = new ConcurrentLinkedDeque<>();
        this.openVisitEdges = new ConcurrentLinkedDeque<>();
        this.openCleanVertices = new ConcurrentLinkedDeque<>();

        this.graph = graph;

        this.openVisitVerticesSize = new AtomicInteger(0);
        this.openVisitEdgesSize = new AtomicInteger(0);
        this.openCleanVerticesSize = new AtomicInteger(0);

        if (Aequivaleo.getInstance().getConfiguration().getServer().useActionPooling.get()) {
            allocate();
        }
    }

    private void allocate() {
        allocateInstances(
                openVisitVertices,
                openVisitVerticesSize,
                MIN_POOL_SIZE,
                () -> new VisitVertex<>(graph)
        );

        allocateInstances(
                openVisitEdges,
                openVisitEdgesSize,
                MIN_POOL_SIZE,
                () -> new VisitEdge<>(graph)
        );

        allocateInstances (
                openCleanVertices,
                openCleanVerticesSize,
                MIN_POOL_SIZE,
                CleanVertex::new
        );
    }

    @SuppressWarnings("SameParameterValue")
    private <T> void allocateInstances(final Queue<T> elements, AtomicInteger currentSize, int minSize, Supplier<T> instanceSupplier) {
        for (int i = currentSize.get(); i < minSize; i++) {
            elements.add(instanceSupplier.get());
            currentSize.getAndIncrement();
        }
    }

    private <T> T getAndRemove(Queue<T> elements, AtomicInteger size) {
        final T element = elements.poll();
        if (element != null) {
            size.getAndDecrement();
            return element;
        }

        throw new IllegalStateException("No elements available");
    }

    public VisitVertex<G, V, E> claimVisitVertex(V vertex) {
        Validate.notNull(vertex);

        if (!Aequivaleo.getInstance().getConfiguration().getServer().useActionPooling.get()) {
            return new VisitVertex<>(graph).vertex(vertex);
        }

        allocate();

        final VisitVertex<G, V, E> visitVertex = getAndRemove(openVisitVertices, openVisitVerticesSize);
        return visitVertex.vertex(vertex);
    }

    public VisitEdge<G, V, E> claimVisitEdge(E edge) {
        Validate.notNull(edge);

        if (!Aequivaleo.getInstance().getConfiguration().getServer().useActionPooling.get()) {
            return new VisitEdge<>(graph).edge(edge);
        }

        allocate();

        final VisitEdge<G, V, E> visitEdge = getAndRemove(openVisitEdges, openVisitEdgesSize);
        return visitEdge.edge(edge);
    }

    public CleanVertex<G, V, E> claimCleanVertex(V vertex) {
        Validate.notNull(vertex);

        if (!Aequivaleo.getInstance().getConfiguration().getServer().useActionPooling.get()) {
            final CleanVertex<G, V, E> cleanVertex = new CleanVertex<>();
            return cleanVertex.vertex(vertex);
        }

        allocate();

        final CleanVertex<G, V, E> cleanVertex = getAndRemove(openCleanVertices, openCleanVerticesSize);
        return cleanVertex.vertex(vertex);
    }

    public void release(VisitVertex<G, V, E> visitVertex) {
        openVisitVertices.add(visitVertex.vertex(null));
        openVisitVerticesSize.getAndIncrement();
    }

    public void release(VisitEdge<G, V, E> visitEdge) {
        openVisitEdges.add(visitEdge.edge(null));
        openVisitEdgesSize.getAndIncrement();
    }

    public void release(CleanVertex<G, V, E> cleanVertex) {
        openCleanVertices.add(cleanVertex.vertex(null));
        openCleanVerticesSize.getAndIncrement();
    }

    public void release(ISearchAction<G, V, E> action) {
        if (action instanceof VisitVertex) {
            release((VisitVertex<G, V, E>) action);
        } else if (action instanceof VisitEdge) {
            release((VisitEdge<G, V, E>) action);
        } else if (action instanceof CleanVertex) {
            release((CleanVertex<G, V, E>) action);
        } else {
            throw new IllegalArgumentException("Unknown action type: " + action.getClass());
        }
    }
}
