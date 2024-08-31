package com.ldtteam.aequivaleo.analysis.jgrapht.connection;

import org.jgrapht.Graph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * A connection finder which uses a queue to find all reachable nodes from a given node in a graph.
 *
 * @param <G> The type of the graph.
 * @param <V> The type of the nodes.
 * @param <E> The type of the edges.
 */
public class QueueBasedConnectionFinder<G extends Graph<V, E>, V, E> implements IConnectionFinder<G, V, E> {

    @Override
    public Set<V> reachableNodes(G graph, V startNode) {
        final Deque<V> queue = new ArrayDeque<>();
        final Set<V> reachableNodes = new HashSet<>();
        queue.add(startNode);

        while (!queue.isEmpty()) {
            final V node = queue.poll();
            reachableNodes.add(node);
            graph.outgoingEdgesOf(node).stream().map(graph::getEdgeTarget).filter(reachableNodes::add).forEach(queue::add);
        }

        return reachableNodes;
    }
}
