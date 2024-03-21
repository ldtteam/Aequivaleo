package com.ldtteam.aequivaleo.analysis.jgrapht.cycles;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth.CrossComponentDepthMapBuilder;
import com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth.FullScanDepthMapBuilder;
import com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth.IDepthMapBuilder;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import com.ldtteam.aequivaleo.utils.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;
import org.jgrapht.alg.cycle.DirectedSimpleCycles;

import java.util.*;
import java.util.function.BiFunction;

public abstract class AbstractJGraphTDirectedCyclesReducer implements ICyclesReducer {
    private static final Logger LOGGER = LogManager.getLogger();

    private final BiFunction<IGraph, List<INode>, INode> vertexReplacerFunction;
    private final TriConsumer<INode, INode, INode> onNeighborNodeReplacedCallback;
    private final boolean reduceSingularCycle;

    public AbstractJGraphTDirectedCyclesReducer(
            final BiFunction<IGraph, List<INode>, INode> vertexReplacerFunction,
            final TriConsumer<INode, INode, INode> onNeighborNodeReplacedCallback) {
        this(vertexReplacerFunction, onNeighborNodeReplacedCallback, true);
    }

    public AbstractJGraphTDirectedCyclesReducer(
            final BiFunction<IGraph, List<INode>, INode> vertexReplacerFunction,
            final TriConsumer<INode, INode, INode> onNeighborNodeReplacedCallback,
            final boolean reduceSingularCycle) {
        this.vertexReplacerFunction = vertexReplacerFunction;
        this.onNeighborNodeReplacedCallback = onNeighborNodeReplacedCallback;
        this.reduceSingularCycle = reduceSingularCycle;
    }

    @Override
    public void reduce(final IGraph graph, INode startNode) {
        //Unit testing has shown that this is enough
        //Saves a recompilation of the cycle detection graph.
        reduceOnce(graph, startNode);
    }

    @SuppressWarnings("DuplicatedCode")
    @VisibleForTesting
    public boolean reduceOnce(final IGraph graph, INode startNode) {
        AnalysisLogHandler.debug(LOGGER, "Reducing the graph");

        final DirectedSimpleCycles<INode, IEdge> cycleFinder = createCycleDetector(graph);
        List<List<INode>> sortedCycles = cycleFinder.findSimpleCycles();

        Set<List<INode>> uniqueValues = new HashSet<>(sortedCycles);
        sortedCycles = new ArrayList<>(uniqueValues);

        if (sortedCycles.isEmpty() || (sortedCycles.size() == 1 && !reduceSingularCycle))
        {
            AnalysisLogHandler.debug(LOGGER, " > Reducing skipped.");
            return false;
        }

        final IDepthMapBuilder depthMapBuilder = new FullScanDepthMapBuilder(graph, startNode);
        final Map<INode, Integer> depthMap = depthMapBuilder.calculateDepthMap();

        //Sort by size, if size is equal sort by depth. The deeper the earlier the replacement.
        sortedCycles.sort(Comparator.<List<INode>>comparingInt(List::size)
                .thenComparingInt(cycle -> Integer.MAX_VALUE - cycle.stream().mapToInt(depthMap::get).max().orElse(0)));

        while(!sortedCycles.isEmpty()) {
            final List<INode> cycle = sortedCycles.remove(0);
            if (cycle.isEmpty())
                continue;

            AnalysisLogHandler.debug(LOGGER, String.format(" > Removing cycle: %s", cycle));

            final INode replacementNode = vertexReplacerFunction.apply(graph, cycle);

            updateRemainingCyclesAfterReplacement(
                    sortedCycles,
                    cycle,
                    replacementNode
            );

            final Map<IEdge, INode> incomingEdges = Maps.newHashMap();
            final Map<IEdge, INode> outgoingEdges = Maps.newHashMap();
            final Multimap<INode, IEdge> incomingEdgesTo = HashMultimap.create();
            final Multimap<INode, IEdge> incomingEdgesOf = HashMultimap.create();
            final Multimap<INode, IEdge> outgoingEdgesOf = HashMultimap.create();
            final Multimap<INode, IEdge> outgoingEdgesTo = HashMultimap.create();

            //Collect all the edges which are relevant to keep.
            for (INode v : cycle)
            {
                for (IEdge e : graph.incomingEdgesOf(v))
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

                for (IEdge edge : graph.outgoingEdgesOf(v))
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

            AnalysisLogHandler.debug(LOGGER, String.format("  > Detected: %s as incoming edges to keep.", incomingEdges));
            AnalysisLogHandler.debug(LOGGER, String.format("  > Detected: %s as outgoing edges to keep.", outgoingEdges));

            //Create the new cycle construct.
            graph.addVertex(replacementNode);
            for (INode incomingSource : incomingEdgesOf.keySet())
            {
                double newEdgeWeight = 0.0;
                for (IEdge e : incomingEdgesOf.get(incomingSource))
                {
                    double weight = e.getWeight();
                    newEdgeWeight += weight;
                }
                graph.addEdge(incomingSource, replacementNode);
                graph.setEdgeWeight(incomingSource, replacementNode, newEdgeWeight);
            }
            for (INode outgoingTarget : outgoingEdgesTo.keySet())
            {
                double newEdgeWeight = 0.0;
                for (IEdge e : outgoingEdgesTo.get(outgoingTarget))
                {
                    double weight = e.getWeight();
                    newEdgeWeight += weight;
                }
                graph.addEdge(replacementNode, outgoingTarget);
                graph.setEdgeWeight(replacementNode, outgoingTarget, newEdgeWeight);
            }

            graph.removeAllVertices(cycle);

            incomingEdgesTo.forEach((cycleNode, edge) -> onNeighborNodeReplacedCallback.accept(incomingEdges.get(edge), cycleNode, replacementNode));
            outgoingEdgesOf.forEach((cycleNode, edge) -> onNeighborNodeReplacedCallback.accept(outgoingEdges.get(edge), cycleNode, replacementNode));

            AnalysisLogHandler.debug(LOGGER, String.format(" > Removed cycle: %s", cycle));
        }

        return true;
    }

    protected abstract DirectedSimpleCycles<INode, IEdge> createCycleDetector(IGraph graph);

    private void updateRemainingCyclesAfterReplacement(final List<List<INode>> cycles, final List<INode> replacedCycle, final INode replacementNode) {
        final Set<INode> replacedNodes = new HashSet<>(replacedCycle);

        for (List<INode> cycle : cycles)
        {
            updateCycleWithReplacement(replacedNodes, replacementNode, cycle);
        }
    }

    private void updateCycleWithReplacement(final Set<INode> replacedCycle, final INode replacementNode, final List<INode> cycle)
    {
        ListUtils.replaceAllInWidthWithoutRepetitions(cycle, replacedCycle, replacementNode);
    }
}
