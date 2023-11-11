package com.ldtteam.aequivaleo.analysis.jgrapht.clique;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MaximalCliqueEnumerationAlgorithm;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class BaseBronKerboschDirectedCliqueFinder<V, E> implements
        MaximalCliqueEnumerationAlgorithm<V, E> {
    /**
     * The underlying graph
     */
    protected final Graph<V, E> graph;
    /**
     * Timeout in nanoseconds
     */
    protected final long nanos;
    /**
     * Whether the last computation terminated due to a time limit.
     */
    protected boolean timeLimitReached;
    /**
     * The result
     */
    protected List<Set<V>> allMaximalCliques;
    /**
     * Size of biggest maximal clique found.
     */
    protected int maxSize;

    /**
     * Constructor
     *
     * @param graph   the input graph; must be simple
     * @param timeout the maximum time to wait, if zero no timeout
     * @param unit    the time unit of the timeout argument
     */
    public BaseBronKerboschDirectedCliqueFinder(Graph<V, E> graph, long timeout, TimeUnit unit) {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        if (timeout == 0L) {
            this.nanos = Long.MAX_VALUE;
        } else {
            this.nanos = unit.toNanos(timeout);
        }
        if (this.nanos < 1L) {
            throw new IllegalArgumentException("Invalid timeout, must be positive");
        }
        this.timeLimitReached = false;
    }

    @Override
    public Iterator<Set<V>> iterator() {
        lazyRun();
        return allMaximalCliques.iterator();
    }

    /**
     * Create an iterator which returns only the maximum cliques of a graph. The iterator computes
     * all maximal cliques and then filters them by the size of the maximum found clique.
     *
     * @return an iterator which returns only the maximum cliques of a graph
     */
    public Iterator<Set<V>> maximumIterator() {
        lazyRun();
        return allMaximalCliques.stream().filter(c -> c.size() == maxSize).iterator();
    }

    /**
     * Check the computation has stopped due to a time limit or due to computing all maximal
     * cliques.
     *
     * @return true if the computation has stopped due to a time limit, false otherwise
     */
    public boolean isTimeLimitReached() {
        return timeLimitReached;
    }

    /**
     * Lazily start the computation.
     */
    protected abstract void lazyRun();
}