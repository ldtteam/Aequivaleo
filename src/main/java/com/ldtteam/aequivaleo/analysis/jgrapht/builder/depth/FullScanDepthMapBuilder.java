package com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth;

import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.traverse.CrossComponentIterator;

import java.util.*;
import java.util.stream.Collectors;

public class FullScanDepthMapBuilder<G extends Graph<V, E>, V, E> implements IDepthMapBuilder<V> {

    private final G graph;
    private final V startNode;

    public FullScanDepthMapBuilder(G graph, V startNode) {
        this.graph = graph;
        this.startNode = startNode;
    }

    @Override
    public Map<V, Integer> calculateDepthMap() {
        final Iterator iterator = new Iterator(graph, startNode);

        while (iterator.hasNext()) {
            iterator.next();
        }

        final Map<V, Integer> depthMap = new HashMap<>(graph.vertexSet().size());
        for (V node : graph.vertexSet()) {
            depthMap.put(node, iterator.getDepth(node));
        }

        return depthMap;
    }


    private static final class Iterator<G extends Graph<V, E>, V, E> extends CrossComponentIterator<V, E, List<V>> {

        private final LinkedList<V> nodes = new LinkedList<>();

        public Iterator(G g, V startVertex) {
            super(g, startVertex);
            putSeenData(startVertex, new LinkedList<>());
        }

        @Override
        protected boolean isConnectedComponentExhausted() {
            return nodes.isEmpty();
        }

        @Override
        protected void encounterVertex(V vertex, E edge) {
            final List<V> newPath = buildPathOverEdge(edge);
            putSeenData(vertex, newPath);
            nodes.add(vertex);
        }

        @NotNull
        private List<V> buildPathOverEdge(E edge) {
            if (edge == null)
                return new LinkedList<>();

            final V source = getGraph().getEdgeSource(edge);
            final List<V> path = getSeenData(source);

            final List<V> newPath = new LinkedList<>(path);
            newPath.add(source);
            return newPath;
        }

        @Override
        protected V provideNextVertex() {
            return nodes.pollLast();
        }

        @Override
        protected Set<E> selectOutgoingEdges(V vertex) {
            final Set<E> edges = super.selectOutgoingEdges(vertex);
            if (edges.isEmpty())
                return edges;

            final List<V> path = getSeenData(vertex);
            return edges.stream()
                    .filter(edge -> !path.contains(getGraph().getEdgeTarget(edge)))
                    .collect(Collectors.toSet());
        }

        @Override
        protected void encounterVertexAgain(V vertex, E edge) {
            final List<V> currentPath = getSeenData(vertex);
            final List<V> newPath = buildPathOverEdge(edge);
            if (newPath.size() > currentPath.size()) {
                putSeenData(vertex, newPath);
            }
        }

        public Integer getDepth(V node) {
            final List<V> path = getSeenData(node);
            if (path == null)
                return Integer.MAX_VALUE;

            return path.size();
        }

    }

}
