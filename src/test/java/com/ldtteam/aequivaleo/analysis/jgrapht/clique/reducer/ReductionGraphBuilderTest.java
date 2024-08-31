package com.ldtteam.aequivaleo.analysis.jgrapht.clique.reducer;

import com.ldtteam.aequivaleo.analysis.jgrapht.clique.graph.CliqueDetectionEdge;
import junit.framework.TestCase;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReductionGraphBuilderTest {

    private static final class StringReductionGraphBuilder extends ReductionGraphBuilder<Graph<String, String>, String, String> {
        @Override
        protected boolean isRelevantNode(String node) {
            return node.startsWith("C");
        }
    }

    private ReductionGraphBuilder<Graph<String, String>, String, String> builder;
    private Graph<String, String> graph;

    @Before
    public void setUp() {
        builder = new StringReductionGraphBuilder();
        graph = new SimpleDirectedGraph<>(null, null, true);
    }

    private void add(String vertex, String target, double weight) {
        graph.addVertex(vertex);
        graph.addVertex(target);
        graph.addEdge(vertex, target, "%s -> %s (%s)".formatted(vertex, target, weight));
    }

    @Test
    public void testReduceDirect() {
        int size = 3;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    add("C" + i, "C" + j, 1);
                }
            }
        }

        var reductionGraph = builder.reduce(graph);

        assertEquals(size, reductionGraph.vertexSet().size());
        for (CliqueDetectionEdge<String> edge : reductionGraph.edgeSet()) {
            assertEquals(0, edge.getIntermediaryNodes().size());
        }
    }

    @Test
    public void testReduceRecipe() {
        int size = 3;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    add("C%d".formatted(i), "I%d".formatted(i), 1);
                    add("I%d".formatted(i), "R%d%d".formatted(i, j), 1);
                    add("R%d%d".formatted(i, j), "C%d".formatted(j), 1);
                }
            }
        }

        var reductionGraph = builder.reduce(graph);

        assertEquals(size, reductionGraph.vertexSet().size());
        for (int i = 0; i < size; i++) {
            //The vertex to check.
            final String vertex = "C%d".formatted(i);

            //Each vertex has size - 1 incoming and outgoing edges (we are simulating a full clique), which the reduction graph needs to respect.
            assertEquals(size - 1, reductionGraph.inDegreeOf(vertex));
            assertEquals(size - 1, reductionGraph.outDegreeOf(vertex));

            //Now check the connection to all other vertices
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    final String other = "C%d".formatted(j);
                    final String intermediary = "I%d".formatted(i);
                    final String recipe = "R%d%d".formatted(i, j);

                    //Check that there is an edge from the vertex to the other vertex
                    assertEquals(1, reductionGraph.getAllEdges(vertex, other).size());

                    final CliqueDetectionEdge<String> edge = reductionGraph.getEdge(vertex, other);
                    assertEquals(2, edge.getIntermediaryNodes().size());
                    assertEquals(intermediary, edge.getIntermediaryNodes().get(0));
                    assertEquals(recipe, edge.getIntermediaryNodes().get(1));
                }
            }
        }
    }

    @Test
    public void testReduceRecipeWithDoubleIngredient() {
        int size = 3;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    add("C%d".formatted(i), "I%d".formatted(i), 1);
                    add("I%d".formatted(i), "R%d%d".formatted(i, j), 2);
                    add("R%d%d".formatted(i, j), "C%d".formatted(j), 2);
                }
            }
        }

        var reductionGraph = builder.reduce(graph);

        assertEquals(size, reductionGraph.vertexSet().size());
        for (int i = 0; i < size; i++) {
            //The vertex to check.
            final String vertex = "C%d".formatted(i);

            //Each vertex has size - 1 incoming and outgoing edges (we are simulating a full clique), which the reduction graph needs to respect.
            assertEquals(size - 1, reductionGraph.inDegreeOf(vertex));
            assertEquals(size - 1, reductionGraph.outDegreeOf(vertex));

            //Now check the connection to all other vertices
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    final String other = "C%d".formatted(j);
                    final String intermediary = "I%d".formatted(i);
                    final String recipe = "R%d%d".formatted(i, j);

                    //Check that there is an edge from the vertex to the other vertex
                    assertEquals(1, reductionGraph.getAllEdges(vertex, other).size());

                    final CliqueDetectionEdge<String> edge = reductionGraph.getEdge(vertex, other);
                    assertEquals(2, edge.getIntermediaryNodes().size());
                    assertEquals(intermediary, edge.getIntermediaryNodes().get(0));
                    assertEquals(recipe, edge.getIntermediaryNodes().get(1));
                }
            }
        }
    }
}