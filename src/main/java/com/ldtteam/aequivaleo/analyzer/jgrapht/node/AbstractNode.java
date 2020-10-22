package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.google.common.collect.*;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisGraphNode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractNode implements INode
{

    private static final Logger LOGGER = LogManager.getLogger();

    @Nullable
    private Set<CompoundInstance>                                                      result       = null;
    @NotNull
    private Multimap<INode, Set<CompoundInstance>> candidates   = HashMultimap.create();
    private boolean                                                                    isIncomplete = false;

    @NotNull
    @Override
    public Optional<Set<CompoundInstance>> getResultingValue()
    {
        return Optional.ofNullable(result);
    }

    @Override
    public void addCandidateResult(final INode neighbor, final IEdge sourceEdge, final Set<CompoundInstance> instances)
    {
        if (neighbor == this)
            this.candidates.removeAll(this);

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
    public Set<INode> getAnalyzedNeighbors()
    {
        return ImmutableSet.copyOf(candidates.keySet().stream().filter(n -> n != this).collect(Collectors.toList()));
    }

    @Override
    public void onReached(final IGraph graph) {
        for (IEdge accessibleWeightEdge : graph.outgoingEdgesOf(this))
        {
            INode v = graph.getEdgeTarget(accessibleWeightEdge);
            v.addCandidateResult(this, accessibleWeightEdge, getResultingValue().orElse(Sets.newHashSet()));

            if (this.isIncomplete())
                v.setIncomplete();
        }
    }

    @Override
    public void forceSetResult(final Set<CompoundInstance> compoundInstances)
    {
        AnalysisLogHandler.debug(LOGGER, String.format("Force setting the result of: %s to: %s", this, compoundInstances));
        this.result = compoundInstances;
    }

    @Override
    public void determineResult(final IGraph graph)
    {
        AnalysisLogHandler.debug(LOGGER, String.format("Determining the result of: %s", this));
        //Short cirquit empty result.
        if (getCandidates().size() == 0)
        {
            if (result != null)
            {
                AnalysisLogHandler.debug(LOGGER, String.format("  > No candidates available. Using current value: %s", this.result));
            }
            else
            {
                AnalysisLogHandler.debug(LOGGER, "  > No candidates available, and result not forced. Setting empty collection!");
                this.result = Collections.emptySet();
            }
            return;
        }

        //Locking happens via the intrinsic value of node itself.
        //Return that value if it exists.
        if (this.candidates.containsKey(this)) {
            this.result = this.candidates.get(this).iterator().next();
            AnalysisLogHandler.debug(LOGGER, String.format("  > Candidate data contained forced value: %s", this.result));
            return;
        }

        //If we have only one other data set we have nothing to choose from.
        //So we take that.
        if (getCandidates().size() == 1)
        {
            this.result = getCandidates().iterator().next();
            AnalysisLogHandler.debug(LOGGER, String.format("  > Candidate data contained exactly one entry: %s", this.result));
            return;
        }

        AnalysisLogHandler.debug(LOGGER, "  > Candidate data contains more then one entry. Mediation is required. Invoking type group callbacks to determine value.");
        //If we have multiples we group them up by type group and then let it decide.
        //Then we collect them all back together into one list
        //Bit of a mess but works.
        this.result = GroupingUtils.groupByUsingSet(getCandidates()
                                              .stream()
                                              .flatMap(candidate -> GroupingUtils.groupByUsingSet(candidate, compoundInstance -> compoundInstance.getType().getGroup()).stream()) //Split apart each of the initial candidate lists into several smaller list based on their group.
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

        AnalysisLogHandler.debug(LOGGER, String.format("  > Mediation completed. Determined value is: %s", this.result));
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

    public boolean hasIncompleteChildren(final IGraph graph) {
        return graph.incomingEdgesOf(this).stream().map(graph::getEdgeSource).anyMatch(IAnalysisGraphNode::isIncomplete);
    }
}
