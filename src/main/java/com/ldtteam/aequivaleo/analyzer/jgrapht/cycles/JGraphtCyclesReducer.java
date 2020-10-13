package com.ldtteam.aequivaleo.analyzer.jgrapht.cycles;

import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.DirectedSimpleCycles;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class JGraphtCyclesReducer<V, E>
{

    private final BiFunction<Graph<V, E>, Set<V>, V> vertexReplacerProducer;

    public JGraphtCyclesReducer(final BiFunction<Graph<V, E>, Set<V>, V> vertexReplacerProducer) {
        this.vertexReplacerProducer = vertexReplacerProducer;
    }

    public void reduce(final Graph<V, E> graph) {

    }

    private boolean reduceOnce(final Graph<V, E> graph) {
        final DirectedSimpleCycles<V, E> cycleFinder = new HawickJamesSimpleCycles<>(graph);
        List<List<V>> sortedCycles = cycleFinder.findSimpleCycles();
        sortedCycles.sort(Comparator.comparing(List::size));

        if (sortedCycles.isEmpty())
            return false;

        final List<V> cycleToPrune = sortedCycles.get(0);
    }
}
