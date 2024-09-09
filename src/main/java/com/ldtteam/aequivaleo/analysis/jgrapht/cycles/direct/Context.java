package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct;

import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search.CleanVertex;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search.ISearchAction;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search.SearchActionPool;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search.VisitVertex;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.trace.ICycleReducingTracer;
import org.jgrapht.Graph;

import java.util.*;
import java.util.function.Function;

public final class Context<G extends Graph<V, E>, V, E> extends SearchActionPool<G, V, E> {
    private final G graph;

    private final Function<List<V>, V> vertexReplacerFunction;

    private final ICycleReducingTracer<G, V, E> tracer;

    private final Path<V> path = new Path<>();
    private final Deque<ISearchAction<G, V, E>> next = new ArrayDeque<>();

    private final Set<V> visitedVertexes = new HashSet<>();

    Context(G graph, V startingNode, Function<List<V>, V> replacementVertexBuilder, ICycleReducingTracer<G, V, E> tracer) {
        super(graph);
        this.graph = graph;
        this.vertexReplacerFunction = replacementVertexBuilder;
        this.tracer = tracer;

        offerAction(claimVisitVertex(startingNode));
    }

    public Graph<V, E> getGraph() {
        return graph;
    }

    public void onCycleFound(V cycleCreatingNode) {
        final List<V> cycle = path.getSubPathUntil(cycleCreatingNode);

        this.tracer.onCycleFound(cycle);

        final V replacementNode = vertexReplacerFunction.apply(cycle);

        final Set<V> cycleSet = new HashSet<>(cycle);
        if (cycleSet.size() != cycle.size()) {
            throw new IllegalStateException("Cycle contains duplicates");
        }

        final Set<E> edgesToKeep = new HashSet<>();
        for (V v : cycle) {
            graph.outgoingEdgesOf(v).stream()
                    .filter(edge -> !cycleSet.contains(graph.getEdgeTarget(edge)))
                    .forEach(edgesToKeep::add);
            graph.incomingEdgesOf(v).stream()
                    .filter(edge -> !cycleSet.contains(graph.getEdgeSource(edge)))
                    .forEach(edgesToKeep::add);
        }

        //Using an iterator delete all elements from next that touch the cycle:
        final Iterator<ISearchAction<G, V, E>> iterator = next.iterator();
        while (iterator.hasNext()) {
            final ISearchAction<G, V, E> action = iterator.next();
            if (action.actionTouchesAnyOf(cycleSet)) {
                iterator.remove();
                release(action);
            }
        }

        graph.addVertex(replacementNode);

        for (E edge : edgesToKeep) {
            final V source = graph.getEdgeSource(edge);
            final V target = graph.getEdgeTarget(edge);

            if (cycleSet.contains(source)) {
                addEdgeOrUpdateWeight(replacementNode, target, graph.getEdgeWeight(edge));
            } else if (cycleSet.contains(target)) {
                addEdgeOrUpdateWeight(source, replacementNode, graph.getEdgeWeight(edge));
            } else {
                throw new IllegalStateException("Edge part of cycle");
            }

            graph.removeEdge(edge);
        }

        cycle.forEach(graph::removeVertex);

        offerAction(claimVisitVertex(replacementNode));

        final List<V> cycleCopy = new ArrayList<>(cycle);
        Collections.reverse(cycleCopy);
        cycleCopy.forEach(path::clean);
    }

    private void addEdgeOrUpdateWeight(V source, V target, double weight) {
        final E edge = graph.getEdge(source, target);
        if (edge == null) {
            final E newEdge = graph.addEdge(source, target);
            graph.setEdgeWeight(newEdge, weight);
        } else {
            final double currentWeight = graph.getEdgeWeight(edge);
            graph.setEdgeWeight(edge, currentWeight + weight);
        }
    }

    public Path<V> getPath() {
        return path;
    }

    public void openVertex(V vertex) {
        path.addFirst(vertex);

        visitedVertexes.add(vertex);
        tracer.onEncounterVertex(vertex);

        offerAction(claimCleanVertex(vertex));
    }

    public void closeVertex(V vertex) {
        path.clean(vertex);

        tracer.onExitVertex(vertex);
    }

    public void offerAction(ISearchAction<G, V, E> action) {
        if (!next.isEmpty() && next.peekFirst().equals(action)) {
            throw new IllegalStateException("Action already in queue");
        }

        next.addFirst(action);

        tracer.onActionAdded(action);
    }

    public boolean hasNextAction() {
        return !next.isEmpty();
    }

    public ISearchAction<G, V, E> popAction() {
        final ISearchAction<G, V, E> action = next.pop();

        tracer.onActionRemoved(action);

        return action;
    }
}
