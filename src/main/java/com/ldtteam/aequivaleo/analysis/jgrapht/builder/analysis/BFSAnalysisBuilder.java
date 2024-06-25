package com.ldtteam.aequivaleo.analysis.jgrapht.builder.analysis;

import com.ldtteam.aequivaleo.analysis.StatCollector;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth.BFSDepthMapBuilder;
import com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth.FullScanDepthMapBuilder;
import com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth.IDepthMapBuilder;
import com.ldtteam.aequivaleo.analysis.jgrapht.core.IAnalysisState;
import com.ldtteam.aequivaleo.analysis.jgrapht.core.SimpleAnalysisState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BFSAnalysisBuilder implements IAnalysisBuilder {
    private final IGraph iteratingGraph;
    private final INode sourceNode;
    private final IGraph analysisGraph;

    public BFSAnalysisBuilder(IGraph iteratingGraph, INode sourceNode) {
        this(iteratingGraph, sourceNode, iteratingGraph);
    }

    public BFSAnalysisBuilder(IGraph iteratingGraph, INode sourceNode, IGraph analysisGraph) {
        this.iteratingGraph = iteratingGraph;
        this.sourceNode = sourceNode;
        this.analysisGraph = analysisGraph;

        if (iteratingGraph != this.analysisGraph) {
            validateGraphs();
        }
    }

    public void validateGraphs() {
        for (INode node : iteratingGraph.vertexSet()) {
            if (!this.analysisGraph.containsVertex(node))
                throw new IllegalStateException("The analysis iterators iterating graph contains a vertex which is not in its analysis graph: " + node);
        }

        for (IEdge edge : iteratingGraph.edgeSet()) {
            if (!this.analysisGraph.containsEdge(edge))
                throw new IllegalStateException("The analysis iterators iterating graph contains an edge which is not in its analysis graph: " + edge);
        }
    }

    @Override
    public void analyse(StatCollector statCollector) {
        final IDepthMapBuilder<INode> depthMapBuilder = new BFSDepthMapBuilder(iteratingGraph, sourceNode);
        final Map<INode, Integer> depthMap = depthMapBuilder.calculateDepthMap();

        final List<INode> toProcess = new ArrayList<>(iteratingGraph.vertexSet());
        final IAnalysisState state = new SimpleAnalysisState(statCollector, false);
        toProcess.sort(Comparator.comparing((Function<INode, Integer>) depthMap::get).thenComparing(INode::type));
        for (INode node : toProcess) {
            node.analyze(state);
        }
    }
}
