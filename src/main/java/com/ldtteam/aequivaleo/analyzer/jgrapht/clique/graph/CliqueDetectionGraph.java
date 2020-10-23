package com.ldtteam.aequivaleo.analyzer.jgrapht.clique.graph;

import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IContainerNode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.graph.SimpleAnalysisGraph;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class CliqueDetectionGraph extends SimpleAnalysisGraph<INode, CliqueDetectionEdge>
{
    public CliqueDetectionGraph()
    {
        super(null);
    }
}
