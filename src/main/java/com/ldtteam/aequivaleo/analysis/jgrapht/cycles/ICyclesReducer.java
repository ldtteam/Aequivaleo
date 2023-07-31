package com.ldtteam.aequivaleo.analysis.jgrapht.cycles;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.core.IAnalysisEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.edge.Edge;
import org.jgrapht.Graph;

public interface ICyclesReducer {
    void reduce(IGraph graph);

    boolean reduceOnce(IGraph graph);
}
