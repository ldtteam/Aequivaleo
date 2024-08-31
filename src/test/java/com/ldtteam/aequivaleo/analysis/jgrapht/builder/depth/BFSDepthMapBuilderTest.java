package com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;

import static org.junit.Assert.*;

public class BFSDepthMapBuilderTest {

    @Test
    public void testSimpleTree() {
        final Graph<String, String> graph = new SimpleDirectedGraph<>(String.class);

        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");

        graph.addEdge("A", "B", "AB");
        graph.addEdge("A", "C", "AC");

        graph.addEdge("B", "D", "BD");
        graph.addEdge("B", "E", "BE");

        final BFSDepthMapBuilder<Graph<String, String>, String, String> depthMapBuilder = new BFSDepthMapBuilder<>(graph, "A");
        final var depthMap = depthMapBuilder.calculateDepthMap();

        assertEquals(0, depthMap.get("A").intValue());
        assertEquals(1, depthMap.get("B").intValue());
        assertEquals(1, depthMap.get("C").intValue());
        assertEquals(2, depthMap.get("D").intValue());
        assertEquals(2, depthMap.get("E").intValue());
    }

    @Test
    public void testDAGWithInterBranchConnections() {
        final Graph<String, String> graph = new SimpleDirectedGraph<>(String.class);

        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");

        graph.addEdge("A", "B", "AB");
        graph.addEdge("A", "C", "AC");

        graph.addEdge("B", "D", "BD");
        graph.addEdge("B", "E", "BE");

        graph.addEdge("D", "E", "DE");

        final BFSDepthMapBuilder<Graph<String, String>, String, String> depthMapBuilder = new BFSDepthMapBuilder<>(graph, "A");
        final var depthMap = depthMapBuilder.calculateDepthMap();

        assertEquals(0, depthMap.get("A").intValue());
        assertEquals(1, depthMap.get("B").intValue());
        assertEquals(1, depthMap.get("C").intValue());
        assertEquals(2, depthMap.get("D").intValue());
        assertEquals(3, depthMap.get("E").intValue());
    }

    @Test
    public void testDAGWithInterBranchIntermediaryConnections() {
        final Graph<String, String> graph = new SimpleDirectedGraph<>(String.class);

        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");
        graph.addVertex("E");
        graph.addVertex("F");
        graph.addVertex("G");

        graph.addEdge("A", "B", "AB");
        graph.addEdge("B", "C", "BC");
        graph.addEdge("C", "D", "CD");
        graph.addEdge("D", "E", "DE");

        graph.addEdge("B", "F", "BF");
        graph.addEdge("F", "G", "FG");
        graph.addEdge("G", "C", "GC");



        final BFSDepthMapBuilder<Graph<String, String>, String, String> depthMapBuilder = new BFSDepthMapBuilder<>(graph, "A");
        final var depthMap = depthMapBuilder.calculateDepthMap();

        assertEquals(0, depthMap.get("A").intValue());
        assertEquals(1, depthMap.get("B").intValue());
        assertEquals(2, depthMap.get("F").intValue());
        assertEquals(3, depthMap.get("G").intValue());
        assertEquals(4, depthMap.get("C").intValue());
        assertEquals(5, depthMap.get("D").intValue());
        assertEquals(6, depthMap.get("E").intValue());
    }

}