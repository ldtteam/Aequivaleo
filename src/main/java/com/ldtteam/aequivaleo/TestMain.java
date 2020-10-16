package com.ldtteam.aequivaleo;

import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.Edge;
import org.jgrapht.alg.cycle.DirectedSimpleCycles;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

public class TestMain
{

    public static void main(String... args) {
        final DefaultDirectedWeightedGraph<String, Edge> graph = new DefaultDirectedWeightedGraph<>(Edge.class);

        final String A = "A";
        final String B = "B";
        final String C = "C";
        final String D = "D";
        final String E = "E";

        graph.addVertex(A);
        graph.addVertex(B);
        graph.addVertex(C);
        graph.addVertex(D);
        graph.addVertex(E);

        graph.addEdge(A, B);
        graph.addEdge(B, C);
        graph.addEdge(C, A);

        graph.addEdge(C, D);
        graph.addEdge(D, E);
        graph.addEdge(E, C);

        final DirectedSimpleCycles<String, Edge> algo = new HawickJamesSimpleCycles<>(graph);
        algo.findSimpleCycles().forEach(s -> {
            System.out.println("Found cycle: Contents:");
            s.forEach(n -> System.out.println("   -> " + n));
            System.out.println("");
        });
    }
}
