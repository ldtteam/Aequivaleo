package com.ldtteam.aequivaleo.analysis.jgrapht;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.compress.utils.Lists;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.CrossComponentIterator;
import org.junit.Assert;
import org.junit.Test;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JGraphTTests {

    @Test
    public void cycleDetectionInLoopWithBFS() {
        final Graph<String, String> target = DefaultDirectedGraph.<String, String>createBuilder(() -> "").build();
        target.addVertex("root");
        target.addVertex("1");
        target.addVertex("2");
        target.addVertex("3");
        target.addVertex("4");
        target.addVertex("5");

        target.addVertex("root2");
        target.addVertex("6");
        target.addVertex("7");
        target.addVertex("8");

        target.addEdge("root", "1", "root-1");
        target.addEdge("1", "2", "1-2");
        target.addEdge("2", "3", "2-3");
        target.addEdge("3", "1", "3-1");
        target.addEdge("2", "4", "2-4");
        target.addEdge("4", "5", "4-5");
        target.addEdge("5", "4", "5-4");
        target.addEdge("5", "1", "5-1");

        target.addEdge("root2", "6", "root2-6");
        target.addEdge("6", "7", "6-7");
        target.addEdge("7", "8", "7-8");
        target.addEdge("8", "6", "8-6");

        final Multimap<String, String> pathTaken = ArrayListMultimap.create();
        final List<List<String>> cycles = new ArrayList<>();
        final BreadthFirstIterator<String, String> sut = new BreadthFirstIterator<>(target) {
            @Override
            protected void encounterVertex(String vertex, String edge) {
                super.encounterVertex(vertex, edge);

                boolean isRoot = edge == null;
                if (!isRoot) {
                    final String input = Graphs.getOppositeVertex(target, edge, vertex);
                    final Collection<String> upUntil = List.copyOf(pathTaken.get(input));
                    pathTaken.putAll(vertex, upUntil);
                    pathTaken.put(vertex, edge);
                }
            }

            @Override
            protected void encounterVertexAgain(String vertex, String edge) {
                super.encounterVertexAgain(vertex, edge);
                final String input = Graphs.getOppositeVertex(target, edge, vertex);
                final List<String> path = new ArrayList<>(pathTaken.get(input));
                Collections.reverse(path);

                final List<String> cycle = new ArrayList<>();
                cycle.add(vertex);
                String workingVertex = input;
                while(!workingVertex.equals(vertex)) {
                    if (path.size() == 0)
                        throw new IllegalStateException("Path could not be turned into cycle");

                    final String workingEdge = path.remove(0);
                    cycle.add(workingVertex);
                    workingVertex = Graphs.getOppositeVertex(target, workingEdge, workingVertex);
                }

                cycles.add(cycle);
            }
        };
        final List<String> result = new ArrayList<>();
        sut.forEachRemaining(result::add);

        Assert.assertEquals(4, cycles.size());
    }
}
