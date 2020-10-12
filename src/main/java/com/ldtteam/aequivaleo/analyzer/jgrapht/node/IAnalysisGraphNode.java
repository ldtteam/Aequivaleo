package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.ldtteam.aequivaleo.analyzer.StatCollector;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
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

    void addCandidateResult(
      final IAnalysisGraphNode<V> neighbor,
      final Set<CompoundInstance> instances
    );

    @NotNull
    Set<V> getCandidates();

    @NotNull
    Set<IAnalysisGraphNode<V>> getAnalyzedNeighbors();

    default boolean canResultBeCalculated(final Graph<IAnalysisGraphNode<V>, AccessibleWeightEdge> graph) {
        return getResultingValue().isPresent() || getAnalyzedNeighbors().equals(graph.incomingEdgesOf(this).stream().map(graph::getEdgeSource).collect(Collectors.toSet()));
    }

    void onReached(final Graph<IAnalysisGraphNode<V>, AccessibleWeightEdge> graph);

    void collectStats(final StatCollector statCollector);

    void forceSetResult(V compoundInstances);

    void determineResult(Graph<IAnalysisGraphNode<V>, AccessibleWeightEdge> graph);

    void setIncomplete();

    boolean isIncomplete();
}
