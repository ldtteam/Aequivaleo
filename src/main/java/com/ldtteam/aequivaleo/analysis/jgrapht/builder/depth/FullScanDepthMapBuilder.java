package com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.traverse.CrossComponentIterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class FullScanDepthMapBuilder implements IDepthMapBuilder {

    private final IGraph graph;
    private final INode startNode;

    public FullScanDepthMapBuilder(IGraph graph, INode startNode) {
        this.graph = graph;
        this.startNode = startNode;
    }

    @Override
    public Map<INode, Integer> calculateDepthMap() {
        final Iterator iterator = new Iterator(graph, startNode);

        while (iterator.hasNext()) {
            iterator.next();
        }

        final Map<INode, Integer> depthMap = new HashMap<>(graph.vertexSet().size());
        for (INode node : graph.vertexSet()) {
            depthMap.put(node, iterator.getDepth(node));
        }

        return depthMap;
    }


    private static final class Iterator extends CrossComponentIterator<INode, IEdge, List<INode>> {

        private final LinkedList<INode> nodes = new LinkedList<>();

        public Iterator(Graph<INode, IEdge> g, INode startVertex) {
            super(g, startVertex);
            putSeenData(startVertex, new LinkedList<>());
        }

        @Override
        protected boolean isConnectedComponentExhausted() {
            return nodes.isEmpty();
        }

        @Override
        protected void encounterVertex(INode vertex, IEdge edge) {
            final List<INode> newPath = buildPathOverEdge(edge);
            putSeenData(vertex, newPath);
            nodes.add(vertex);
        }

        @NotNull
        private List<INode> buildPathOverEdge(IEdge edge) {
            if (edge == null)
                return new LinkedList<>();

            final INode source = getGraph().getEdgeSource(edge);
            final List<INode> path = getSeenData(source);

            final List<INode> newPath = new LinkedList<>(path);
            newPath.add(source);
            return newPath;
        }

        @Override
        protected INode provideNextVertex() {
            return nodes.pollLast();
        }

        @Override
        protected Set<IEdge> selectOutgoingEdges(INode vertex) {
            final Set<IEdge> edges = super.selectOutgoingEdges(vertex);
            if (edges.isEmpty())
                return edges;

            final List<INode> path = getSeenData(vertex);
            return edges.stream()
                    .filter(edge -> !path.contains(getGraph().getEdgeTarget(edge)))
                    .collect(Collectors.toSet());
        }

        @Override
        protected void encounterVertexAgain(INode vertex, IEdge edge) {
            final List<INode> currentPath = getSeenData(vertex);
            final List<INode> newPath = buildPathOverEdge(edge);
            if (newPath.size() > currentPath.size()) {
                putSeenData(vertex, newPath);
            }
        }

        public Integer getDepth(INode node) {
            final List<INode> path = getSeenData(node);
            if (path == null)
                return Integer.MAX_VALUE;

            return path.size();
        }

    }

}
