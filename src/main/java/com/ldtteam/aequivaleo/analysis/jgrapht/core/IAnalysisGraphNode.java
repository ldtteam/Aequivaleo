package com.ldtteam.aequivaleo.analysis.jgrapht.core;

import com.ldtteam.aequivaleo.analysis.StatCollector;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
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

    default boolean canResultBeCalculated(final G analysisGraph, Set<S> nodesToBeAnalyzed) {
        //Either we already have a value, are forced analyzed or all our neighbors need to be analyzed.
        if (getResultingValue().isPresent() || !analysisGraph.containsVertex(getSelf()))
        {
            return true;
        }

        Set<S> set = new HashSet<>();
        for (E iEdge : analysisGraph.incomingEdgesOf(getSelf()))
        {
            S edgeSource = analysisGraph.getEdgeSource(iEdge);
            set.add(edgeSource);
        }

        final Set<S> analyzedNeighbors = getAnalyzedNeighbors();
        //TODO: Check if this case ever happens and what the consequences are.
        if (set.isEmpty() && analyzedNeighbors.isEmpty()) {
            return true;
        }

        final Set<S> missing = new HashSet<>(set);
        missing.removeAll(analyzedNeighbors);

        //If these are two different graphs (reference wise check suffices), then that means we are operating in a
        //scenario where candidates are being searched. For example in cycles. As such some inputs will not have values, yet we can calculate our results of
        //what we have.
        if (!nodesToBeAnalyzed.isEmpty()) {
            missing.removeAll(nodesToBeAnalyzed);
        }

        //If the set is empty then all nodes are potentially accounted for:
        //- Either they are calculated
        //- Or are potentially being calculated in a circle.
        return missing.isEmpty();
    }

    boolean hasUncalculatedChildren(final G graph);

    void onReached(final G graph);

    void collectStats(final StatCollector statCollector);

    void forceSetResult(N compoundInstances);

    void setBaseResult(N compoundInstances);

    void determineResult(G graph);

    boolean hasMissingData(G graph, ICompoundTypeGroup group);

    default boolean hasParentsWithMissingData(G graph, ICompoundTypeGroup group) {
        for (E e : graph.incomingEdgesOf(getSelf()))
        {
            S edgeSource = graph.getEdgeSource(e);
            if (edgeSource.hasMissingData(graph, group))
            {
                return true;
            }
        }
        return false;
    }

    default void onNeighborReplaced(final S originalNeighbor, final S newNeighbor) {}

    default void onOutgoingEdgeDisable(INode target, IEdge edge) {};

    default void onOutgoingEdgeEnabled(INode target, IEdge edge) {};
}
