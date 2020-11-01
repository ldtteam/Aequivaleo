package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.google.common.collect.*;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.mediation.IMediationCandidate;
import com.ldtteam.aequivaleo.api.mediation.IMediationContext;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import com.ldtteam.aequivaleo.mediation.SimpleMediationCandidate;
import com.ldtteam.aequivaleo.mediation.SimpleMediationContext;
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
    private final Multimap<INode, Optional<Set<CompoundInstance>>> candidates    = HashMultimap.create();

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
                AnalysisLogHandler.debug(LOGGER, "  > No candidates available, and result not forced. Setting empty collection!");
                this.result = null;
            }
            return;
        }

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

        final Table<ICompoundTypeGroup, INode, Set<CompoundInstance>> typeNodeCandidates = HashBasedTable.create();
        this.candidates.forEach((node, optionalResult) -> {
            if (optionalResult.isPresent()) {
                final Map<ICompoundTypeGroup, Collection<CompoundInstance>> groupedInstances = GroupingUtils.groupByUsingSetToMap(
                  optionalResult.get(),
                  compoundInstance -> compoundInstance.getType().getGroup()
                );

                groupedInstances.forEach((group, candidates) -> typeNodeCandidates.put(group, node, new HashSet<>(candidates)));
            }
        });

        final boolean hasUncalculatedChildren = hasUncalculatedChildren(graph);

        final Map<ICompoundTypeGroup, Set<CompoundInstance>> mediatedValues = Maps.newHashMap();
        typeNodeCandidates.rowKeySet().forEach(compoundTypeGroup -> {
            final Map<INode, Set<CompoundInstance>> instancesForGroup = typeNodeCandidates.row(compoundTypeGroup);

            final Set<IMediationCandidate> mediationCandidates =
              new HashSet<>();
            for (INode node : instancesForGroup.keySet())
            {
                SimpleMediationCandidate simpleMediationCandidate = new SimpleMediationCandidate(instancesForGroup.get(node), () -> node.hasMissingData(graph, compoundTypeGroup));
                mediationCandidates.add(simpleMediationCandidate);
            }

            final IMediationContext context = new SimpleMediationContext(
              mediationCandidates,
              () -> !hasUncalculatedChildren
            );

            final Optional<Set<CompoundInstance>> mediatedValue = compoundTypeGroup.getMediationEngine().determineMediationResult(context);

            mediatedValue.ifPresent(instances -> mediatedValues.put(
              compoundTypeGroup,
              instances
            ));
        });

        Set<CompoundInstance> workingResult = new HashSet<>();
        for (Set<CompoundInstance> compoundInstances : mediatedValues.values())
        {
            workingResult.addAll(compoundInstances);
        }
        this.result = workingResult;
        AnalysisLogHandler.debug(LOGGER, String.format("  > Mediation completed. Determined value is: %s", this.result));
    }
}
