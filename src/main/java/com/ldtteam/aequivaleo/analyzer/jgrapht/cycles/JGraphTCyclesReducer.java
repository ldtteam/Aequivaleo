package com.ldtteam.aequivaleo.analyzer.jgrapht.cycles;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.DirectedSimpleCycles;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class JGraphTCyclesReducer<V, E>
{

    private static final Logger LOGGER = LogManager.getLogger();

    private final BiFunction<Graph<V, E>, List<V>, V> vertexReplacerFunction;
    private final TriConsumer<Graph<V, E>, E, E>      replacementEdgeAddedCallback;
    private final TriConsumer<V, V, V> onNeighborNodeReplacedCallback;

    public JGraphTCyclesReducer(
      final BiFunction<Graph<V, E>, List<V>, V> vertexReplacerFunction,
      final TriConsumer<Graph<V, E>, E, E> replacementEdgeAddedCallback,
      final TriConsumer<V, V, V> onNeighborNodeReplacedCallback) {
        this.vertexReplacerFunction = vertexReplacerFunction;
        this.replacementEdgeAddedCallback = replacementEdgeAddedCallback;
        this.onNeighborNodeReplacedCallback = onNeighborNodeReplacedCallback;
    }

    public void reduce(final Graph<V, E> graph) {
        while(reduceOnce(graph)) {
            //Noop we keep on doing this until the graph is cycle free.
        }
    }

    @VisibleForTesting
    public boolean reduceOnce(final Graph<V, E> graph) {
        LOGGER.debug("Reducing the graph");

        final DirectedSimpleCycles<V, E> cycleFinder = new HawickJamesSimpleCycles<>(graph);
        List<List<V>> sortedCycles = cycleFinder.findSimpleCycles();

        if (sortedCycles.isEmpty())
        {
            LOGGER.debug(" > Reducing skipped. Graph is cycle free.");
            return false;
        }

        sortedCycles.sort(Comparator.comparing(List::size));

        while(!sortedCycles.isEmpty()) {
            final List<V> cycle = sortedCycles.get(0);

            LOGGER.debug(String.format(" > Removing cycle: %s", cycle));

            final V replacementNode = vertexReplacerFunction.apply(graph, cycle);

            updateRemainingCyclesAfterReplacement(
              sortedCycles,
              cycle,
              replacementNode
            );

            final Map<E, V> incomingEdges = Maps.newHashMap();
            final Map<E, V> outgoingEdges = Maps.newHashMap();
            final Multimap<V, E> incomingEdgesTo = HashMultimap.create();
            final Multimap<V, E> outgoingEdgesOf = HashMultimap.create();

            //Collect all the edges which are relevant to keep.
            cycle.forEach(cycleNode -> {
                graph.incomingEdgesOf(cycleNode)
                  .stream()
                  .filter(edge -> !cycle.contains(graph.getEdgeSource(edge)))
                  .peek(edge -> incomingEdgesTo.put(cycleNode, edge))
                  .forEach(edge -> incomingEdges.put(edge, graph.getEdgeSource(edge)));

                graph.outgoingEdgesOf(cycleNode)
                  .stream()
                  .filter(edge -> !cycle.contains(graph.getEdgeTarget(edge)))
                  .peek(edge -> outgoingEdgesOf.put(cycleNode, edge))
                  .forEach(edge -> outgoingEdges.put(edge, graph.getEdgeTarget(edge)));
            });

            incomingEdges.keySet().forEach(outgoingEdges::remove);

            LOGGER.debug(String.format("  > Detected: %s as incoming edges to keep.", incomingEdges));
            LOGGER.debug(String.format("  > Detected: %s as outgoing edges to keep.", outgoingEdges));

            //Create the new cycle construct.
            graph.addVertex(replacementNode);
            incomingEdges.forEach((incomingEdge, sourceNode) -> {
                final E newIncomingEdge = graph.addEdge(sourceNode, replacementNode);

                replacementEdgeAddedCallback.accept(graph, incomingEdge, newIncomingEdge);

                graph.removeEdge(incomingEdge);
            });
            outgoingEdges.forEach((outgoingEdge, targetNode) -> {
                final E newOutgoingEdge = graph.addEdge(replacementNode, targetNode);

                replacementEdgeAddedCallback.accept(graph, outgoingEdge, newOutgoingEdge);

                graph.removeEdge(outgoingEdge);

            });

            graph.removeAllVertices(cycle);

            incomingEdgesTo.forEach((cycleNode, edge) -> onNeighborNodeReplacedCallback.accept(incomingEdges.get(edge), cycleNode, replacementNode));
            outgoingEdgesOf.forEach((cycleNode, edge) -> onNeighborNodeReplacedCallback.accept(outgoingEdges.get(edge), cycleNode, replacementNode));

            LOGGER.debug(String.format(" > Removed cycle: %s", cycle));
        }

        return true;
    }

    private void updateRemainingCyclesAfterReplacement(final List<List<V>> cycles, final List<V> replacedCycle, final V replacementNode) {
        cycles.remove(replacedCycle);
        cycles.forEach(cycle -> {
            final List<V> intersectingNodes = cycle.stream().filter(replacedCycle::contains).collect(Collectors.toList());
            intersectingNodes.forEach(intersectingNode -> {
                final int nodeIndex = cycle.indexOf(intersectingNode);
                cycle.remove(nodeIndex);
                cycle.add(nodeIndex, replacementNode);
            });
        });
    }
}
