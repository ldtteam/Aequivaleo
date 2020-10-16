package com.ldtteam.aequivaleo.analyzer.jgrapht.core;

import com.ldtteam.aequivaleo.analyzer.StatCollector;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//Marker interface indicating that this is a possible component of a graph.
public interface IAnalysisGraphNode<G extends Graph<S, E>, N, S extends IAnalysisGraphNode<G, N, S, E>, E extends IAnalysisEdge>
{
    S getSelf();

    @NotNull
    Optional<N> getResultingValue();

    default void addCandidateResult(
      final S neighbor,
      final E sourceEdge,
      final Set<CompoundInstance> instances
    ) {
        addCandidateResult(neighbor, instances);
    }

    void addCandidateResult(
      final S neighbor,
      final Set<CompoundInstance> instances
    );

    @NotNull
    Set<N> getCandidates();

    @NotNull
    Set<S> getAnalyzedNeighbors();

    default boolean canResultBeCalculated(final Graph<S, IEdge> graph) {
        return getResultingValue().isPresent() || getAnalyzedNeighbors().equals(graph.incomingEdgesOf(getSelf()).stream().map(graph::getEdgeSource).collect(Collectors.toSet()));
    }

    void onReached(final G graph);

    void collectStats(final StatCollector statCollector);

    void forceSetResult(N compoundInstances);

    void determineResult(G graph);

    void setIncomplete();

    boolean isIncomplete();

    default void onNeighborReplaced(final S originalNeighbor, final S newNeighbor) {};
}
