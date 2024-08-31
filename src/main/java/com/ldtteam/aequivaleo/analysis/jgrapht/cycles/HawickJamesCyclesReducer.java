package com.ldtteam.aequivaleo.analysis.jgrapht.cycles;

import org.apache.logging.log4j.util.TriConsumer;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.DirectedSimpleCycles;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Deprecated(forRemoval = true)
public class HawickJamesCyclesReducer<G extends Graph<V, E>, V, E> extends AbstractJGraphTDirectedCyclesReducer<G, V, E> {


    public HawickJamesCyclesReducer(BiFunction<G, List<V>, V> vertexReplacerFunction, TriConsumer<V, V, V> onNeighborNodeReplacedCallback) {
        super(vertexReplacerFunction, onNeighborNodeReplacedCallback);
    }

    public HawickJamesCyclesReducer(BiFunction<G, List<V>, V> vertexReplacerFunction, TriConsumer<V, V, V> onNeighborNodeReplacedCallback, boolean reduceSingularCycle) {
        super(vertexReplacerFunction, onNeighborNodeReplacedCallback, reduceSingularCycle);
    }

    @Override
    protected DirectedSimpleCycles<V, E> createCycleDetector(G graph, Map<V, Integer> depthMap) {
        return new HawickJamesSimpleCycles<>(graph);
    }
}
