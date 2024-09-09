package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct;

import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.ICyclesReducer;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search.ISearchAction;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.trace.ICycleReducingTracer;
import com.ldtteam.aequivaleo.api.util.AequivaleoLogger;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;
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

        if (Aequivaleo.getInstance().getConfiguration().getServer().performCycleReductionInspection.get()) {
            AequivaleoLogger.startBigWarning("Cycle Reduction Inspection");
            final SzwarcfiterLauerSimpleCycles<V, E> cycleDetector = new SzwarcfiterLauerSimpleCycles<>(graph);
            final List<List<V>> cycles = cycleDetector.findSimpleCycles();
            for (List<V> cycle : cycles) {
                AequivaleoLogger.warning(" - Found cycle: " + cycle.size());
                for (V vertex : cycle) {
                    AequivaleoLogger.warning("   - " + vertex);
                }
            }
            AequivaleoLogger.endBigWarning("Cycle Reduction Inspection");
        }
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
            context.release(action);
        }

        return true;
    }

}
