package com.ldtteam.aequivaleo.analysis.jgrapht.iterator;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jgrapht.Graphs;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.Map;

public class BFSDepthMapBuilder implements IDepthMapBuilder {
    
    private final IGraph graph;
    private final INode sourceVertex;

    public BFSDepthMapBuilder(final IGraph graph, INode sourceVertex) {
        this.graph = graph;
        this.sourceVertex = sourceVertex;
    }

    @Override
    public Map<INode, Integer> calculateDepthMap() {
        final Object2IntMap<INode> depthMap = new Object2IntArrayMap<>();

        final BreadthFirstIterator<INode, IEdge> iterator = new BreadthFirstIterator<>(graph, sourceVertex) {
            @Override
            protected void encounterVertex(INode vertex, IEdge edge) {
                if (edge == null) {
                    depthMap.put(vertex, 0);
                    return;
                }

                final INode source = Graphs.getOppositeVertex(graph, edge, vertex);
                if (!depthMap.containsKey(source))
                    throw new IllegalStateException("Unknown depth map vertex: " + vertex + ". Did BFS iteration fail?");

                final int sourceDepth = depthMap.getInt(source);
                depthMap.put(vertex, sourceDepth + 1);
            }
        };

        while(iterator.hasNext()) { iterator.next(); }

        return depthMap;
    }
}
