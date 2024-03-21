package com.ldtteam.aequivaleo.analysis.jgrapht.cycles;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.core.IAnalysisEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.edge.Edge;
import org.jgrapht.Graph;

public interface ICyclesReducer {
    void reduce(IGraph graph, INode startNode);

    boolean reduceOnce(IGraph graph, INode startNode);
}
