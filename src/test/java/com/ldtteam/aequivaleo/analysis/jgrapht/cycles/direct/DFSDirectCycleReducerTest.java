package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.config.CommonConfiguration;
import com.ldtteam.aequivaleo.config.Configuration;
import com.ldtteam.aequivaleo.config.ServerConfiguration;
import com.ldtteam.aequivaleo.testing.compound.container.testing.StringCompoundContainer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.IForgeRegistry;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class DFSDirectCycleReducerTest {

    MockedStatic<Aequivaleo> aequivaleoMock;
    MockedStatic<ModList> modListMock;

    @Before
    public void setUp()
    {
        aequivaleoMock = mockStatic(Aequivaleo.class);
        modListMock = mockStatic(ModList.class);
        Aequivaleo mod = mock(Aequivaleo.class);
        when(Aequivaleo.getInstance()).thenReturn(mod);

        Configuration config = mock(Configuration.class);
        ServerConfiguration serverConfig = mock(ServerConfiguration.class);
        ForgeConfigSpec.BooleanValue alwaysFalseConfig = mock(ForgeConfigSpec.BooleanValue.class);
        ForgeConfigSpec.BooleanValue alwaysTrueConfig = mock(ForgeConfigSpec.BooleanValue.class);

        when(alwaysFalseConfig.get()).thenReturn(false);
        serverConfig.exportGraph = alwaysFalseConfig;
        serverConfig.writeResultsToLog = alwaysFalseConfig;
        serverConfig.useActionPooling = alwaysTrueConfig;
        serverConfig.performCycleReductionInspection = alwaysTrueConfig;
        when(config.getServer()).thenReturn(serverConfig);

        CommonConfiguration commonConfiguration = mock(CommonConfiguration.class);
        when(alwaysTrueConfig.get()).thenReturn(true);
        commonConfiguration.debugAnalysisLog = alwaysTrueConfig;
        when(config.getCommon()).thenReturn(commonConfiguration);

        when(mod.getConfiguration()).thenReturn(config);

        List<ICompoundContainerFactory<?>> containerFactories = ImmutableList.of(new StringCompoundContainer.Factory());
        ModRegistries.CONTAINER_FACTORY = Suppliers.memoize(() -> mock(IForgeRegistry.class));
        when(ModRegistries.CONTAINER_FACTORY.get().iterator()).thenReturn(containerFactories.iterator());
        CompoundContainerFactoryManager.getInstance().bake();
    }

    @After
    public void close() {
        aequivaleoMock.close();
        modListMock.close();
    }

    @Test
    public void reduceSimpleCycle() {
        // Arrange
        final AtomicInteger dynamicEdgeCounter = new AtomicInteger(0);
        final SimpleDirectedWeightedGraph<String, String> graph = new SimpleDirectedWeightedGraph<>(null, () -> "DynamicEdge" + dynamicEdgeCounter.getAndIncrement());
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");

        graph.addEdge("A", "B", "AB");
        graph.addEdge("B", "C", "BC");
        graph.addEdge("C", "D", "CD");
        graph.addEdge("D", "A", "DA");

        final DFSDirectCycleReducer<SimpleDirectedWeightedGraph<String, String>, String, String> cycleReducer = new DFSDirectCycleReducer<>(
                list -> String.join("", list)
        );
        // Act
        cycleReducer.reduce(graph, "A");

        // Assert
        Assert.assertEquals(1, graph.vertexSet().size());
        Assert.assertEquals(0, graph.edgeSet().size());
        Assert.assertEquals("ABCD", graph.vertexSet().iterator().next());
    }

    @Test
    public void reduceSimpleCycleTail() {
        // Arrange
        final AtomicInteger dynamicEdgeCounter = new AtomicInteger(0);
        final SimpleDirectedWeightedGraph<String, String> graph = new SimpleDirectedWeightedGraph<>(null, () -> "DynamicEdge" + dynamicEdgeCounter.getAndIncrement());
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");

        graph.addVertex("1");
        graph.addVertex("2");
        graph.addVertex("3");

        graph.addEdge("A", "B", "AB");
        graph.addEdge("B", "C", "BC");
        graph.addEdge("C", "D", "CD");
        graph.addEdge("D", "A", "DA");
        graph.addEdge("C", "1", "C1");
        graph.addEdge("1", "2", "12");
        graph.addEdge("2", "3", "23");

        final DFSDirectCycleReducer<SimpleDirectedWeightedGraph<String, String>, String, String> cycleReducer = new DFSDirectCycleReducer<>(
                list -> String.join("", list)
        );
        // Act
        cycleReducer.reduce(graph, "A");

        // Assert
        Assert.assertEquals(4, graph.vertexSet().size());
        Assert.assertEquals(3, graph.edgeSet().size());
        Assert.assertTrue(graph.containsVertex("ABCD"));
        Assert.assertTrue(graph.containsVertex("1"));
        Assert.assertTrue(graph.containsVertex("2"));
        Assert.assertTrue(graph.containsVertex("3"));
        Assert.assertTrue(graph.containsEdge("ABCD", "1"));
        Assert.assertTrue(graph.containsEdge("1", "2"));
        Assert.assertTrue(graph.containsEdge("2", "3"));
    }

    @Test
    public void reduceSimpleCycleHead() {
        // Arrange
        final AtomicInteger dynamicEdgeCounter = new AtomicInteger(0);
        final SimpleDirectedWeightedGraph<String, String> graph = new SimpleDirectedWeightedGraph<>(null, () -> "DynamicEdge" + dynamicEdgeCounter.getAndIncrement());
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("1");
        graph.addVertex("2");
        graph.addVertex("3");

        graph.addEdge("1", "2", "12");
        graph.addEdge("2", "3", "23");
        graph.addEdge("3", "A", "3A");

        graph.addEdge("A", "B", "AB");
        graph.addEdge("B", "C", "BC");
        graph.addEdge("C", "D", "CD");
        graph.addEdge("D", "A", "DA");

        final DFSDirectCycleReducer<SimpleDirectedWeightedGraph<String, String>, String, String> cycleReducer = new DFSDirectCycleReducer<>(
                list -> String.join("", list)
        );
        // Act
        cycleReducer.reduce(graph, "1");

        // Assert
        Assert.assertEquals(4, graph.vertexSet().size());
        Assert.assertEquals(3, graph.edgeSet().size());
        Assert.assertTrue(graph.containsVertex("ABCD"));
        Assert.assertTrue(graph.containsVertex("1"));
        Assert.assertTrue(graph.containsVertex("2"));
        Assert.assertTrue(graph.containsVertex("3"));
        Assert.assertTrue(graph.containsEdge("3", "ABCD"));
        Assert.assertTrue(graph.containsEdge("1", "2"));
        Assert.assertTrue(graph.containsEdge("2", "3"));
    }

    @Test
    public void reduceSimpleCycleTailAndHead() {
        // Arrange
        final AtomicInteger dynamicEdgeCounter = new AtomicInteger(0);
        final SimpleDirectedWeightedGraph<String, String> graph = new SimpleDirectedWeightedGraph<>(null, () -> "DynamicEdge" + dynamicEdgeCounter.getAndIncrement());
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");

        graph.addVertex("1");
        graph.addVertex("2");
        graph.addVertex("3");

        graph.addVertex("-");
        graph.addVertex("+");
        graph.addVertex("=");

        graph.addEdge("-", "+", "-+");
        graph.addEdge("+", "=", "+=");
        graph.addEdge("=", "A", "=A");
        graph.addEdge("A", "B", "AB");
        graph.addEdge("B", "C", "BC");
        graph.addEdge("C", "D", "CD");
        graph.addEdge("D", "A", "DA");
        graph.addEdge("C", "1", "C1");
        graph.addEdge("1", "2", "12");
        graph.addEdge("2", "3", "23");

        final DFSDirectCycleReducer<SimpleDirectedWeightedGraph<String, String>, String, String> cycleReducer = new DFSDirectCycleReducer<>(
                list -> String.join("", list)
        );
        // Act
        cycleReducer.reduce(graph, "A");

        // Assert
        Assert.assertEquals(7, graph.vertexSet().size());
        Assert.assertEquals(6, graph.edgeSet().size());
        Assert.assertTrue(graph.containsVertex("-"));
        Assert.assertTrue(graph.containsVertex("+"));
        Assert.assertTrue(graph.containsVertex("="));
        Assert.assertTrue(graph.containsVertex("ABCD"));
        Assert.assertTrue(graph.containsVertex("1"));
        Assert.assertTrue(graph.containsVertex("2"));
        Assert.assertTrue(graph.containsVertex("3"));
        Assert.assertTrue(graph.containsEdge("-", "+"));
        Assert.assertTrue(graph.containsEdge("+", "="));
        Assert.assertTrue(graph.containsEdge("=", "ABCD"));
        Assert.assertTrue(graph.containsEdge("ABCD", "1"));
        Assert.assertTrue(graph.containsEdge("1", "2"));
        Assert.assertTrue(graph.containsEdge("2", "3"));
    }
}