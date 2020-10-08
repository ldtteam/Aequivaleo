package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.nio.Attribute;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//Marker interface indicating that this is a possible component of a graph.
public interface IAnalysisGraphNode<V>
{

    @NotNull
    Optional<V> getResultingValue();

    @NotNull
    Set<V> getCandidates();

    @NotNull
    Set<IAnalysisGraphNode<V>> getAnalyzedNeighbors();

    default boolean isComplete(final Graph<IAnalysisGraphNode<V>, AccessibleWeightEdge> graph) {
        return getAnalyzedNeighbors().equals(graph.incomingEdgesOf(this).stream().map(graph::getEdgeSource).collect(Collectors.toSet()));
    }

    default void onNeighboringSource() {}

    void onReached(final Graph<IAnalysisGraphNode<V>, AccessibleWeightEdge> graph);

    void determineResult();
}
