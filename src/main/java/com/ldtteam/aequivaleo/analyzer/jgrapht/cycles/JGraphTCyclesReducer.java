package com.ldtteam.aequivaleo.analyzer.jgrapht.cycles;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisEdge;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.DirectedSimpleCycles;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class JGraphTCyclesReducer<G extends Graph<V, E>, V, E extends IAnalysisEdge>
{

    private static final Logger LOGGER = LogManager.getLogger();

    private final BiFunction<G, List<V>, V> vertexReplacerFunction;
    private final TriConsumer<V, V, V> onNeighborNodeReplacedCallback;
    private final boolean reduceSingularCycle;

    public JGraphTCyclesReducer(
      final BiFunction<G, List<V>, V> vertexReplacerFunction,
      final TriConsumer<V, V, V> onNeighborNodeReplacedCallback) {
        this(vertexReplacerFunction, onNeighborNodeReplacedCallback, true);
    }

    public JGraphTCyclesReducer(
      final BiFunction<G, List<V>, V> vertexReplacerFunction,
      final TriConsumer<V, V, V> onNeighborNodeReplacedCallback,
      final boolean reduceSingularCycle) {
        this.vertexReplacerFunction = vertexReplacerFunction;
        this.onNeighborNodeReplacedCallback = onNeighborNodeReplacedCallback;
        this.reduceSingularCycle = reduceSingularCycle;
    }

    public void reduce(final G graph) {
        //Unit testing has shown that this is enough
        //Saves a recompilation of the cycle detection graph.
        reduceOnce(graph);
    }

    @VisibleForTesting
    public boolean reduceOnce(final G graph) {
        AnalysisLogHandler.debug(LOGGER, "Reducing the graph");

        final DirectedSimpleCycles<V, E> cycleFinder = new HawickJamesSimpleCycles<>(graph);
        List<List<V>> sortedCycles = cycleFinder.findSimpleCycles();

        sortedCycles = sortedCycles.stream().distinct().collect(Collectors.toList());

        if (sortedCycles.isEmpty() || (sortedCycles.size() == 1 && !reduceSingularCycle))
        {
            AnalysisLogHandler.debug(LOGGER, " > Reducing skipped.");
            return false;
        }

        sortedCycles.sort(Comparator.comparing(List::size));

        while(!sortedCycles.isEmpty()) {
            final List<V> cycle = sortedCycles.get(0);

            AnalysisLogHandler.debug(LOGGER, String.format(" > Removing cycle: %s", cycle));

            final V replacementNode = vertexReplacerFunction.apply(graph, cycle);

            sortedCycles = updateRemainingCyclesAfterReplacement(
              sortedCycles,
              cycle,
              replacementNode
            );

            final Map<E, V> incomingEdges = Maps.newHashMap();
            final Map<E, V> outgoingEdges = Maps.newHashMap();
            final Multimap<V, E> incomingEdgesTo = HashMultimap.create();
            final Multimap<V, E> incomingEdgesOf = HashMultimap.create();
            final Multimap<V, E> outgoingEdgesOf = HashMultimap.create();
            final Multimap<V, E> outgoingEdgesTo = HashMultimap.create();

            //Collect all the edges which are relevant to keep.
            cycle.forEach(cycleNode -> {
                graph.incomingEdgesOf(cycleNode)
                  .stream()
                  .filter(edge -> !cycle.contains(graph.getEdgeSource(edge)))
                  .filter(edge -> !incomingEdgesTo.containsEntry(cycleNode, edge))
                  .peek(edge -> incomingEdgesTo.put(cycleNode, edge))
                  .peek(edge -> incomingEdgesOf.put(graph.getEdgeSource(edge), edge))
                  .forEach(edge -> incomingEdges.put(edge, graph.getEdgeSource(edge)));

                graph.outgoingEdgesOf(cycleNode)
                  .stream()
                  .filter(edge -> !cycle.contains(graph.getEdgeTarget(edge)))
                  .filter(edge -> !outgoingEdgesOf.containsEntry(cycleNode, edge))
                  .peek(edge -> outgoingEdgesOf.put(cycleNode, edge))
                  .peek(edge -> outgoingEdgesTo.put(graph.getEdgeTarget(edge), edge))
                  .forEach(edge -> outgoingEdges.put(edge, graph.getEdgeTarget(edge)));
            });

            incomingEdges.keySet().forEach(outgoingEdges::remove);

            AnalysisLogHandler.debug(LOGGER, String.format("  > Detected: %s as incoming edges to keep.", incomingEdges));
            AnalysisLogHandler.debug(LOGGER, String.format("  > Detected: %s as outgoing edges to keep.", outgoingEdges));

            //Create the new cycle construct.
            graph.addVertex(replacementNode);
            incomingEdgesOf.keySet()
                .forEach(incomingSource -> {
                    final double newEdgeWeight = incomingEdgesOf.get(incomingSource).stream().mapToDouble(IAnalysisEdge::getWeight).sum();
                    graph.addEdge(incomingSource, replacementNode);
                    graph.setEdgeWeight(incomingSource, replacementNode, newEdgeWeight);
                });
            outgoingEdgesTo.keySet()
                .forEach(outgoingTarget -> {
                    final double newEdgeWeight = outgoingEdgesTo.get(outgoingTarget).stream().mapToDouble(IAnalysisEdge::getWeight).sum();
                    graph.addEdge(replacementNode, outgoingTarget);
                    graph.setEdgeWeight(replacementNode, outgoingTarget, newEdgeWeight);
                });

            graph.removeAllVertices(cycle);

            incomingEdgesTo.forEach((cycleNode, edge) -> onNeighborNodeReplacedCallback.accept(incomingEdges.get(edge), cycleNode, replacementNode));
            outgoingEdgesOf.forEach((cycleNode, edge) -> onNeighborNodeReplacedCallback.accept(outgoingEdges.get(edge), cycleNode, replacementNode));

            AnalysisLogHandler.debug(LOGGER, String.format(" > Removed cycle: %s", cycle));
        }

        return true;
    }

    private List<List<V>> updateRemainingCyclesAfterReplacement(final List<List<V>> cycles, final List<V> replacedCycle, final V replacementNode) {
        cycles.remove(replacedCycle);

        return cycles.stream()
          .map(cycle -> {
              final List<V> intersectingNodes = cycle.stream().filter(replacedCycle::contains).collect(Collectors.toList());
              intersectingNodes.forEach(intersectingNode -> {
                  AnalysisLogHandler.debug(LOGGER, "    > Replacing: " + intersectingNode + " with: " + replacementNode);
                  final int nodeIndex = cycle.indexOf(intersectingNode);
                  cycle.remove(nodeIndex);
                  cycle.add(nodeIndex, replacementNode);
              });

              List<V> result = new ArrayList<>();
              V lastAdded = null;
              for (int i = 0; i < cycle.size(); i++)
              {
                  final V v = cycle.get(i);
                  if (!v.equals(lastAdded) && (i != cycle.size() -1 || cycle.get(0) != v))
                  {
                      lastAdded = v;
                      result.add(lastAdded);
                  }
              }
              return result;

          })
          .filter(cycle -> cycle.size() > 1)
          .sorted(Comparator.comparing(List::size))
          .collect(Collectors.toList());
    }
}
