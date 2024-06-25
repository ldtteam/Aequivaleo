package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class DFSDirectCycleReducerTest {

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