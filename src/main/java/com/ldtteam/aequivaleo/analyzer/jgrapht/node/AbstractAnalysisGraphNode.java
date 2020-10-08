package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractAnalysisGraphNode implements IAnalysisGraphNode<Set<CompoundInstance>>
{
    @Nullable
    private Set<CompoundInstance> result = null;
    @NotNull
    private Set<Set<CompoundInstance>> candidates = Sets.newConcurrentHashSet();
    @NotNull
    private Set<IAnalysisGraphNode<Set<CompoundInstance>>> analyzedNeigbors = Sets.newConcurrentHashSet();

    @NotNull
    @Override
    public Optional<Set<CompoundInstance>> getResultingValue()
    {
        return Optional.ofNullable(result);
    }

    @NotNull
    @Override
    public Set<Set<CompoundInstance>> getCandidates()
    {
        return candidates;
    }

    @NotNull
    @Override
    public Set<IAnalysisGraphNode<Set<CompoundInstance>>> getAnalyzedNeighbors()
    {
        return analyzedNeigbors;
    }

    @Override
    public void onReached(final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> graph) {
        for (AccessibleWeightEdge accessibleWeightEdge : graph.outgoingEdgesOf(this))
        {
            IAnalysisGraphNode<Set<CompoundInstance>> v = graph.getEdgeTarget(accessibleWeightEdge);
            v.getAnalyzedNeighbors().add(this);
        }
    }

    @Override
    public void determineResult()
    {
        this.result = getCandidates().stream()
          .min(Comparator.comparing(s -> s.stream().mapToDouble(CompoundInstance::getAmount).sum())).orElse(Collections.emptySet());
    }
}
