package com.ldtteam.aequivaleo.analysis.jgrapht.cycles;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth.FullScanDepthMapBuilder;
import com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth.IDepthMapBuilder;
import com.ldtteam.aequivaleo.api.util.AequivaleoLogger;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import com.ldtteam.aequivaleo.utils.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.DirectedSimpleCycles;

import java.util.*;
import java.util.function.BiFunction;

public abstract class AbstractJGraphTDirectedCyclesReducer<G extends Graph<N, E>, N, E> implements ICyclesReducer<G, N, E> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final BiFunction<G, List<N>, N> vertexReplacerFunction;
    private final TriConsumer<N, N, N> onNeighborNodeReplacedCallback;
    private final boolean reduceSingularCycle;

    public AbstractJGraphTDirectedCyclesReducer(
            final BiFunction<G, List<N>, N> vertexReplacerFunction,
            final TriConsumer<N, N, N> onNeighborNodeReplacedCallback) {
        this(vertexReplacerFunction, onNeighborNodeReplacedCallback, true);
    }

    public AbstractJGraphTDirectedCyclesReducer(
            final BiFunction<G, List<N>, N> vertexReplacerFunction,
            final TriConsumer<N, N, N> onNeighborNodeReplacedCallback,
            final boolean reduceSingularCycle) {
        this.vertexReplacerFunction = vertexReplacerFunction;
        this.onNeighborNodeReplacedCallback = onNeighborNodeReplacedCallback;
        this.reduceSingularCycle = reduceSingularCycle;
    }

    @Override
    public void reduce(final G graph, N startNode) {
        //Unit testing has shown that this is enough
        //Saves a recompilation of the cycle detection graph.
        reduceOnce(graph, startNode);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public boolean reduceOnce(final G graph, N startNode) {
        AnalysisLogHandler.debug(LOGGER, "Reducing the graph");

        List<List<N>> sortedCycles = detectCycles(graph);
        if (sortedCycles.isEmpty() || (sortedCycles.size() == 1 && !reduceSingularCycle))
        {
            AnalysisLogHandler.debug(LOGGER, " > Reducing skipped.");
            return false;
        }

        Map<N, Integer> depthMap = buildCycleProcesingDepthMap(graph, startNode);

        sortCycles(sortedCycles, depthMap);

        int updateCount = sortedCycles.size() / 100;

        while(!sortedCycles.isEmpty()) {
            if (updateCount-- == 0) {
                AequivaleoLogger.getLogger().info(String.format(" > Remaining cycles: %d", sortedCycles.size()));
                updateCount = sortedCycles.size() > 100 ? sortedCycles.size() / 100 : 100;
            }
            final List<N> cycle = sortedCycles.remove(0);
            final CycleProcessResult<N> result = processCycle(graph, cycle, sortedCycles);
            sortedCycles = result.newCycles();
            if (result.requiresSorting()) {
                depthMap = buildCycleProcesingDepthMap(graph, startNode);
                sortCycles(sortedCycles, depthMap);
            }
        }

        return true;
    }

    @VisibleForTesting
    CycleProcessResult<N> processCycle(G graph, List<N> cycle, List<List<N>> sortedCycles) {
        if (cycle.isEmpty())
            return new CycleProcessResult<>(false, sortedCycles);

        AnalysisLogHandler.debug(LOGGER, String.format(" > Removing cycle: %s", cycle));

        final N replacementNode = vertexReplacerFunction.apply(graph, cycle);

        final CycleProcessResult<N> newCycles = updateRemainingCyclesAfterReplacement(
                sortedCycles,
                cycle,
                replacementNode
        );

        EdgeReplacementInformation<N, E> result = detectEdgeReplacement(graph, cycle);

        AnalysisLogHandler.debug(LOGGER, String.format("  > Detected: %s as incoming edges to keep.", result.incomingEdges()));
        AnalysisLogHandler.debug(LOGGER, String.format("  > Detected: %s as outgoing edges to keep.", result.outgoingEdges()));

        //Create the new cycle construct.
        replaceCycle(graph, cycle, replacementNode, result);

        AnalysisLogHandler.debug(LOGGER, String.format(" > Removed cycle: %s", cycle));

        return newCycles;
    }

    @VisibleForTesting
    void replaceCycle(G graph, List<N> cycle, N replacementNode, EdgeReplacementInformation<N, E> result) {
        graph.addVertex(replacementNode);
        for (N incomingSource : result.incomingEdgesOf().keySet())
        {
            double newEdgeWeight = 0.0;
            for (E e : result.incomingEdgesOf().get(incomingSource))
            {
                double weight = graph.getEdgeWeight(e);
                newEdgeWeight += weight;
            }
            graph.addEdge(incomingSource, replacementNode);
            graph.setEdgeWeight(incomingSource, replacementNode, newEdgeWeight);
        }
        for (N outgoingTarget : result.outgoingEdgesTo().keySet())
        {
            double newEdgeWeight = 0.0;
            for (E e : result.outgoingEdgesTo().get(outgoingTarget))
            {
                double weight = graph.getEdgeWeight(e);
                newEdgeWeight += weight;
            }
            graph.addEdge(replacementNode, outgoingTarget);
            graph.setEdgeWeight(replacementNode, outgoingTarget, newEdgeWeight);
        }

        graph.removeAllVertices(cycle);

        result.incomingEdgesTo().forEach((cycleNode, edge) -> onNeighborNodeReplacedCallback.accept(result.incomingEdges().get(edge), cycleNode, replacementNode));
        result.outgoingEdgesOf().forEach((cycleNode, edge) -> onNeighborNodeReplacedCallback.accept(result.outgoingEdges().get(edge), cycleNode, replacementNode));
    }

    @VisibleForTesting
    @NotNull EdgeReplacementInformation<N, E> detectEdgeReplacement(G graph, List<N> cycle) {
        final Map<E, N> incomingEdges = Maps.newHashMap();
        final Map<E, N> outgoingEdges = Maps.newHashMap();
        final Multimap<N, E> incomingEdgesTo = HashMultimap.create();
        final Multimap<N, E> incomingEdgesOf = HashMultimap.create();
        final Multimap<N, E> outgoingEdgesOf = HashMultimap.create();
        final Multimap<N, E> outgoingEdgesTo = HashMultimap.create();

        //Collect all the edges which are relevant to keep.
        for (N v : cycle)
        {
            for (E e : graph.incomingEdgesOf(v))
            {
                if (!cycle.contains(graph.getEdgeSource(e)))
                {
                    if (!incomingEdgesTo.containsEntry(v, e))
                    {
                        incomingEdgesTo.put(v, e);
                        incomingEdgesOf.put(graph.getEdgeSource(e), e);
                        incomingEdges.put(e, graph.getEdgeSource(e));
                    }
                }
            }

            for (E edge : graph.outgoingEdgesOf(v))
            {
                if (!cycle.contains(graph.getEdgeTarget(edge)))
                {
                    if (!outgoingEdgesOf.containsEntry(v, edge))
                    {
                        outgoingEdgesOf.put(v, edge);
                        outgoingEdgesTo.put(graph.getEdgeTarget(edge), edge);
                        outgoingEdges.put(edge, graph.getEdgeTarget(edge));
                    }
                }
            }
        }
        return new EdgeReplacementInformation<>(incomingEdges, outgoingEdges, incomingEdgesTo, incomingEdgesOf, outgoingEdgesOf, outgoingEdgesTo);
    }

    private record EdgeReplacementInformation<N, E>(Map<E, N> incomingEdges, Map<E, N> outgoingEdges, Multimap<N, E> incomingEdgesTo, Multimap<N, E> incomingEdgesOf, Multimap<N, E> outgoingEdgesOf, Multimap<N, E> outgoingEdgesTo) {
    }

    private void sortCycles(List<List<N>> sortedCycles, Map<N, Integer> depthMap) {
        //Sort by size, if size is equal sort by depth. The deeper the earlier the replacement.
        sortedCycles.sort(Comparator.<List<N>>comparingInt(List::size)
                .thenComparingInt(cycle -> Integer.MAX_VALUE - cycle.stream().mapToInt(depthMap::get).max().orElse(0)));
    }

    private Map<N, Integer> buildCycleProcesingDepthMap(G graph, N startNode) {
        final IDepthMapBuilder<N> depthMapBuilder = new FullScanDepthMapBuilder<>(graph, startNode);
        return depthMapBuilder.calculateDepthMap();
    }

    private @NotNull List<List<N>> detectCycles(G graph) {
        final DirectedSimpleCycles<N, E> cycleFinder = createCycleDetector(graph);
        List<List<N>> sortedCycles = cycleFinder.findSimpleCycles();

        Set<List<N>> uniqueValues = new HashSet<>(sortedCycles);
        sortedCycles = new ArrayList<>(uniqueValues);
        return sortedCycles;
    }

    protected abstract DirectedSimpleCycles<N, E> createCycleDetector(G graph);

    private CycleProcessResult<N> updateRemainingCyclesAfterReplacement(final List<List<N>> cycles, final List<N> replacedCycle, final N replacementNode) {
        final Set<N> replacedNodes = new HashSet<>(replacedCycle);

        final Set<List<N>> newCycles = new LinkedHashSet<>();
        boolean requiresSorting = false;
        for (List<N> cycle : cycles)
        {
            final ListUtils.ReplacementResult<N> result = updateCycleWithReplacement(replacedNodes, replacementNode, cycle);
            newCycles.addAll(result.newCycles());
            requiresSorting |= (result.sizeChanged() || result.newCycles().size() > 1);
        }

        return new CycleProcessResult<>(requiresSorting, new ArrayList<>(newCycles));
    }

    private ListUtils.ReplacementResult<N> updateCycleWithReplacement(final Set<N> replacedCycle, final N replacementNode, final List<N> cycle)
    {
        return ListUtils.rebuildCycleList(cycle, replacedCycle, replacementNode);
    }

    private record CycleProcessResult<N>(boolean requiresSorting, List<List<N>> newCycles) {}
}
