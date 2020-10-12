package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractAnalysisGraphNode implements IAnalysisGraphNode<Set<CompoundInstance>>
{
    @Nullable
    private Set<CompoundInstance> result = null;
    @NotNull
    private Map<IAnalysisGraphNode<Set<CompoundInstance>>, Set<CompoundInstance>> candidates = Maps.newHashMap();
    private boolean isIncomplete = false;

    @NotNull
    @Override
    public Optional<Set<CompoundInstance>> getResultingValue()
    {
        return Optional.ofNullable(result);
    }

    @Override
    public void addCandidateResult(final IAnalysisGraphNode<Set<CompoundInstance>> neighbor, final Set<CompoundInstance> instances)
    {
        this.candidates.put(neighbor, instances);
    }

    @NotNull
    @Override
    public Set<Set<CompoundInstance>> getCandidates()
    {
        return ImmutableSet.copyOf(candidates.values());
    }

    @NotNull
    @Override
    public Set<IAnalysisGraphNode<Set<CompoundInstance>>> getAnalyzedNeighbors()
    {
        return ImmutableSet.copyOf(candidates.keySet().stream().filter(n -> n != this).collect(Collectors.toList()));
    }

    @Override
    public void onReached(final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> graph) {
        for (AccessibleWeightEdge accessibleWeightEdge : graph.outgoingEdgesOf(this))
        {
            IAnalysisGraphNode<Set<CompoundInstance>> v = graph.getEdgeTarget(accessibleWeightEdge);
            v.addCandidateResult(this, getResultingValue().orElse(Sets.newHashSet()));

            if (this.isIncomplete())
                v.setIncomplete();
        }
    }

    @Override
    public void forceSetResult(final Set<CompoundInstance> compoundInstances)
    {
        this.result = compoundInstances;
    }

    @Override
    public void determineResult(final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> graph)
    {
        //Short cirquit empty result.
        if (getCandidates().size() == 0)
        {
            this.result = result != null ? result : Collections.emptySet();
            return;
        }

        //Locking happens via the intrinsic value of node itself.
        //Return that value if it exists.
        if (this.candidates.containsKey(this)) {
            this.result = this.candidates.get(this);
            return;
        }

        //If we have only one other data set we have nothing to choose from.
        //So we take that.
        if (getCandidates().size() == 1)
        {
            this.result = getCandidates().iterator().next();
            return;
        }

        //If we have multiples we group them up by type group and then let it decide.
        //Then we collect them all back together into one list
        //Bit of a mess but works.
        this.result = GroupingUtils.groupBy(getCandidates()
                                              .stream()
                                              .flatMap(candidate -> GroupingUtils.groupBy(candidate, compoundInstance -> compoundInstance.getType().getGroup()).stream()) //Split apart each of the initial candidate lists into several smaller list based on their group.
                                              .filter(collection -> !collection.isEmpty())
                                              .map(Sets::newHashSet)
                                              .map(hs -> (Set<CompoundInstance>) hs)
                                              .collect(Collectors.toList()), compoundInstances -> compoundInstances.iterator().next().getType().getGroup()) //Group each of the list again on their group, so that all candidates with the same group are together.
          .stream()
          .map(Sets::newHashSet)
          .filter(s -> !s.isEmpty())
          .map(s -> s.iterator().next().iterator().next().getType().getGroup().determineResult(s, canResultBeCalculated(graph))) //For each type invoke the determination routine.
          .collect(Collectors.toSet()) //
          .stream()
          .flatMap(Collection::stream)
          .collect(Collectors.toSet()); //Group all of them together.
    }

    @Override
    public void setIncomplete()
    {
        this.isIncomplete = true;
    }

    @Override
    public boolean isIncomplete()
    {
        return this.isIncomplete;
    }

    public boolean hasIncompleteChildren(final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> graph) {
        return graph.incomingEdgesOf(this).stream().map(graph::getEdgeSource).anyMatch(IAnalysisGraphNode::isIncomplete);
    }
}
