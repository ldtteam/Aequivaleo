package com.ldtteam.aequivaleo.analysis.jgrapht.cycles;

import com.ldtteam.aequivaleo.analysis.jgrapht.edge.Edge;
import com.ldtteam.aequivaleo.analysis.jgrapht.graph.SimpleAnalysisGraph;
import org.jgrapht.Graph;

import java.util.Collection;

public class HawickJamesCyclesReducerTest extends AbstractsCyclesReducerTest<HawickJamesCyclesReducer<Graph<String, Edge>, String, Edge>>
{

    @Override
    protected HawickJamesCyclesReducer<Graph<String, Edge>, String, Edge> createReducer() {
        return new HawickJamesCyclesReducer<>(
                (graph, vertices) -> {
                    final Graph<String, Edge> innerGraph = new SimpleAnalysisGraph<>(Edge::new);
                    vertices.forEach(innerGraph::addVertex);
                    vertices.stream()
                            .map(graph::outgoingEdgesOf)
                            .flatMap(Collection::stream)
                            .filter(e -> vertices.contains(graph.getEdgeTarget(e)))
                            .forEach(e -> innerGraph.addEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e), e));

                    return innerGraph.toString();
                },
                (s, s2, s3) -> {
                    //Do not care.
                });
    }
}