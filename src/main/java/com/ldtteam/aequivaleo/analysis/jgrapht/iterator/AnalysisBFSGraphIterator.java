package com.ldtteam.aequivaleo.analysis.jgrapht.iterator;

import com.google.common.collect.Maps;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INodeWithoutResult;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graphs;
import org.jgrapht.traverse.CrossComponentIterator;

import java.util.*;

public class AnalysisBFSGraphIterator extends CrossComponentIterator<INode, IEdge, AnalysisBFSGraphIterator.SearchNodeData>
{
    private static final Logger LOGGER = LogManager.getLogger();

    private final INode startNode;

    private final Map<INode, Integer> depthMap = Maps.newHashMap();

    private final Queue<INode> completeQueue = new ArrayDeque<>();
    private final PriorityQueue<INode> incompleteQueue = new PriorityQueue<>(Comparator.comparing(n -> depthMap.getOrDefault(n, Integer.MAX_VALUE)));
    private final IGraph analysisGraph;

    public AnalysisBFSGraphIterator(final IGraph iteratingGraph, final INode sourceGraphNode) {
        this(iteratingGraph, sourceGraphNode, iteratingGraph);
    }

    public AnalysisBFSGraphIterator(final IGraph iteratingGraph, final INode sourceGraphNode, final IGraph analysisGraph)
    {
        super(iteratingGraph, sourceGraphNode);

        final DepthMapBFSIterator depthMapBFSIterator = new DepthMapBFSIterator(iteratingGraph, sourceGraphNode);
        while(depthMapBFSIterator.hasNext())
        {
            depthMapBFSIterator.next();
        }
        this.depthMap.putAll(depthMapBFSIterator.getDepthMap());
        this.startNode = sourceGraphNode;
        this.analysisGraph = analysisGraph;

        if (this.getGraph() != this.analysisGraph) {
            validateGraphs();
        }
    }

    public void validateGraphs() {
        for (INode node : getGraph().vertexSet()) {
            if (!this.analysisGraph.containsVertex(node))
                throw new IllegalStateException("The analysis iterators iterating graph contains a vertex which is not in its analysis graph: " + node);
        }

        for (IEdge edge : getGraph().edgeSet()) {
            if (!this.analysisGraph.containsEdge(edge))
                throw new IllegalStateException("The analysis iterators iterating graph contains an edge which is not in its analysis graph: " + edge);
        }
    }

    @Override
    protected boolean isConnectedComponentExhausted()
    {
        return completeQueue.isEmpty() && incompleteQueue.isEmpty();
    }

    public IGraph getGraph() {
        return (IGraph) super.getGraph();
    }

    @Override
    protected void encounterVertex(final INode vertex, final IEdge edge)
    {
        AnalysisLogHandler.debug(LOGGER, String.format("Initially encountered: %s", vertex));

        int depth = (edge == null ? 0
                       : getSeenData(Graphs.getOppositeVertex(graph, edge, vertex)).depth + 1);
        putSeenData(vertex, new AnalysisBFSGraphIterator.SearchNodeData(edge, depth));

        if (vertex.canResultBeCalculated(getGraph()) || vertex == startNode)
            completeQueue.offer(vertex);
        else
            incompleteQueue.offer(vertex);
    }

    @Override
    protected INode provideNextVertex()
    {
        final INode vertex = innerProvideNextVertex();

        vertex.determineResult(this.analysisGraph);
        if (vertex instanceof INodeWithoutResult) {
            AnalysisLogHandler.debug(LOGGER, String.format("  > Processed node without result: %s", vertex));
        }
        else
        {
            final Optional<Set<CompoundInstance>> result = vertex.getResultingValue();
            AnalysisLogHandler.debug(LOGGER, String.format("  > Determined result to be: %s", result.isPresent() ? result.get() : "<MISSING>"));
        }
        vertex.onReached(this.analysisGraph);

        return vertex;
    }

    private INode innerProvideNextVertex() {
        if (!completeQueue.isEmpty())
        {
            final INode complete = completeQueue.poll();
            AnalysisLogHandler.debug(LOGGER, String.format("Accessing next complete node: %s", complete));
            return complete;
        }

        final INode incomplete = incompleteQueue.poll();
        AnalysisLogHandler.debug(LOGGER, String.format("Accessing next incomplete node: %s", incomplete));
        return incomplete;
    }

    @Override
    protected void encounterVertexAgain(final INode vertex, final IEdge edge)
    {
        if (!vertex.getResultingValue().isPresent() && incompleteQueue.contains(vertex) && vertex.canResultBeCalculated(getGraph())) {
            AnalysisLogHandler.debug(LOGGER, String.format("Upgrading completion state from incomplete to complete on the queued vertex: %s", vertex));
            incompleteQueue.remove(vertex);
            completeQueue.offer(vertex);
        }
    }

    static class SearchNodeData
    {
        /**
         * Edge to parent
         */
        final IEdge edge;
        /**
         * Depth of node in search tree
         */
        final int  depth;

        SearchNodeData(IEdge edge, int depth)
        {
            this.edge = edge;
            this.depth = depth;
        }
    }
}
