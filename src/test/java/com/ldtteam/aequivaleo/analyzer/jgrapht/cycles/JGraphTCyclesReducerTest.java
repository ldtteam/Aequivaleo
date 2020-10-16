package com.ldtteam.aequivaleo.analyzer.jgrapht.cycles;

import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.Edge;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("net.minecraft.world.World")
@PowerMockIgnore({"jdk.internal.reflect.*", "org.apache.log4j.*", "org.apache.commons.logging.*", "javax.management.*"})
@PrepareForTest({Aequivaleo.class})
public class JGraphTCyclesReducerTest
{

    JGraphTCyclesReducer<String, Edge> reducer;

    @Before
    public void setUp() throws Exception
    {
        reducer = new JGraphTCyclesReducer<>(
          (graph, vertices) -> {
              final Graph<String, Edge> innerGraph = new DefaultDirectedWeightedGraph<>(Edge.class);
              vertices.forEach(innerGraph::addVertex);
              vertices.stream()
                .map(graph::outgoingEdgesOf)
                .flatMap(Collection::stream)
                .filter(e -> vertices.contains(graph.getEdgeTarget(e)))
                .forEach(e -> innerGraph.addEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e), e));

              return innerGraph.toString();
          },
          (graph, originalEdge, replacementEdge) -> graph.setEdgeWeight(replacementEdge, graph.getEdgeWeight(replacementEdge)),
          (s, s2, s3) -> {
              //Do not care.
          });
    }

    @Test
    public void reduceOnceOnlyCycleToSingleNode() {
        final Graph<String, Edge> graph = new DefaultDirectedWeightedGraph<>(Edge.class);
        graph.addVertex("cycle-1");
        graph.addVertex("cycle-2");
        graph.addEdge("cycle-1", "cycle-2");
        graph.addEdge("cycle-2", "cycle-1");

        reducer.reduceOnce(graph);

        assertEquals(1, graph.vertexSet().size());
        assertEquals(0, graph.edgeSet().size());
        assertEquals("([([cycle-2, cycle-1], [(cycle-2 : cycle-1)=(cycle-2,cycle-1), (cycle-1 : cycle-2)=(cycle-1,cycle-2)])], [])", graph.toString());
    }

    @Test
    public void reduceOnceCycleWithAppendix() {
        final Graph<String, Edge> graph = new DefaultDirectedWeightedGraph<>(Edge.class);
        graph.addVertex("cycle-1");
        graph.addVertex("cycle-2");
        graph.addVertex("appendix");
        graph.addEdge("cycle-1", "cycle-2");
        graph.addEdge("cycle-2", "cycle-1");
        graph.addEdge("cycle-2", "appendix");

        reducer.reduceOnce(graph);

        assertEquals(2, graph.vertexSet().size());
        assertEquals(1, graph.edgeSet().size());
        assertEquals("([appendix, ([cycle-2, cycle-1], [(cycle-2 : cycle-1)=(cycle-2,cycle-1), (cycle-1 : cycle-2)=(cycle-1,cycle-2)])], [(([cycle-2, cycle-1], [(cycle-2 : cycle-1)=(cycle-2,cycle-1), (cycle-1 : cycle-2)=(cycle-1,cycle-2)]) : appendix)=(([cycle-2, cycle-1], [(cycle-2 : cycle-1)=(cycle-2,cycle-1), (cycle-1 : cycle-2)=(cycle-1,cycle-2)]),appendix)])", graph.toString());
    }

    @Test
    public void reduceOnceCycleWithPrefixAndAppendix() {
        final Graph<String, Edge> graph = new DefaultDirectedWeightedGraph<>(Edge.class);
        graph.addVertex("cycle-1");
        graph.addVertex("cycle-2");
        graph.addVertex("prefix");
        graph.addVertex("appendix");
        graph.addEdge("prefix", "cycle-1");
        graph.addEdge("cycle-1", "cycle-2");
        graph.addEdge("cycle-2", "cycle-1");
        graph.addEdge("cycle-2", "appendix");

        reducer.reduceOnce(graph);

        assertEquals(3, graph.vertexSet().size());
        assertEquals(2, graph.edgeSet().size());
        assertEquals("([prefix, appendix, ([cycle-2, cycle-1], [(cycle-2 : cycle-1)=(cycle-2,cycle-1), (cycle-1 : cycle-2)=(cycle-1,cycle-2)])], [(prefix : ([cycle-2, cycle-1], [(cycle-2 : cycle-1)=(cycle-2,cycle-1), (cycle-1 : cycle-2)=(cycle-1,cycle-2)]))=(prefix,([cycle-2, cycle-1], [(cycle-2 : cycle-1)=(cycle-2,cycle-1), (cycle-1 : cycle-2)=(cycle-1,cycle-2)])), (([cycle-2, cycle-1], [(cycle-2 : cycle-1)=(cycle-2,cycle-1), (cycle-1 : cycle-2)=(cycle-1,cycle-2)]) : appendix)=(([cycle-2, cycle-1], [(cycle-2 : cycle-1)=(cycle-2,cycle-1), (cycle-1 : cycle-2)=(cycle-1,cycle-2)]),appendix)])", graph.toString());
    }

    @Test
    public void reduceDualCycleCompletely() {
        final Graph<String, Edge> graph = new DefaultDirectedWeightedGraph<>(Edge.class);
        graph.addVertex("cycle-a-1");
        graph.addVertex("cycle-ab-2");
        graph.addVertex("cycle-b-1");

        graph.addEdge("cycle-a-1", "cycle-ab-2");
        graph.addEdge("cycle-ab-2", "cycle-a-1");
        graph.addEdge("cycle-b-1", "cycle-ab-2");
        graph.addEdge("cycle-ab-2", "cycle-b-1");

        reducer.reduce(graph);

        assertEquals(1, graph.vertexSet().size());
        assertEquals(0, graph.edgeSet().size());
        assertEquals("([([cycle-b-1, ([cycle-ab-2, cycle-a-1], [(cycle-ab-2 : cycle-a-1)=(cycle-ab-2,cycle-a-1), (cycle-a-1 : cycle-ab-2)=(cycle-a-1,cycle-ab-2)])], [(cycle-b-1 : ([cycle-ab-2, cycle-a-1], [(cycle-ab-2 : cycle-a-1)=(cycle-ab-2,cycle-a-1), (cycle-a-1 : cycle-ab-2)=(cycle-a-1,cycle-ab-2)]))=(cycle-b-1,([cycle-ab-2, cycle-a-1], [(cycle-ab-2 : cycle-a-1)=(cycle-ab-2,cycle-a-1), (cycle-a-1 : cycle-ab-2)=(cycle-a-1,cycle-ab-2)])), (([cycle-ab-2, cycle-a-1], [(cycle-ab-2 : cycle-a-1)=(cycle-ab-2,cycle-a-1), (cycle-a-1 : cycle-ab-2)=(cycle-a-1,cycle-ab-2)]) : cycle-b-1)=(([cycle-ab-2, cycle-a-1], [(cycle-ab-2 : cycle-a-1)=(cycle-ab-2,cycle-a-1), (cycle-a-1 : cycle-ab-2)=(cycle-a-1,cycle-ab-2)]),cycle-b-1)])], [])", graph.toString());
    }
}