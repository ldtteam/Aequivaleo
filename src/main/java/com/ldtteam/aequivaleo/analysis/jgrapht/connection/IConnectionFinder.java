package com.ldtteam.aequivaleo.analysis.jgrapht.connection;

import org.jgrapht.Graph;

import java.util.Set;

/**
 * Defines an algorithm which can find all nodes reachable from a given node in a graph.
 *
 * @param <G> The type of the graph.
 * @param <V> The type of the nodes.
 * @param <E> The type of the edges.
 */
public interface IConnectionFinder<G extends Graph<V, E>, V, E> {

    /**
     * Finds all nodes reachable from a given node in a graph.
     *
     * @param graph The graph.
     * @param startNode The node to start from.
     * @return The set of reachable nodes.
     */
    Set<V> reachableNodes(G graph, V startNode);
}
