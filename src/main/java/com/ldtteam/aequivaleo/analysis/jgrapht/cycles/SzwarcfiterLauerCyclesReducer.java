package com.ldtteam.aequivaleo.analysis.jgrapht.cycles;

import org.apache.logging.log4j.util.TriConsumer;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.DirectedSimpleCycles;
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;

import java.util.List;
import java.util.function.BiFunction;

public class SzwarcfiterLauerCyclesReducer<G extends Graph<V, E>, V, E> extends AbstractJGraphTDirectedCyclesReducer<G, V, E> {


    public SzwarcfiterLauerCyclesReducer(BiFunction<G, List<V>, V> vertexReplacerFunction, TriConsumer<V, V, V> onNeighborNodeReplacedCallback) {
        super(vertexReplacerFunction, onNeighborNodeReplacedCallback);
    }

    public SzwarcfiterLauerCyclesReducer(BiFunction<G, List<V>, V> vertexReplacerFunction, TriConsumer<V, V, V> onNeighborNodeReplacedCallback, boolean reduceSingularCycle) {
        super(vertexReplacerFunction, onNeighborNodeReplacedCallback, reduceSingularCycle);
    }

    @Override
    protected DirectedSimpleCycles<V, E> createCycleDetector(G graph) {
        return new SzwarcfiterLauerSimpleCycles<>(graph);
    }
}
