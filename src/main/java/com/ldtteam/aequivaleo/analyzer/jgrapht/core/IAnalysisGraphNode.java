package com.ldtteam.aequivaleo.analyzer.jgrapht.core;

import com.ldtteam.aequivaleo.analyzer.StatCollector;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

//Marker interface indicating that this is a possible component of a graph.
public interface IAnalysisGraphNode<G extends Graph<S, E>, N, S extends IAnalysisGraphNode<G, N, S, E>, E extends IAnalysisEdge>
{
    S getSelf();

    @NotNull
    Optional<N> getResultingValue();

    void addCandidateResult(
       final S neighbor,
      final E sourceEdge,
      final Optional<Set<CompoundInstance>> instances
    );

    @NotNull
    Set<N> getCandidates();

    @NotNull
    Set<S> getAnalyzedNeighbors();

    default boolean canResultBeCalculated(final Graph<S, IEdge> graph) {
        //Either we already have a value, are forced analyzed or all our neighbors need to be analyzed.
        if (getResultingValue().isPresent() || !graph.containsVertex(getSelf()))
        {
            return true;
        }

        Set<S> set = new HashSet<>();
        for (IEdge iEdge : graph.incomingEdgesOf(getSelf()))
        {
            S edgeSource = graph.getEdgeSource(iEdge);
            set.add(edgeSource);
        }
        return getAnalyzedNeighbors().containsAll(set);
    }

    void onReached(final G graph);

    void collectStats(final StatCollector statCollector);

    void forceSetResult(N compoundInstances);

    void determineResult(G graph);

    void clearIncompletionState();

    void setIncomplete();

    boolean isIncomplete();

    default void onNeighborReplaced(final S originalNeighbor, final S newNeighbor) {}
}
