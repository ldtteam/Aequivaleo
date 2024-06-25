package com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;

public class BFSDepthMapBuilder<G extends Graph<V, E>, V, E> implements IDepthMapBuilder<V> {
    
    private final G graph;
    private final V sourceVertex;

    public BFSDepthMapBuilder(final G graph, V sourceVertex) {
        this.graph = graph;
        this.sourceVertex = sourceVertex;

        if (graph.inDegreeOf(sourceVertex) != 0)
            throw new IllegalArgumentException("Source vertex must have in-degree of 0");
    }

    @Override
    public Map<V, Integer> calculateDepthMap() {
        final Object2IntMap<V> incomingDegreeMap = new Object2IntOpenHashMap<>(graph.vertexSet().size());
        final Object2IntMap<V> depthMap = new Object2IntOpenHashMap<>(graph.vertexSet().size());

        for (V vertex : graph.vertexSet()) {
            incomingDegreeMap.put(vertex, graph.inDegreeOf(vertex));
        }

        final Deque<V> zeroInDegreeQueue = new ArrayDeque<>();
        zeroInDegreeQueue.add(sourceVertex);
        depthMap.put(sourceVertex, 0);

        while (!zeroInDegreeQueue.isEmpty()) {
            final V vertex = zeroInDegreeQueue.poll();
            final int inDegree = incomingDegreeMap.getInt(vertex);
            if (inDegree != 0)
                throw new IllegalStateException("Vertex with non-zero in-degree in zero in-degree queue: " + vertex);

            final int myDepth = depthMap.getInt(vertex);
            final int neighborDepth = myDepth + 1;

            for (E edge : graph.outgoingEdgesOf(vertex)) {
                final V target = Graphs.getOppositeVertex(graph, edge, vertex);
                final int newInDegree = incomingDegreeMap.getInt(target) - 1;
                incomingDegreeMap.put(target, newInDegree);
                if (newInDegree == 0) {
                    zeroInDegreeQueue.add(target);
                }

                depthMap.compute(target, (k, v) -> {
                    if (v == null) {
                        return neighborDepth;
                    } else {
                        return Math.max(v, neighborDepth);
                    }
                });
            }
        }

        return depthMap;
    }
}
