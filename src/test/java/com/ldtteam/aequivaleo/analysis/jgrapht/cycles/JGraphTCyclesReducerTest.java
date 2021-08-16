package com.ldtteam.aequivaleo.analysis.jgrapht.cycles;

import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.analysis.jgrapht.edge.Edge;
import com.ldtteam.aequivaleo.analysis.jgrapht.graph.SimpleAnalysisGraph;
import com.ldtteam.aequivaleo.config.CommonConfiguration;
import com.ldtteam.aequivaleo.config.Configuration;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jgrapht.Graph;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor({"net.minecraft.world.World"})
@PowerMockIgnore({"jdk.internal.reflect.*", "org.apache.log4j.*", "org.apache.commons.logging.*", "javax.management.*"})
@PrepareForTest({Aequivaleo.class})
public class JGraphTCyclesReducerTest
{

    JGraphTCyclesReducer<Graph<String, Edge>, String, Edge> reducer;
    @Before
    public void setUp()
    {
        mockStatic(Aequivaleo.class);
        Aequivaleo mod = mock(Aequivaleo.class);
        when(Aequivaleo.getInstance()).thenReturn(mod);

        Configuration config = mock(Configuration.class);
        CommonConfiguration commonConfiguration = mock(CommonConfiguration.class);
        ForgeConfigSpec.BooleanValue alwaysTrueConfig = mock(ForgeConfigSpec.BooleanValue.class);
        when(alwaysTrueConfig.get()).thenReturn(true);
        commonConfiguration.debugAnalysisLog = alwaysTrueConfig;
        when(config.getCommon()).thenReturn(commonConfiguration);
        when(mod.getConfiguration()).thenReturn(config);

        reducer = new JGraphTCyclesReducer<>(
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

    @Test
    public void reduceOnceOnlyCycleToSingleNode() {
        final Graph<String, Edge> graph = new SimpleAnalysisGraph<>(Edge::new);
        graph.addVertex("cycle-1");
        graph.addVertex("cycle-2");
        graph.addEdge("cycle-1", "cycle-2");
        graph.addEdge("cycle-2", "cycle-1");

        reducer.reduceOnce(graph);

        assertEquals(1, graph.vertexSet().size());
        assertEquals(0, graph.edgeSet().size());
        assertEquals("([([cycle-2, cycle-1], [(cycle-2,cycle-1), (cycle-1,cycle-2)])], [])", graph.toString());
    }

    @Test
    public void reduceOnceCycleWithAppendix() {
        final Graph<String, Edge> graph = new SimpleAnalysisGraph<>(Edge::new);
        graph.addVertex("cycle-1");
        graph.addVertex("cycle-2");
        graph.addVertex("appendix");
        graph.addEdge("cycle-1", "cycle-2");
        graph.addEdge("cycle-2", "cycle-1");
        graph.addEdge("cycle-2", "appendix");

        reducer.reduceOnce(graph);

        assertEquals(2, graph.vertexSet().size());
        assertEquals(1, graph.edgeSet().size());
        assertEquals("([appendix, ([cycle-2, cycle-1], [(cycle-2,cycle-1), (cycle-1,cycle-2)])], [(([cycle-2, cycle-1], [(cycle-2,cycle-1), (cycle-1,cycle-2)]),appendix)])", graph.toString());
    }

    @Test
    public void reduceOnceCycleWithPrefixAndAppendix() {
        final Graph<String, Edge> graph = new SimpleAnalysisGraph<>(Edge::new);
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
        assertEquals("([prefix, appendix, ([cycle-2, cycle-1], [(cycle-2,cycle-1), (cycle-1,cycle-2)])], [(prefix,([cycle-2, cycle-1], [(cycle-2,cycle-1), (cycle-1,cycle-2)])), (([cycle-2, cycle-1], [(cycle-2,cycle-1), (cycle-1,cycle-2)]),appendix)])", graph.toString());
    }

    @Test
    public void reduceDualCycleCompletely() {
        final Graph<String, Edge> graph = new SimpleAnalysisGraph<>(Edge::new);
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
        assertEquals("([([cycle-b-1, ([cycle-ab-2, cycle-a-1], [(cycle-ab-2,cycle-a-1), (cycle-a-1,cycle-ab-2)])], [(cycle-b-1,([cycle-ab-2, cycle-a-1], [(cycle-ab-2,cycle-a-1), (cycle-a-1,cycle-ab-2)])), (([cycle-ab-2, cycle-a-1], [(cycle-ab-2,cycle-a-1), (cycle-a-1,cycle-ab-2)]),cycle-b-1)])], [])", graph.toString());
    }

    @Test
    public void reduceCycleWithMultipleInputsFromDifferentNodes() {
        final Graph<String, Edge> graph = new SimpleAnalysisGraph<>(Edge::new);
        graph.addVertex("cycle-1");
        graph.addVertex("cycle-2");
        graph.addVertex("prefix-1");
        graph.addVertex("appendix-1");
        graph.addVertex("prefix-2");
        graph.addVertex("appendix-2");
        graph.addEdge("prefix-1", "cycle-1");
        graph.addEdge("prefix-2", "cycle-1");
        graph.addEdge("cycle-1", "cycle-2");
        graph.addEdge("cycle-2", "cycle-1");
        graph.addEdge("cycle-2", "appendix-1");
        graph.addEdge("cycle-2", "appendix-2");

        reducer.reduceOnce(graph);

        assertEquals(5, graph.vertexSet().size());
        assertEquals(4, graph.edgeSet().size());
        assertEquals("([prefix-1, appendix-1, prefix-2, appendix-2, ([cycle-2, cycle-1], [(cycle-2,cycle-1), (cycle-1,cycle-2)])], [(prefix-1,([cycle-2, cycle-1], [(cycle-2,cycle-1), (cycle-1,cycle-2)])), (prefix-2,([cycle-2, cycle-1], [(cycle-2,cycle-1), (cycle-1,cycle-2)])), (([cycle-2, cycle-1], [(cycle-2,cycle-1), (cycle-1,cycle-2)]),appendix-1), (([cycle-2, cycle-1], [(cycle-2,cycle-1), (cycle-1,cycle-2)]),appendix-2)])", graph.toString());
    }

    @Test
    public void reduceCycleWithMultipleInputsFromSameNodes() {
        final Graph<String, Edge> graph = new SimpleAnalysisGraph<>(Edge::new);
        graph.addVertex("cycle-1");
        graph.addVertex("cycle-2");
        graph.addVertex("prefix");
        graph.addVertex("appendix");
        graph.addEdge("prefix", "cycle-1");
        graph.addEdge("prefix", "cycle-2");
        graph.addEdge("cycle-1", "cycle-2");
        graph.addEdge("cycle-2", "cycle-1");
        graph.addEdge("cycle-2", "appendix");
        graph.addEdge("cycle-1", "appendix");

        reducer.reduceOnce(graph);

        assertEquals(3, graph.vertexSet().size());
        assertEquals(2, graph.edgeSet().size());
        assertEquals(4, graph.edgeSet().stream().mapToDouble(graph::getEdgeWeight).sum(), 0.000000d);
        assertEquals("([prefix, appendix, ([cycle-2, cycle-1], [(cycle-2,cycle-1), (cycle-1,cycle-2)])], [(prefix,([cycle-2, cycle-1], [(cycle-2,cycle-1), (cycle-1,cycle-2)])), (([cycle-2, cycle-1], [(cycle-2,cycle-1), (cycle-1,cycle-2)]),appendix)])", graph.toString());
    }


    @Test
    public void reduceLargeCycleWithSmallerInnerCycle() {
        final Graph<String, Edge> graph = new SimpleAnalysisGraph<>(Edge::new);
        graph.addVertex("cycle-1");
        graph.addVertex("cycle-2");
        graph.addVertex("cycle-3");
        graph.addVertex("cycle-4");
        graph.addVertex("cycle-5");
        graph.addVertex("cycle-6");
        graph.addVertex("cycle-7");
        graph.addVertex("cycle-8");
        graph.addVertex("cycle-9");
        graph.addVertex("cycle-10");
        graph.addVertex("cycle-a");
        graph.addVertex("cycle-b");
        graph.addVertex("cycle-c");

        graph.addEdge("cycle-1", "cycle-2");
        graph.addEdge("cycle-2", "cycle-3");
        graph.addEdge("cycle-3", "cycle-4");
        graph.addEdge("cycle-4", "cycle-5");
        graph.addEdge("cycle-5", "cycle-6");
        graph.addEdge("cycle-6", "cycle-7");
        graph.addEdge("cycle-7", "cycle-8");
        graph.addEdge("cycle-8", "cycle-9");
        graph.addEdge("cycle-9", "cycle-10");
        graph.addEdge("cycle-10", "cycle-1");


        graph.addEdge("cycle-1", "cycle-a");
        graph.addEdge("cycle-a", "cycle-b");
        graph.addEdge("cycle-b", "cycle-c");
        graph.addEdge("cycle-c", "cycle-9");


        reducer.reduceOnce(graph);

        assertEquals(1, graph.vertexSet().size());
        assertEquals(0, graph.edgeSet().size());
        assertEquals(0, graph.edgeSet().stream().mapToDouble(graph::getEdgeWeight).sum(), 0.000000d);
        assertEquals("([([([cycle-10, cycle-9, cycle-c, cycle-b, cycle-a, cycle-1], [(cycle-10,cycle-1), (cycle-9,cycle-10), (cycle-c,cycle-9), (cycle-b,cycle-c), (cycle-a,cycle-b), (cycle-1,cycle-a)]), cycle-8, cycle-7, cycle-6, cycle-5, cycle-4, cycle-3, cycle-2], [(([cycle-10, cycle-9, cycle-c, cycle-b, cycle-a, cycle-1], [(cycle-10,cycle-1), (cycle-9,cycle-10), (cycle-c,cycle-9), (cycle-b,cycle-c), (cycle-a,cycle-b), (cycle-1,cycle-a)]),cycle-2), (cycle-8,([cycle-10, cycle-9, cycle-c, cycle-b, cycle-a, cycle-1], [(cycle-10,cycle-1), (cycle-9,cycle-10), (cycle-c,cycle-9), (cycle-b,cycle-c), (cycle-a,cycle-b), (cycle-1,cycle-a)])), (cycle-7,cycle-8), (cycle-6,cycle-7), (cycle-5,cycle-6), (cycle-4,cycle-5), (cycle-3,cycle-4), (cycle-2,cycle-3)])], [])", graph.toString());
    }
}