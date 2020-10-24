package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractNode implements INode
{

    private static final Logger LOGGER = LogManager.getLogger();

    @Nullable
    private       Set<CompoundInstance>                            result       = null;
    @NotNull
    private final Multimap<INode, Optional<Set<CompoundInstance>>> candidates   = HashMultimap.create();
    private       boolean                                          isIncomplete = false;

    @NotNull
    @Override
    public Optional<Set<CompoundInstance>> getResultingValue()
    {
        return Optional.ofNullable(result);
    }

    @Override
    public void addCandidateResult(final INode neighbor, final IEdge sourceEdge, final Optional<Set<CompoundInstance>> instances)
    {
        if (neighbor == this)
        {
            this.candidates.removeAll(this);
        }

        this.candidates.put(neighbor, instances);
    }

    @NotNull
    @Override
    public Set<Set<CompoundInstance>> getCandidates()
    {
        Set<Set<CompoundInstance>> set = new HashSet<>();
        for (Optional<Set<CompoundInstance>> compoundInstances : candidates.values())
        {
            if (compoundInstances.isPresent())
            {
                Set<CompoundInstance> instances = compoundInstances.get();
                set.add(instances);
            }
        }
        return set;
    }

    @NotNull
    @Override
    public Set<INode> getAnalyzedNeighbors()
    {
        List<INode> list = new ArrayList<>();
        for (INode n : candidates.keySet())
        {
            if (n != this)
            {
                list.add(n);
            }
        }
        return ImmutableSet.copyOf(list);
    }

    @Override
    public void onReached(final IGraph graph)
    {
        for (IEdge accessibleWeightEdge : graph.outgoingEdgesOf(this))
        {
            INode v = graph.getEdgeTarget(accessibleWeightEdge);
            v.addCandidateResult(
              this,
              accessibleWeightEdge,
              getResultingValue()
            );

            if (this.isIncomplete())
            {
                v.setIncomplete();
            }
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
        //Short circuit empty result.
        if (getCandidates().size() == 0)
        {
            if (result != null)
            {
                AnalysisLogHandler.debug(LOGGER, String.format("  > No candidates available. Using current value: %s", this.result));
            }
            else
            {
                this.isIncomplete = true;
                AnalysisLogHandler.debug(LOGGER, "  > No candidates available, and result not forced. Setting empty collection!");
                this.result = null;
            }
            return;
        }

        this.isIncomplete = false;

        //Locking happens via the intrinsic value of node itself.
        //Return that value if it exists.
        if (this.candidates.containsKey(this))
        {
            this.result = this.candidates.get(this).iterator().next().orElse(null);
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
        //
        Set<CompoundInstance> set = new HashSet<>();
        //Group each of the list again on their group, so that all candidates with the same group are together.
        //For each type invoke the determination routine.
        Set<Set<CompoundInstance>> set1 = new HashSet<>();
        //Split apart each of the initial candidate lists into several smaller list based on their group.
        List<Set<CompoundInstance>> list = new ArrayList<>();
        for (Set<CompoundInstance> candidate : getCandidates())
        {
            for (Collection<CompoundInstance> collection : GroupingUtils.groupByUsingSet(candidate,
              compoundInstance -> compoundInstance.getType().getGroup()))
            {
                if (!collection.isEmpty())
                {
                    Set<CompoundInstance> instances = Sets.newHashSet(collection);
                    list.add(instances);
                }
            }
        }
        for (Collection<Set<CompoundInstance>> sets : GroupingUtils.groupByUsingSet(list,
          compoundInstances -> compoundInstances.iterator()
                                 .next()
                                 .getType()
                                 .getGroup()))
        {
            HashSet<Set<CompoundInstance>> s = Sets.newHashSet(sets);
            if (!s.isEmpty())
            {
                Optional<Set<CompoundInstance>> compoundInstanceSet = s.iterator()
                                                                        .next()
                                                                        .iterator()
                                                                        .next()
                                                                        .getType()
                                                                        .getGroup()
                                                                        .determineResult(s, canResultBeCalculated(graph), isIncomplete());
                if (compoundInstanceSet.isPresent())
                {
                    Set<CompoundInstance> instanceSet = compoundInstanceSet.get();
                    set1.add(instanceSet);
                }
            }
        }
        for (Set<CompoundInstance> instances : set1)
        {
            set.addAll(instances);
        }
        this.result = set; //Group all of them together.

        if (this.result.isEmpty()) {
            this.result = null;
            this.isIncomplete = true;
        }

        AnalysisLogHandler.debug(LOGGER, String.format("  > Mediation completed. Determined value is: %s", this.result));
    }

    @Override
    public void clearIncompletionState()
    {
        this.isIncomplete = false;
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

    public boolean hasIncompleteChildren(final IGraph graph)
    {
        for (IEdge iEdge : graph.incomingEdgesOf(this))
        {
            INode edgeSource = graph.getEdgeSource(iEdge);
            if (edgeSource.isIncomplete())
            {
                return true;
            }
        }
        return false;
    }
}
