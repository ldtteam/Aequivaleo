package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct;

import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.ICyclesReducer;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search.ISearchAction;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.trace.ICycleReducingTracer;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.*;
import java.util.function.Function;

public final class DFSDirectCycleReducer<G extends Graph<V, E>, V, E> implements ICyclesReducer<G, V, E> {

    private final Function<List<V>, V> vertexReplacerFunction;

    private final ICycleReducingTracer<G, V, E> tracer;

    public DFSDirectCycleReducer(Function<List<V>, V> vertexReplacerFunction, ICycleReducingTracer<G, V, E> tracer) {
        this.vertexReplacerFunction = vertexReplacerFunction;
        this.tracer = tracer;
    }

    public DFSDirectCycleReducer(Function<List<V>, V> vertexReplacerFunction) {
        this(vertexReplacerFunction, ICycleReducingTracer.noop());
    }

    @Override
    public void reduce(G graph, V startNode) {
        reduceOnce(graph, startNode);
    }

    @Override
    public boolean reduceOnce(G graph, V startNode) {
        final SimpleDirectedWeightedGraph<V, E> simpleDirectedWeightedGraph = new SimpleDirectedWeightedGraph<>(null, null);
        graph.vertexSet().forEach(simpleDirectedWeightedGraph::addVertex);
        graph.edgeSet().forEach(edge -> simpleDirectedWeightedGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge), edge));

        final Context<G, V, E> context = new Context<>(graph, startNode, vertexReplacerFunction, tracer);

        while (context.hasNextAction()) {
            final ISearchAction<G, V, E> action = context.popAction();
            action.perform(context);
        }

        return true;
    }

}
