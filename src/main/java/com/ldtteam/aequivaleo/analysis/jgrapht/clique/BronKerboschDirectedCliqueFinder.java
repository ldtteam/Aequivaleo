package com.ldtteam.aequivaleo.analysis.jgrapht.clique;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class BronKerboschDirectedCliqueFinder<V, E> extends
        BaseBronKerboschDirectedCliqueFinder<V, E>
{
    /**
     * Constructs a new clique finder.
     *
     * @param graph the input graph; must be simple
     */
    public BronKerboschDirectedCliqueFinder(Graph<V, E> graph)
    {
        this(graph, 0L, TimeUnit.SECONDS);
    }

    /**
     * Constructs a new clique finder.
     *
     * @param graph the input graph; must be simple
     * @param timeout the maximum time to wait, if zero no timeout
     * @param unit the time unit of the timeout argument
     */
    public BronKerboschDirectedCliqueFinder(Graph<V, E> graph, long timeout, TimeUnit unit)
    {
        super(graph, timeout, unit);
    }

    /**
     * Lazily execute the enumeration algorithm.
     */
    @Override
    protected void lazyRun()
    {
        if (allMaximalCliques == null) {
            if (!GraphTests.isSimple(graph)) {
                throw new IllegalArgumentException("Graph must be simple");
            }
            allMaximalCliques = new ArrayList<>();

            long nanosTimeLimit;
            try {
                nanosTimeLimit = Math.addExact(System.nanoTime(), nanos);
            } catch (ArithmeticException ignore) {
                nanosTimeLimit = Long.MAX_VALUE;
            }

            findCliques(
                    new ArrayList<>(), new ArrayList<>(graph.vertexSet()), new ArrayList<>(),
                    nanosTimeLimit);
        }
    }

    private void findCliques(
            List<V> potentialClique, List<V> candidates, List<V> alreadyFound,
            final long nanosTimeLimit)
    {
        /*
         * Termination condition: check if any already found node is connected to all candidate
         * nodes.
         */
        for (V v : alreadyFound) {
            if (candidates.stream().allMatch(c -> graph.containsEdge(v, c) && graph.containsEdge(c, v))) {
                return;
            }
        }

        /*
         * Check each candidate
         */
        for (V candidate : new ArrayList<>(candidates)) {
            /*
             * Check if timeout
             */
            if (nanosTimeLimit - System.nanoTime() < 0) {
                timeLimitReached = true;
                return;
            }

            List<V> newCandidates = new ArrayList<>();
            List<V> newAlreadyFound = new ArrayList<>();

            // move candidate node to potentialClique
            potentialClique.add(candidate);
            candidates.remove(candidate);

            // create newCandidates by removing nodes in candidates not
            // connected to candidate node
            for (V newCandidate : candidates) {
                if (graph.containsEdge(candidate, newCandidate) && graph.containsEdge(newCandidate, candidate)) {
                    newCandidates.add(newCandidate);
                }
            }

            // create newAlreadyFound by removing nodes in alreadyFound
            // not connected to candidate node
            for (V newFound : alreadyFound) {
                if (graph.containsEdge(candidate, newFound) && graph.containsEdge(newFound, candidate)) {
                    newAlreadyFound.add(newFound);
                }
            }

            // if newCandidates and newAlreadyFound are empty
            if (newCandidates.isEmpty() && newAlreadyFound.isEmpty()) {
                // potential clique is maximal clique
                Set<V> maximalClique = new HashSet<>(potentialClique);
                if (maximalClique.size() > 1) {
                    allMaximalCliques.add(maximalClique);
                    maxSize = Math.max(maxSize, maximalClique.size());
                }
            } else {
                // recursive call
                findCliques(potentialClique, newCandidates, newAlreadyFound, nanosTimeLimit);
            }

            // move candidate node from potentialClique to alreadyFound
            alreadyFound.add(candidate);
            potentialClique.remove(candidate);
        }
    }

}
