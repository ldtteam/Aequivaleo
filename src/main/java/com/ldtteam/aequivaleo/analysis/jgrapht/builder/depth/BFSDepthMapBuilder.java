package com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.CrossComponentIterator;

import java.util.*;

public class BFSDepthMapBuilder<G extends Graph<V, E>, V, E> implements IDepthMapBuilder<V> {

    private final DirectedAcyclicGraph<V, E> graph;
    private final V sourceVertex;

    public BFSDepthMapBuilder(final G graph, V sourceVertex) {
        this.graph = createAnalysisGraph(graph);
        this.sourceVertex = sourceVertex;

        if (graph.inDegreeOf(sourceVertex) != 0)
            throw new IllegalArgumentException("Source vertex must have in-degree of 0, it currently has: " + graph.inDegreeOf(sourceVertex));
    }

    private DirectedAcyclicGraph<V, E> createAnalysisGraph(final G graph) {
        final DirectedAcyclicGraph<V, E> result = new DirectedAcyclicGraph<>(null, null, true, false);
        for (V vertex : graph.vertexSet()) {
            result.addVertex(vertex);
        }

        for (E edge : graph.edgeSet()) {
            final V source = graph.getEdgeSource(edge);
            final V target = graph.getEdgeTarget(edge);
            result.addEdge(source, target, edge);
        }

        return result;
    }

    @Override
    public Map<V, Integer> calculateDepthMap() {
        final Object2IntMap<V> depthMap = new Object2IntOpenHashMap<>(graph.vertexSet().size());

        final BreadthFirstIteratorWithLevel<V, E> iterator = new BreadthFirstIteratorWithLevel<>(graph, sourceVertex);
        while (iterator.hasNext()) {
            iterator.next();
        }

        for (V vertex : graph.vertexSet()) {
            final int depth = iterator.getDepth(vertex);
            if (depth == -1)
                continue;

            depthMap.put(vertex, depth);
        }

        return depthMap;
    }


    /**
     * A breadth-first iterator for a directed or undirected graph which supports querying the depth of a vertex
     * in the search tree.
     *
     * <p>
     * For this iterator to work correctly the graph must not be modified during iteration. Currently,
     * there are no means to ensure that, nor to fail-fast. The results of such modifications are
     * undefined.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    public static class BreadthFirstIteratorWithLevel<V, E>
            extends CrossComponentIterator<V, E, Integer> {
        private Deque<V> queue = new ArrayDeque<>();

        /**
         * Creates a new breadth-first iterator for the specified graph.
         *
         * @param g the graph to be iterated.
         */
        public BreadthFirstIteratorWithLevel(Graph<V, E> g) {
            this(g, (V) null);
        }

        /**
         * Creates a new breadth-first iterator for the specified graph. Iteration will start at the
         * specified start vertex and will be limited to the connected component that includes that
         * vertex. If the specified start vertex is <code>null</code>, iteration will start at an
         * arbitrary vertex and will not be limited, that is, will be able to traverse all the graph.
         *
         * @param g           the graph to be iterated.
         * @param startVertex the vertex iteration to be started.
         */
        public BreadthFirstIteratorWithLevel(Graph<V, E> g, V startVertex) {
            super(g, startVertex);
        }

        /**
         * Creates a new breadth-first iterator for the specified graph. Iteration will start at the
         * specified start vertices and will be limited to the connected component that includes those
         * vertices. If the specified start vertices is <code>null</code>, iteration will start at an
         * arbitrary vertex and will not be limited, that is, will be able to traverse all the graph.
         *
         * @param g             the graph to be iterated.
         * @param startVertices the vertices iteration to be started.
         */
        public BreadthFirstIteratorWithLevel(Graph<V, E> g, Iterable<V> startVertices) {
            super(g, startVertices);
        }

        /**
         * @see CrossComponentIterator#isConnectedComponentExhausted()
         */
        @Override
        protected boolean isConnectedComponentExhausted() {
            return queue.isEmpty();
        }

        /**
         * @see CrossComponentIterator#encounterVertex(Object, Object)
         */
        @Override
        protected void encounterVertex(V vertex, E edge) {
            int current = getDepth(vertex);
            int depth = (edge == null ? 0 : getDepth(Graphs.getOppositeVertex(graph, edge, vertex)) + 1);

            int newDepth = Math.max(current, depth);

            if (newDepth > current) {
                putSeenData(vertex, newDepth);
                queue.add(vertex);
            }
        }

        /**
         * @see CrossComponentIterator#encounterVertexAgain(Object, Object)
         */
        @Override
        protected void encounterVertexAgain(V vertex, E edge) {
            encounterVertex(vertex, edge);
        }

        /**
         * @see CrossComponentIterator#provideNextVertex()
         */
        @Override
        protected V provideNextVertex() {
            return queue.removeFirst();
        }

        public int getDepth(V v) {
            if (getSeenData(v) == null)
                return -1;

            return getSeenData(v);
        }
    }
}
