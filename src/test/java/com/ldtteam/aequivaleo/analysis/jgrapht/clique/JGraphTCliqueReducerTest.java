package com.ldtteam.aequivaleo.analysis.jgrapht.clique;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.junit.Test;

public class JGraphTCliqueReducerTest  {

    @Test
    public void testReduce() {
        final SimpleDirectedWeightedGraph<String, String> graph = new SimpleDirectedWeightedGraph<>(String.class);
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");
        graph.addVertex("D");

        graph.addEdge("A", "B", "AB");
        graph.addEdge("A", "C", "AC");
        graph.addEdge("A", "D", "AD");
        graph.addEdge("B", "A", "BA");
        graph.addEdge("B", "C", "BC");
        graph.addEdge("B", "D", "BD");
        graph.addEdge("C", "A", "CA");
        graph.addEdge("C", "B", "CB");
        graph.addEdge("C", "D", "CD");
        graph.addEdge("D", "A", "DA");
        graph.addEdge("D", "B", "DB");
        graph.addEdge("D", "C", "DC");

        graph.setEdgeWeight("AB", 1);
        graph.setEdgeWeight("AC", 1);
        graph.setEdgeWeight("AD", 1);
        graph.setEdgeWeight("BA", 1);
        graph.setEdgeWeight("BC", 1);
        graph.setEdgeWeight("BC", 1);
        graph.setEdgeWeight("CD", 1);
        graph.setEdgeWeight("DA", 1);


    }

}