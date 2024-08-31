package com.ldtteam.aequivaleo.analysis.jgrapht.clique.reducer;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IContainerNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.clique.graph.CliqueDetectionEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.clique.graph.CliqueDetectionGraph;
import org.jgrapht.Graph;

import java.util.*;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.Collectors;

public abstract class ReductionGraphBuilder<G extends Graph<V, E>, V, E> {

    public final CliqueDetectionGraph<V> reduce(G graph) {
        final CliqueDetectionGraph<V> reductionGraph = new CliqueDetectionGraph<>();

        for (V vertex : graph.vertexSet()) {
            if (isRelevantNode(vertex)) {
                reductionGraph.addVertex(vertex);
                for (RelevantNodeRoute<V> route : relevantNodesOf(graph, vertex)) {
                    final V target = route.route().get(route.route().size() - 1);
                    if (vertex.equals(target)) {
                        //This happens when a recipe has an input that is the same as its output (for example trapped chest + hook -> trapped chest)
                        continue;
                    }

                    reductionGraph.addVertex(target);

                    //Remove the target and the source from the route
                    route.route().removeFirst();
                    route.route().removeLast();

                    //Final list of intermediary nodes
                    final List<V> intermediaryNodes = new ArrayList<>(route.route());

                    if (reductionGraph.containsEdge(vertex, target)) {
                        final CliqueDetectionEdge<V> edge = reductionGraph.getEdge(vertex, target);
                        intermediaryNodes.addAll(edge.getIntermediaryNodes());

                        reductionGraph.removeEdge(vertex, target);
                    }

                    final CliqueDetectionEdge<V> edge = new CliqueDetectionEdge<>(intermediaryNodes);
                    reductionGraph.addEdge(vertex, target, edge);
                }
            }
        }

        return reductionGraph;
    }

    private record RelevantNodeRoute<V>(double totalWeight, LinkedList<Double> weights, LinkedList<V> route) {
        public RelevantNodeRoute() {
            this(0, new LinkedList<>(), new LinkedList<>());
        }

        public RelevantNodeRoute(double totalWeight, V start, V next) {
            this(totalWeight, List.of(totalWeight), List.of(start, next));
        }

        public RelevantNodeRoute(List<Double> totalWeight, List<V> route) {
            this(totalWeight.stream().mapToDouble(Double::doubleValue).reduce(1d, (left, right) -> left * right), new LinkedList<>(totalWeight), route);
        }

        public RelevantNodeRoute(double totalWeight, List<Double> weights, List<V> route) {
            this(totalWeight, new LinkedList<>(weights), new LinkedList<>(route));
        }
    }

    private Collection<RelevantNodeRoute<V>> relevantNodesOf(G graph, V vertex) {
        final List<RelevantNodeRoute<V>> result = new ArrayList<>();
        final Deque<RelevantNodeRoute<V>> queue = graph.outgoingEdgesOf(vertex)
                .stream()
                .map(e -> new RelevantNodeRoute<>(
                        graph.getEdgeWeight(e),
                        vertex,
                        graph.getEdgeTarget(e)
                ))
                .collect(Collectors.toCollection(ArrayDeque::new));

        while (!queue.isEmpty()) {
            final RelevantNodeRoute<V> route = queue.poll();
            final double edgeWeight = route.totalWeight();
            final V target = route.route().get(route.route().size() - 1);

            if (isRelevantNode(target)) {
                result.add(route);
            } else {
                for (E e : graph.outgoingEdgesOf(target)) {
                    if (graph.getEdgeWeight(e) == edgeWeight) {
                        final V next = graph.getEdgeTarget(e);
                        final LinkedList<Double> nextWeights = new LinkedList<>(route.weights());
                        final LinkedList<V> nextRoute = new LinkedList<>(route.route());
                        nextWeights.add(edgeWeight);
                        nextRoute.add(next);
                        queue.add(new RelevantNodeRoute<>(nextWeights, nextRoute));
                    }
                }
            }
        }

        return result;
    }

    protected abstract boolean isRelevantNode(V node);

    public static final class RecipeNodeReducer extends ReductionGraphBuilder<IGraph, INode, IEdge> {

        @Override
        protected boolean isRelevantNode(INode node) {
            return node instanceof IContainerNode;
        }
    }
}
