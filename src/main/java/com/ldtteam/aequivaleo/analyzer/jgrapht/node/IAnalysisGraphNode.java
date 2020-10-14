package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.ldtteam.aequivaleo.analyzer.StatCollector;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//Marker interface indicating that this is a possible component of a graph.
public interface IAnalysisGraphNode<N>
{
    @NotNull
    Optional<N> getResultingValue();

    void addCandidateResult(
      final IAnalysisGraphNode<N> neighbor,
      final Set<CompoundInstance> instances
    );

    @NotNull
    Set<N> getCandidates();

    @NotNull
    Set<IAnalysisGraphNode<N>> getAnalyzedNeighbors();

    default boolean canResultBeCalculated(final Graph<IAnalysisGraphNode<N>, AccessibleWeightEdge> graph) {
        return getResultingValue().isPresent() || getAnalyzedNeighbors().equals(graph.incomingEdgesOf(this).stream().map(graph::getEdgeSource).collect(Collectors.toSet()));
    }

    void onReached(final Graph<IAnalysisGraphNode<N>, AccessibleWeightEdge> graph);

    void collectStats(final StatCollector statCollector);

    void forceSetResult(N compoundInstances);

    void determineResult(Graph<IAnalysisGraphNode<N>, AccessibleWeightEdge> graph);

    void setIncomplete();

    boolean isIncomplete();

    default void onNeighborReplaced(final IAnalysisGraphNode<N> originalNeighbor, final IAnalysisGraphNode<N> newNeighbor) {};
}
