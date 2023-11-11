package com.ldtteam.aequivaleo.analysis.jgrapht.node;

import com.google.common.collect.*;
import com.ldtteam.aequivaleo.analysis.StatCollector;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.*;
import com.ldtteam.aequivaleo.analysis.jgrapht.edge.Edge;
import com.ldtteam.aequivaleo.analysis.jgrapht.graph.AequivaleoGraph;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
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

import java.util.*;

@SuppressWarnings("SuspiciousMethodCalls")
public class CliqueNode
        implements IInnerNode, IContainerNode, IIOAwareNode, IRecipeInputNode, IRecipeResidueNode, IRecipeOutputNode, IStartAnalysisNode {
    private static final Logger LOGGER = LogManager.getLogger();

    private Set<CompoundInstance> finalResult = null;
    private final IGraph ioGraph = new AequivaleoGraph();
    private final Set<INode> innerCliqueNodes = Sets.newHashSet();
    private final Set<IContainerNode> innerContainerNodes = Sets.newHashSet();
    private int hash;

    private final Table<INode, INode, IEdge> disabledIoGraphEdges = HashBasedTable.create();

    private final Multimap<INode, Optional<Set<CompoundInstance>>> candidates = ArrayListMultimap.create();

    public CliqueNode(
            final IGraph sourceGraph,
            final Set<INode> innerVertices
    ) {
        setupGraphs(sourceGraph, innerVertices);
        AnalysisLogHandler.debug(LOGGER, String.format("Created inner graph node: %s", toString()));
    }

    @Override
    public IGraph getIOGraph(final IGraph graph) {
        return ioGraph;
    }

    @Override
    public Optional<ICompoundContainer<?>> getWrapper() {
        return Optional.empty();
    }

    @Override
    public Set<ICompoundContainer<?>> getTargetedWrapper(final INode sourceNeighbor) {
        if (!ioGraph.containsVertex(sourceNeighbor)) {
            return Collections.emptySet();
        }

        Set<ICompoundContainer<?>> set = new HashSet<>();
        for (IEdge iEdge : ioGraph.outgoingEdgesOf(sourceNeighbor)) {
            INode edgeTarget = ioGraph.getEdgeTarget(iEdge);
            if (edgeTarget instanceof IContainerNode) {
                IContainerNode cn = (IContainerNode) edgeTarget;
                set.addAll(cn.getTargetedWrapper(sourceNeighbor));
            }
        }
        return set;
    }

    @Override
    public Set<ICompoundContainer<?>> getSourcedWrapper(final INode targetNeighbor) {
        if (!ioGraph.containsVertex(targetNeighbor)) {
            return Collections.emptySet();
        }

        Set<ICompoundContainer<?>> set = new HashSet<>();
        for (IEdge iEdge : ioGraph.incomingEdgesOf(targetNeighbor)) {
            INode edgeSource = ioGraph.getEdgeSource(iEdge);
            if (edgeSource instanceof IContainerNode) {
                IContainerNode cn = (IContainerNode) edgeSource;
                set.addAll(cn.getSourcedWrapper(targetNeighbor));
            }
        }
        return set;
    }

    @Override
    public Set<INode> getInnerNodes() {
        return ImmutableSet.copyOf(innerCliqueNodes);
    }

    @Override
    public Set<INode> getSourceNeighborOf(final INode neighbor) {
        if (!ioGraph.containsVertex(neighbor)) {
            return Collections.emptySet();
        }

        Set<INode> set = new HashSet<>();
        for (IEdge iEdge : ioGraph.incomingEdgesOf(neighbor)) {
            INode edgeSource = ioGraph.getEdgeSource(iEdge);
            set.add(edgeSource);
        }
        return set;
    }

    @Override
    public Set<INode> getTargetNeighborOf(final INode neighbor) {
        if (!ioGraph.containsVertex(neighbor)) {
            return Collections.emptySet();
        }

        Set<INode> set = new HashSet<>();
        for (IEdge iEdge : ioGraph.outgoingEdgesOf(neighbor)) {
            INode edgeTarget = ioGraph.getEdgeTarget(iEdge);
            set.add(edgeTarget);
        }
        return set;
    }

    @NotNull
    @Override
    public Optional<Set<CompoundInstance>> getResultingValue() {
        return Optional.ofNullable(finalResult);
    }

    @Override
    public void addCandidateResult(final INode neighbor, final IEdge sourceEdge, final Optional<Set<CompoundInstance>> instances) {
        if (!ioGraph.containsVertex(neighbor)) {
            return;
        }

        final double totalOutgoingEdgeWeight = sourceEdge.getWeight();

        for (IEdge iEdge : ioGraph.outgoingEdgesOf(neighbor)) {
            INode edgeTarget = ioGraph.getEdgeTarget(iEdge);
            final Optional<Set<CompoundInstance>> workingSet = instances.map(innerInstances -> {
                        Set<CompoundInstance> set = new HashSet<>();
                        for (CompoundInstance ci : innerInstances) {
                            CompoundInstance instance = new CompoundInstance(ci.getType(),
                                    ci.getAmount() * (ioGraph.getEdgeWeight(ioGraph.getEdge(neighbor, edgeTarget))
                                            / totalOutgoingEdgeWeight));
                            set.add(instance);
                        }
                        return set;
                    }
            );
            edgeTarget.addCandidateResult(neighbor, sourceEdge, workingSet);
            candidates.put(neighbor, instances);
            break;
        }
    }

    @NotNull
    @Override
    public Set<Set<CompoundInstance>> getCandidates() {
        Set<Set<CompoundInstance>> set = new HashSet<>();
        for (Optional<Set<CompoundInstance>> compoundInstances : candidates.values()) {
            if (compoundInstances.isPresent()) {
                Set<CompoundInstance> instances = compoundInstances.get();
                set.add(instances);
            }
        }
        return set;
    }

    @NotNull
    @Override
    public Set<INode> getAnalyzedNeighbors() {
        return candidates.keySet();
    }

    @Override
    public void onReached(final IGraph graph) {
        for (IEdge edge : ioGraph.edgeSet()) {
            if (!innerCliqueNodes.contains(ioGraph.getEdgeTarget(edge))) {
                final INode source = ioGraph.getEdgeSource(edge);
                final INode target = ioGraph.getEdgeTarget(edge);

                target.addCandidateResult(this, graph.getEdge(this, target), source.getResultingValue());
            }
        }
    }

    @Override
    public void collectStats(final StatCollector statCollector) {
        statCollector.onInnerGraphNode();
    }

    @Override
    public void forceSetResult(final Set<CompoundInstance> compoundInstances) {
        for (INode node : innerCliqueNodes) {
            node.forceSetResult(compoundInstances);
        }
    }

    @Override
    public void setBaseResult(final Set<CompoundInstance> compoundInstances) {
        for (INode node : innerCliqueNodes) {
            node.setBaseResult(compoundInstances);
        }
    }

    @Override
    public void determineResult(final IGraph graph) {
        final Set<INode> startingNodes = new HashSet<>();
        for (IContainerNode innerCliqueNode : innerContainerNodes) {
            if (!innerCliqueNode.getCandidates().isEmpty()) {
                startingNodes.add(innerCliqueNode);
            }
        }
        final Set<Set<CompoundInstance>> candidates = new HashSet<>();
        for (INode startingNode : startingNodes) {
            startingNode.determineResult(graph);
            Set<CompoundInstance> compoundInstanceSet = startingNode.getResultingValue().orElse(Sets.newHashSet());
            candidates.add(compoundInstanceSet);
        }

        Set<CompoundInstance> result = getResultingValue().orElse(null);

        //Short circuit empty result.
        if (candidates.size() == 0) {
            if (result != null) {
                AnalysisLogHandler.debug(LOGGER, String.format("  > No candidates available. Using current value: %s", result));
            } else {
                AnalysisLogHandler.debug(LOGGER, "  > No candidates available, and result not forced. Setting empty collection!");
                finalResult = null;
                for (INode node : innerCliqueNodes) {
                    node.forceSetResult(finalResult);
                }
            }
            return;
        }

        //If we have only one other data set we have nothing to choose from.
        //So we take that.
        if (getCandidates().size() == 1) {
            result = candidates.iterator().next();
            finalResult = result;
            for (INode node : innerCliqueNodes) {
                node.forceSetResult(finalResult);
            }
            AnalysisLogHandler.debug(LOGGER, String.format("  > Candidate data contained exactly one entry: %s", result));
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

                groupedInstances.forEach((group, candidateValues) -> typeNodeCandidates.put(group, node, new HashSet<>(candidateValues)));
            }
        });

        final boolean hasUncalculatedChildren = hasUncalculatedChildren(graph);

        final Map<ICompoundTypeGroup, Set<CompoundInstance>> mediatedValues = Maps.newHashMap();
        typeNodeCandidates.rowKeySet().forEach(compoundTypeGroup -> {
            final Map<INode, Set<CompoundInstance>> instancesForGroup = typeNodeCandidates.row(compoundTypeGroup);

            final Set<IMediationCandidate> mediationCandidates =
                    new HashSet<>();
            for (INode node : instancesForGroup.keySet()) {
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
        for (Set<CompoundInstance> compoundInstances : mediatedValues.values()) {
            workingResult.addAll(compoundInstances);
        }
        this.finalResult = workingResult;

        if (this.finalResult.isEmpty()) {
            this.finalResult = null;
        }

        for (INode node : innerCliqueNodes) {
            node.forceSetResult(finalResult);
        }
    }

    @Override
    public void onNeighborReplaced(final INode originalNeighbor, final INode newNeighbor) {
        if (ioGraph.containsVertex(originalNeighbor)) {
            AnalysisLogHandler.debug(LOGGER, "Updating neighbor data from: " + originalNeighbor + " to: " + newNeighbor);
            ioGraph.addVertex(newNeighbor);
            for (IEdge edge : ioGraph.outgoingEdgesOf(originalNeighbor)) {
                ioGraph.addEdge(newNeighbor, ioGraph.getEdgeTarget(edge));
                ioGraph.setEdgeWeight(newNeighbor, ioGraph.getEdgeTarget(edge), ioGraph.getEdgeWeight(edge));
            }
            for (IEdge edge : ioGraph.incomingEdgesOf(originalNeighbor)) {
                ioGraph.addEdge(ioGraph.getEdgeSource(edge), newNeighbor);
                ioGraph.setEdgeWeight(ioGraph.getEdgeSource(edge), newNeighbor, ioGraph.getEdgeWeight(edge));
            }
            ioGraph.removeVertex(originalNeighbor);

            validateIOGraph();
        }
    }

    @Override
    public void onOutgoingEdgeDisable(final INode target, final IEdge edge) {
        ioGraph.vertexSet().forEach(sourceNode -> {
            if (ioGraph.containsEdge(sourceNode, target)) {
                disabledIoGraphEdges.put(sourceNode, target, ioGraph.getEdge(sourceNode, target));
                ioGraph.removeEdge(sourceNode, target);
            }
        });
    }

    @Override
    public void onOutgoingEdgeEnabled(final INode target, final IEdge edge) {
        final Set<INode> sourceNodes = new HashSet<>(disabledIoGraphEdges.rowKeySet());

        sourceNodes.forEach(sourceNode -> {
            if (disabledIoGraphEdges.contains(sourceNode, target)) {
                ioGraph.addEdge(sourceNode, target, disabledIoGraphEdges.get(sourceNode, target));
                disabledIoGraphEdges.remove(sourceNode, target);
            }
        });
    }

    private void setupGraphs(final IGraph graph, final Set<INode> innerVertices) {
        setupInnerGraph(innerVertices);
        setupIOGraph(graph, innerVertices);

        this.hash = Objects.hash(ioGraph, innerCliqueNodes);
    }

    private void setupInnerGraph(final Set<INode> innerVertices) {
        Set<INode> innerNodesCollector = new HashSet<>();
        Set<IContainerNode> innerContainerNodesCollector = new HashSet<>();
        for (INode innerVertex : innerVertices) {
            if (innerVertex instanceof IRecipeInputNode ) {
                innerNodesCollector.add(innerVertex);
            }
            if (innerVertex instanceof IRecipeOutputNode) {
                innerNodesCollector.add(innerVertex);
            }
            if (innerVertex instanceof IContainerNode container) {
                innerContainerNodesCollector.add(container);
            }
        }
        this.innerCliqueNodes.addAll(
                innerNodesCollector
        );
        this.innerContainerNodes.addAll(
                innerContainerNodesCollector
        );
    }

    private void setupIOGraph(final IGraph graph, final Set<INode> innerVertices) {
        for (INode node : innerVertices) {
            for (IEdge iEdge : graph.incomingEdgesOf(node)) {
                if (!innerVertices.contains(graph.getEdgeSource(iEdge))) {
                    if (!ioGraph.containsVertex(graph.getEdgeSource(iEdge))) {
                        ioGraph.addVertex(graph.getEdgeSource(iEdge));
                    }

                    if (!ioGraph.containsVertex(graph.getEdgeTarget(iEdge))) {
                        ioGraph.addVertex(graph.getEdgeTarget(iEdge));
                    }
                    ioGraph.addEdge(graph.getEdgeSource(iEdge), node, new Edge());
                    ioGraph.setEdgeWeight(graph.getEdgeSource(iEdge), node, iEdge.getWeight());
                }
            }

            for (IEdge edge : graph.outgoingEdgesOf(node)) {
                if (!innerVertices.contains(graph.getEdgeTarget(edge))) {
                    if (!ioGraph.containsVertex(graph.getEdgeSource(edge))) {
                        ioGraph.addVertex(graph.getEdgeSource(edge));
                    }

                    if (!ioGraph.containsVertex(graph.getEdgeTarget(edge))) {
                        ioGraph.addVertex(graph.getEdgeTarget(edge));
                    }
                    ioGraph.addEdge(node, graph.getEdgeTarget(edge), new Edge());
                    ioGraph.setEdgeWeight(node, graph.getEdgeTarget(edge), edge.getWeight());
                }
            }
        }

        validateIOGraph();
    }

    private void validateIOGraph() {
        for (INode node : ioGraph.vertexSet()) {
            if (innerCliqueNodes.contains(node)) {
                Optional<INode> result = Optional.empty();
                for (IEdge edge : ioGraph.incomingEdgesOf(node)) {
                    INode edgeSource = ioGraph.getEdgeSource(edge);
                    if (innerCliqueNodes.contains(edgeSource)) {
                        result = Optional.of(edgeSource);
                        break;
                    }
                }
                result
                        .ifPresent(illegalNode -> {
                            throw new IllegalStateException("The build IO Graph contains a inner edge, which is illegal. Between: " + node + " and: " + illegalNode);
                        });

                Optional<INode> found = Optional.empty();
                for (IEdge iEdge : ioGraph.outgoingEdgesOf(node)) {
                    INode edgeTarget = ioGraph.getEdgeTarget(iEdge);
                    if (innerCliqueNodes.contains(edgeTarget)) {
                        found = Optional.of(edgeTarget);
                        break;
                    }
                }
                found
                        .ifPresent(illegalNode -> {
                            throw new IllegalStateException("The build IO Graph contains a inner edge, which is illegal. Between: " + node + " and: " + illegalNode);
                        });
            }
        }
    }

    @Override
    public String toString() {
        return "CliqueNode@" + Integer.toHexString(hashCode());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CliqueNode that = (CliqueNode) o;
        return hash == that.hash;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public Set<CompoundInstance> getInputInstances(final IRecipeNode recipeNode) {
        if (!ioGraph.containsVertex(recipeNode)) {
            return Collections.emptySet();
        }

        final Set<CompoundInstance> result = Sets.newHashSet();
        for (final IEdge iEdge : ioGraph.incomingEdgesOf(recipeNode)) {
            final INode source = ioGraph.getEdgeSource(iEdge);
            if (source instanceof IRecipeInputNode) {
                final IRecipeInputNode residueNode = (IRecipeInputNode) source;
                result.addAll(residueNode.getInputInstances(recipeNode));
            }
        }

        Set<CompoundInstance> compoundedResult = new HashSet<>();
        for (Collection<CompoundInstance> sameType : GroupingUtils.groupByUsingList(result, CompoundInstance::getType)) {
            if (!sameType.isEmpty()) {
                CompoundInstance instance = new CompoundInstance(sameType.iterator().next().getType(), sameType.stream().mapToDouble(CompoundInstance::getAmount).sum());
                compoundedResult.add(instance);
            }
        }
        return compoundedResult;
    }

    @Override
    public Set<IRecipeInputNode> getInputNodes(final IRecipeNode recipeNode) {
        if (!ioGraph.containsVertex(recipeNode)) {
            return Collections.emptySet();
        }

        final Set<IRecipeInputNode> result = Sets.newHashSet();
        for (final IEdge iEdge : ioGraph.incomingEdgesOf(recipeNode)) {
            final INode source = ioGraph.getEdgeSource(iEdge);
            if (source instanceof IRecipeInputNode) {
                final IRecipeInputNode residueNode = (IRecipeInputNode) source;
                result.addAll(residueNode.getInputNodes(recipeNode));
            }
        }

        return result;
    }

    @Override
    public Set<CompoundInstance> getResidueInstances(final IRecipeNode recipeNode) {
        if (!ioGraph.containsVertex(recipeNode)) {
            return Collections.emptySet();
        }

        final Set<CompoundInstance> result = Sets.newHashSet();
        for (final IEdge iEdge : ioGraph.incomingEdgesOf(recipeNode)) {
            final INode source = ioGraph.getEdgeSource(iEdge);
            if (source instanceof IRecipeResidueNode) {
                final IRecipeResidueNode residueNode = (IRecipeResidueNode) source;
                result.addAll(residueNode.getResidueInstances(recipeNode));
            }
        }

        Set<CompoundInstance> compoundedResult = new HashSet<>();
        for (Collection<CompoundInstance> sameType : GroupingUtils.groupByUsingList(result, CompoundInstance::getType)) {
            if (!sameType.isEmpty()) {
                CompoundInstance instance = new CompoundInstance(sameType.iterator().next().getType(), sameType.stream().mapToDouble(CompoundInstance::getAmount).sum());
                compoundedResult.add(instance);
            }
        }
        return compoundedResult;
    }
}
