package com.ldtteam.aequivaleo.analysis.jgrapht.cycles;

import com.ldtteam.aequivaleo.analysis.jgrapht.core.IAnalysisEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.edge.Edge;
import org.jgrapht.Graph;

public interface ICyclesReducer<G extends Graph<V, E>, V, E extends IAnalysisEdge> {
    void reduce(G graph);

    boolean reduceOnce(G graph);
}
