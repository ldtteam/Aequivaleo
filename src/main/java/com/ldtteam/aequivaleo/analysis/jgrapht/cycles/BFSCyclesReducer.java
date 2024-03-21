package com.ldtteam.aequivaleo.analysis.jgrapht.cycles;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.jgrapht.Graphs;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;
import java.util.function.BiFunction;

public class BFSCyclesReducer implements ICyclesReducer {
    private static final Logger LOGGER = LogManager.getLogger();

    private final BiFunction<IGraph, List<INode>, INode> vertexReplacerFunction;
    private final TriConsumer<INode, INode, INode> onNeighborNodeReplacedCallback;
    private final boolean reduceSingularCycle;

    public BFSCyclesReducer(BiFunction<IGraph, List<INode>, INode> vertexReplacerFunction, TriConsumer<INode, INode, INode> onNeighborNodeReplacedCallback, boolean reduceSingularCycle) {
        this.vertexReplacerFunction = vertexReplacerFunction;
        this.onNeighborNodeReplacedCallback = onNeighborNodeReplacedCallback;
        this.reduceSingularCycle = reduceSingularCycle;
    }

    public BFSCyclesReducer(
            final BiFunction<IGraph, List<INode>, INode> vertexReplacerFunction,
            final TriConsumer<INode, INode, INode> onNeighborNodeReplacedCallback) {
        this(vertexReplacerFunction, onNeighborNodeReplacedCallback, true);
    }

    @Override
    public void reduce(IGraph graph, INode startNode) {
        if (!reduceOnce(graph, startNode) && !reduceSingularCycle) {
            return;
        }

        int count = 10;
        final CycleDetector<INode, IEdge> cycleDetector = new CycleDetector<>(graph);
        while(cycleDetector.detectCycles() && count > 0) {
            if (!reduceOnce(graph, startNode) && !reduceSingularCycle) {
                return;
            }
            count--;
        }

        if (cycleDetector.detectCycles()) {
            throw new IllegalStateException("Failed to reduce all cycles!");
        }
    }

    @VisibleForTesting
    public boolean reduceOnce(IGraph graph, INode startNode) {
        final Multimap<INode, IEdge> pathTaken = ArrayListMultimap.create();
        final List<List<INode>> cycles = new ArrayList<>();
        final BreadthFirstIterator<INode, IEdge> iterator = new BreadthFirstIterator<>(graph) {
            @Override
            protected void encounterVertex(INode vertex, IEdge edge) {
                super.encounterVertex(vertex, edge);

                boolean isRoot = edge == null;
                if (!isRoot) {
                    final INode input = Graphs.getOppositeVertex(graph, edge, vertex);
                    final Collection<IEdge> upUntil = List.copyOf(pathTaken.get(input));
                    pathTaken.putAll(vertex, upUntil);
                    pathTaken.put(vertex, edge);
                } else if (graph.incomingEdgesOf(vertex).size() != 0) {
                    for (IEdge e : graph.incomingEdgesOf(vertex)) {
                        pathTaken.put(vertex, e);
                    }
                }
            }

            @Override
            protected void encounterVertexAgain(INode vertex, IEdge edge) {
                super.encounterVertexAgain(vertex, edge);
                final INode input = Graphs.getOppositeVertex(graph, edge, vertex);
                final List<IEdge> path = new ArrayList<>(pathTaken.get(input));
                Collections.reverse(path);

                final List<INode> cycle = new ArrayList<>();
                cycle.add(vertex);
                INode workingVertex = input;
                while(!workingVertex.equals(vertex)) {
                    if (path.size() == 0) {
                        INode finalWorkingVertex = workingVertex;
                        final Optional<List<INode>> containedCycle = cycles.stream()
                                .filter(c -> c.contains(finalWorkingVertex) && c.contains(vertex))
                                .findFirst();

                        if (containedCycle.isEmpty())
                            return;

                        final int workingVertexIndex = containedCycle.get().indexOf(finalWorkingVertex);
                        final int targetVertexIndex = containedCycle.get().indexOf(vertex);

                        final int startIndex = Math.min(workingVertexIndex, targetVertexIndex);
                        final int endIndex = Math.max(workingVertexIndex, targetVertexIndex);
                        final int length = endIndex - startIndex;

                        final List<INode> subCycle = containedCycle.get().subList(startIndex, endIndex);
                        cycle.addAll(subCycle);
                        break;
                    }

                    final IEdge workingEdge = path.remove(0);
                    cycle.add(workingVertex);

                    if (graph.getEdgeSource(workingEdge) != workingVertex && graph.getEdgeTarget(workingEdge) != workingVertex)
                        return;

                    workingVertex = Graphs.getOppositeVertex(graph, workingEdge, workingVertex);
                }

                cycles.add(cycle);
            }
        };

        while(iterator.hasNext()) { iterator.next(); }

        List<List<INode>> sortedCycles = new ArrayList<>(cycles);
        sortedCycles.sort(Comparator.comparing(List::size));

        List<List<INode>> list = new ArrayList<>();
        Set<List<INode>> uniqueValues = new HashSet<>();
        for (List<INode> sortedCycle : sortedCycles)
        {
            if (uniqueValues.add(sortedCycle))
            {
                list.add(sortedCycle);
            }
        }
        sortedCycles = list;

        if (sortedCycles.isEmpty() || (sortedCycles.size() == 1 && !reduceSingularCycle))
        {
            AnalysisLogHandler.debug(LOGGER, " > Reducing skipped.");
            return false;
        }

        sortedCycles.sort(Comparator.comparing(List::size));

        while(!sortedCycles.isEmpty()) {
            final List<INode> cycle = sortedCycles.get(0);

            AnalysisLogHandler.debug(LOGGER, String.format(" > Removing cycle: %s", cycle));

            final INode replacementNode = vertexReplacerFunction.apply(graph, cycle);

            sortedCycles = updateRemainingCyclesAfterReplacement(
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

    private List<List<INode>> updateRemainingCyclesAfterReplacement(final List<List<INode>> cycles, final List<INode> replacedCycle, final INode replacementNode) {
        cycles.remove(replacedCycle);

        List<List<INode>> list = new ArrayList<>();
        for (List<INode> cycle : cycles)
        {
            List<INode> vs = updateCycleWithReplacement(replacedCycle, replacementNode, cycle);
            if (vs.size() > 1)
            {
                list.add(vs);
            }
        }
        list.sort(Comparator.comparing(List::size));
        return list;
    }

    @NotNull
    private List<INode> updateCycleWithReplacement(final List<INode> replacedCycle, final INode replacementNode, final List<INode> cycle)
    {
        final List<INode> intersectingNodes = new ArrayList<>();
        for (INode v1 : cycle)
        {
            if (replacedCycle.contains(v1))
            {
                intersectingNodes.add(v1);
            }
        }
        for (INode intersectingNode : intersectingNodes)
        {
            AnalysisLogHandler.debug(LOGGER, "    > Replacing: " + intersectingNode + " with: " + replacementNode);
            final int nodeIndex = cycle.indexOf(intersectingNode);
            cycle.remove(nodeIndex);
            cycle.add(nodeIndex, replacementNode);
        }

        List<INode> result = new ArrayList<>();
        INode lastAdded = null;
        for (int i = 0; i < cycle.size(); i++)
        {
            final INode v = cycle.get(i);
            if (!v.equals(lastAdded) && (i != cycle.size() -1 || cycle.get(0) != v))
            {
                lastAdded = v;
                result.add(lastAdded);
            }
        }
        return result;
    }
}
