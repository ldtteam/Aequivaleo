package com.ldtteam.aequivaleo.analysis.jgrapht.cycles;

import org.apache.logging.log4j.util.TriConsumer;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.DirectedSimpleCycles;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class DFSCyclesReducer<G extends Graph<V, E>, V, E> extends AbstractJGraphTDirectedCyclesReducer<G, V, E> {

    private final Consumer<List<V>> onCycleFound;

    public DFSCyclesReducer(BiFunction<G, List<V>, V> vertexReplacerFunction, TriConsumer<V, V, V> onNeighborNodeReplacedCallback, Consumer<List<V>> onCycleFound) {
        super(vertexReplacerFunction, onNeighborNodeReplacedCallback);
        this.onCycleFound = onCycleFound;
    }

    public DFSCyclesReducer(BiFunction<G, List<V>, V> vertexReplacerFunction, TriConsumer<V, V, V> onNeighborNodeReplacedCallback, boolean reduceSingularCycle, Consumer<List<V>> onCycleFound) {
        super(vertexReplacerFunction, onNeighborNodeReplacedCallback, reduceSingularCycle);
        this.onCycleFound = onCycleFound;
    }

    public DFSCyclesReducer(BiFunction<G, List<V>, V> vertexReplacerFunction, TriConsumer<V, V, V> onNeighborNodeReplacedCallback) {
        super(vertexReplacerFunction, onNeighborNodeReplacedCallback);
        this.onCycleFound = null;
    }

    public DFSCyclesReducer(BiFunction<G, List<V>, V> vertexReplacerFunction, TriConsumer<V, V, V> onNeighborNodeReplacedCallback, boolean reduceSingularCycle) {
        super(vertexReplacerFunction, onNeighborNodeReplacedCallback, reduceSingularCycle);
        this.onCycleFound = null;
    }

    @Override
    protected DirectedSimpleCycles<V, E> createCycleDetector(G graph, Map<V, Integer> depthMap) {
        return new DirectedSimpleCycles<>() {

            private static final class Path<V> {

                private final Set<V> contents = new HashSet<>();
                private final LinkedList<V> path = new LinkedList<>();

                public void addFirst(V vertex) {
                    path.addFirst(vertex);
                    contents.add(vertex);
                }

                public boolean contains(V edgeTarget) {
                    return contents.contains(edgeTarget);
                }

                public int indexOfOr(V value, int i) {
                    if (!contains(value))
                        return i;

                    return path.indexOf(value);
                }

                public List<V> getSubPathUntil(V target) {
                    if (!contains(target))
                        throw new IllegalStateException("Path does not contain target vertex");

                    List<V> subPath = new ArrayList<>();

                    for (V vertex : path) {
                        subPath.add(vertex);
                        if (vertex.equals(target))
                            break;
                    }

                    Collections.reverse(subPath);

                    return subPath;
                }

                public void clean(V vertex) {
                    if (!path.peekFirst().equals(vertex))
                        throw new IllegalStateException("Path does not start with vertex");

                    path.removeFirst();
                    contents.remove(vertex);
                }
            }

            private interface ISearchAction<V> {
                void perform(List<List<V>> cycles, Path<V> path, Deque<ISearchAction<V>> next);
            }

            private final class VisitVertex implements ISearchAction<V> {
                private final V vertex;

                private VisitVertex(V vertex) {
                    this.vertex = vertex;
                }

                @Override
                public void perform(
                        List<List<V>> cycles,
                        Path<V> path,
                        Deque<ISearchAction<V>> next) {

                    path.addFirst(vertex);

                    next.addFirst(new CleanVertex(vertex));

                    graph.outgoingEdgesOf(vertex).stream().sorted(
                            Comparator.comparingInt(value -> {
                                final V target = graph.getEdgeTarget(value);
                                return Integer.MAX_VALUE - path.indexOfOr(target, 0);
                            })
                    ).forEach(edge -> {
                        next.addFirst(new VisitEdge(edge));
                    });
                }
            }

            private final class VisitEdge implements ISearchAction<V> {
                private final E edge;

                private VisitEdge(E edge) {
                    this.edge = edge;
                }

                @Override
                public void perform(
                        List<List<V>> cycles,
                        Path<V> path,
                        Deque<ISearchAction<V>> next) {

                    final V target = graph.getEdgeTarget(edge);
                    if (path.contains(target)) {
                        final List<V> cycle = path.getSubPathUntil(target);
                        cycles.add(cycle);
                        reportCycleFound(cycle);
                    } else {
                        next.addFirst(new VisitVertex(target));
                    }
                }
            }

            private final class CleanVertex implements ISearchAction<V> {
                private final V vertex;

                private CleanVertex(V vertex) {
                    this.vertex = vertex;
                }

                @Override
                public void perform(
                        List<List<V>> cycles,
                        Path<V> path,
                        Deque<ISearchAction<V>> next) {
                    path.clean(vertex);
                }
            }

            @Override
            public List<List<V>> findSimpleCycles() {
                final List<List<V>> cycles = new ArrayList<>();
                final Path<V> path = new Path<>();
                final Deque<ISearchAction<V>> next = new ArrayDeque<>();

                final List<V> vertices = new ArrayList<>(graph.vertexSet());
                vertices.sort(Comparator.comparingInt(depthMap::get));

                offer(vertices, next);

                while (!next.isEmpty()) {
                    final ISearchAction<V> head = next.pop();

                    head.perform(cycles, path, next);
                }
                return cycles;
            }

            private void offer(List<V> vertices, Deque<ISearchAction<V>> next) {
                final V vertex = vertices.remove(0);
                next.addFirst(new VisitVertex(vertex));
            }
        };
    }

    private void reportCycleFound(List<V> cycle) {
        if (onCycleFound != null) {
            onCycleFound.accept(cycle);
        }
    }
}
