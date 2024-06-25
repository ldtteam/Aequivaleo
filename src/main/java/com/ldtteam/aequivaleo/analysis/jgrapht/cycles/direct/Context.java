package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct;

import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search.CleanVertex;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search.ISearchAction;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search.VisitVertex;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.trace.ICycleReducingTracer;
import org.jgrapht.Graph;

import java.util.*;
import java.util.function.Function;

public final class Context<G extends Graph<V, E>, V, E> {
    private final G graph;

    private final Function<List<V>, V> vertexReplacerFunction;

    private final ICycleReducingTracer<G, V, E> tracer;

    private final Path<V> path = new Path<>();
    private final Deque<ISearchAction<G, V, E>> next = new ArrayDeque<>();

    private final Set<V> visitedVertexes = new HashSet<>();

    Context(G graph, V startingNode, Function<List<V>, V> replacementVertexBuilder, ICycleReducingTracer<G, V, E> tracer) {
        this.graph = graph;
        this.vertexReplacerFunction = replacementVertexBuilder;
        this.tracer = tracer;

        offerAction(new VisitVertex<>(startingNode, graph));
    }

    public Graph<V, E> getGraph() {
        return graph;
    }

    public void onCycleFound(V cycleCreatingNode) {
        final List<V> cycle = path.getSubPathUntil(cycleCreatingNode);

        this.tracer.onCycleFound(cycle);

        final V replacementNode = vertexReplacerFunction.apply(cycle);

        final Set<V> cycleSet = new HashSet<>(cycle);
        if (cycleSet.size() != cycle.size()) {
            throw new IllegalStateException("Cycle contains duplicates");
        }

        final Set<E> edgesToKeep = new HashSet<>();
        for (V v : cycle) {
            graph.outgoingEdgesOf(v).stream()
                    .filter(edge -> !cycleSet.contains(graph.getEdgeTarget(edge)))
                    .forEach(edgesToKeep::add);
            graph.incomingEdgesOf(v).stream()
                    .filter(edge -> !cycleSet.contains(graph.getEdgeSource(edge)))
                    .forEach(edgesToKeep::add);
        }

        final Deque<ISearchAction<G, V, E>> actionsTouchingCycle = new ArrayDeque<>();
        for (ISearchAction<G, V, E> gveiSearchAction : this.next) {
            if (gveiSearchAction.actionTouchesAnyOf(cycleSet)) {
                actionsTouchingCycle.add(gveiSearchAction);
            }
        }

        this.next.removeAll(actionsTouchingCycle);

        graph.addVertex(replacementNode);

        for (E edge : edgesToKeep) {
            final V source = graph.getEdgeSource(edge);
            final V target = graph.getEdgeTarget(edge);

            if (cycleSet.contains(source)) {
                addEdgeOrUpdateWeight(replacementNode, target, graph.getEdgeWeight(edge));
            } else if (cycleSet.contains(target)) {
                addEdgeOrUpdateWeight(source, replacementNode, graph.getEdgeWeight(edge));
            } else {
                throw new IllegalStateException("Edge part of cycle");
            }

            graph.removeEdge(edge);
        }

        cycle.forEach(graph::removeVertex);

        offerAction(new VisitVertex<>(replacementNode, graph));

        final List<V> cycleCopy = new ArrayList<>(cycle);
        Collections.reverse(cycleCopy);
        cycleCopy.forEach(path::clean);
    }

    private void addEdgeOrUpdateWeight(V source, V target, double weight) {
        final E edge = graph.getEdge(source, target);
        if (edge == null) {
            final E newEdge = graph.addEdge(source, target);
            graph.setEdgeWeight(newEdge, weight);
        } else {
            final double currentWeight = graph.getEdgeWeight(edge);
            graph.setEdgeWeight(edge, currentWeight + weight);
        }
    }

    public Path<V> getPath() {
        return path;
    }

    public void openVertex(V vertex) {
        if (vertex.toString().equals("CliqueNode{nodes=[ContainerNode{contents=1.0 x Item: minecraft:raw_gold_block}, IngredientNode{ingredient=SimpleIngredient{candidates=[1.0 x Item: minecraft:raw_gold_block], count=1.0}}, RecipeNode{recipe=Equivalency via Instance: raw_gold_block to: 1 raw_gold_block}, ContainerNode{contents=1.0 x ItemStack: 1 raw_gold_block}, IngredientNode{ingredient=SimpleIngredient{candidates=[1.0 x ItemStack: 1 raw_gold_block], count=1.0}}, RecipeNode{recipe=Equivalency via Instance: 1 raw_gold_block to: raw_gold_block}]}") ||
                vertex.toString().equals("RecipeNode{recipe=SimpleEquivalencyRecipe{inputs=[SimpleIngredient{candidates=[1.0 x ItemStack: 1 raw_gold_block], count=1.0}], requiredKnownOutputs=[], outputs=[9.0 x ItemStack: 1 raw_gold]}}") ||
                vertex.toString().equals("CliqueNode{nodes=[ContainerNode{contents=1.0 x ItemStack: 1 raw_gold}, IngredientNode{ingredient=SimpleIngredient{candidates=[1.0 x ItemStack: 1 raw_gold], count=1.0}}, RecipeNode{recipe=Equivalency via Instance: 1 raw_gold to: raw_gold}, ContainerNode{contents=1.0 x Item: minecraft:raw_gold}, IngredientNode{ingredient=SimpleIngredient{candidates=[1.0 x Item: minecraft:raw_gold], count=1.0}}, RecipeNode{recipe=Equivalency via Instance: raw_gold to: 1 raw_gold}]}") ||
                vertex.toString().equals("RecipeNode{recipe=SimpleEquivalencyRecipe{inputs=[SimpleIngredient{candidates=[1.0 x ItemStack: 1 raw_gold], count=9.0}], requiredKnownOutputs=[], outputs=[1.0 x ItemStack: 1 raw_gold_block]}}")
        ) {
            System.out.println("Hello");
        }
        path.addFirst(vertex);

        visitedVertexes.add(vertex);
        tracer.onEncounterVertex(vertex);

        offerAction(new CleanVertex<>(vertex));
    }

    public void closeVertex(V vertex) {
        path.clean(vertex);

        tracer.onExitVertex(vertex);
    }

    public void offerAction(ISearchAction<G, V, E> action) {
        if (action instanceof VisitVertex<G, V, E> visitVertex) {
            if (visitVertex.vertex().toString().equals("CliqueNode{nodes=[ContainerNode{contents=1.0 x Item: minecraft:raw_copper_block}, RecipeNode{recipe=Equivalency via Instance: raw_copper_block to: 1 raw_copper_block}, IngredientNode{ingredient=SimpleIngredient{candidates=[1.0 x Item: minecraft:raw_copper_block], count=1.0}}, ContainerNode{contents=1.0 x ItemStack: 1 raw_copper_block}, RecipeNode{recipe=Equivalency via Instance: 1 raw_copper_block to: raw_copper_block}, IngredientNode{ingredient=SimpleIngredient{candidates=[1.0 x ItemStack: 1 raw_copper_block], count=1.0}}]}") ||
                    visitVertex.vertex().toString().equals("RecipeNode{recipe=SimpleEquivalencyRecipe{inputs=[SimpleIngredient{candidates=[1.0 x ItemStack: 1 raw_copper_block], count=1.0}], requiredKnownOutputs=[], outputs=[9.0 x ItemStack: 1 raw_copper]}}") ||
                    visitVertex.vertex().toString().equals("CliqueNode{nodes=[ContainerNode{contents=1.0 x Item: minecraft:raw_copper}, RecipeNode{recipe=Equivalency via Instance: raw_copper to: 1 raw_copper}, IngredientNode{ingredient=SimpleIngredient{candidates=[1.0 x Item: minecraft:raw_copper], count=1.0}}, ContainerNode{contents=1.0 x ItemStack: 1 raw_copper}, RecipeNode{recipe=Equivalency via Instance: 1 raw_copper to: raw_copper}, IngredientNode{ingredient=SimpleIngredient{candidates=[1.0 x ItemStack: 1 raw_copper], count=1.0}}]}") ||
                    visitVertex.vertex().toString().equals("RecipeNode{recipe=SimpleEquivalencyRecipe{inputs=[SimpleIngredient{candidates=[1.0 x ItemStack: 1 raw_copper], count=9.0}], requiredKnownOutputs=[], outputs=[1.0 x ItemStack: 1 raw_copper_block]}}")) {
                System.out.println("Hello");
            }
        }

        next.addFirst(action);

        tracer.onActionAdded(action);
    }

    public boolean hasNextAction() {
        return !next.isEmpty();
    }

    public ISearchAction<G, V, E> popAction() {
        final ISearchAction<G, V, E> action = next.pop();

        tracer.onActionRemoved(action);

        return action;
    }
}
