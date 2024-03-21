package com.ldtteam.aequivaleo.analysis.jgrapht.builder.analysis;

import com.ldtteam.aequivaleo.analysis.StatCollector;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INodeWithoutResult;
import com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth.BFSDepthMapBuilder;
import com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth.CrossComponentDepthMapBuilder;
import com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth.IDepthMapBuilder;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;
import java.util.function.Function;

public class BFSAnalysisBuilder implements IAnalysisBuilder {
    private static final Logger LOGGER = LogManager.getLogger();

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
        final IDepthMapBuilder depthMapBuilder = new CrossComponentDepthMapBuilder(iteratingGraph, sourceNode);
        final Map<INode, Integer> depthMap = depthMapBuilder.calculateDepthMap();

        final List<INode> incomplete = new ArrayList<>();

        final List<INode> toProcess = new ArrayList<>(iteratingGraph.vertexSet());
        final Set<INode> nodesToProcess = new HashSet<>(toProcess);
        toProcess.sort(Comparator.comparing((Function<INode, Integer>) depthMap::get).thenComparing(INode::getInertImportance));
        toProcess.forEach(node -> {
            if (node.canResultBeCalculated(analysisGraph, nodesToProcess)) {
                calculateResult(node, statCollector);
            } else {
                incomplete.add(node);
                nodesToProcess.remove(node);
            }
        });

        incomplete.sort(Comparator.comparing((Function<INode, Integer>) depthMap::get).thenComparing(INode::getInertImportance));
        incomplete.forEach(node -> this.calculateResult(node, statCollector));
    }

    private void calculateResult(final INode node, final StatCollector statCollector) {
        node.determineResult(this.analysisGraph);
        if (node instanceof INodeWithoutResult) {
            AnalysisLogHandler.debug(LOGGER, String.format("  > Processed node without result: %s", node));
        }
        else
        {
            final Optional<Set<CompoundInstance>> result = node.getResultingValue();
            AnalysisLogHandler.debug(LOGGER, String.format("  > Determined result to be: %s", result.isPresent() ? result.get() : "<MISSING>"));
        }
        node.onReached(this.iteratingGraph);
        node.collectStats(statCollector);
    }
}
