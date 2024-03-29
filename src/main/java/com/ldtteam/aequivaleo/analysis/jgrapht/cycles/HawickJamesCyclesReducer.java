package com.ldtteam.aequivaleo.analysis.jgrapht.cycles;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import org.apache.logging.log4j.util.TriConsumer;
import org.jgrapht.alg.cycle.DirectedSimpleCycles;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;

import java.util.List;
import java.util.function.BiFunction;

@Deprecated(forRemoval = true)
public class HawickJamesCyclesReducer extends AbstractJGraphTDirectedCyclesReducer {


    public HawickJamesCyclesReducer(BiFunction<IGraph, List<INode>, INode> vertexReplacerFunction, TriConsumer<INode, INode, INode> onNeighborNodeReplacedCallback) {
        super(vertexReplacerFunction, onNeighborNodeReplacedCallback);
    }

    public HawickJamesCyclesReducer(BiFunction<IGraph, List<INode>, INode> vertexReplacerFunction, TriConsumer<INode, INode, INode> onNeighborNodeReplacedCallback, boolean reduceSingularCycle) {
        super(vertexReplacerFunction, onNeighborNodeReplacedCallback, reduceSingularCycle);
    }

    @Override
    protected DirectedSimpleCycles<INode, IEdge> createCycleDetector(IGraph graph) {
        return new HawickJamesSimpleCycles<>(graph);
    }
}
