package com.ldtteam.aequivaleo.analyzer.jgrapht.iterator;

import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.Edge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisGraphNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.traverse.CrossComponentIterator;

import java.util.*;

public class AnalysisBFSGraphIterator<N> extends CrossComponentIterator<IAnalysisGraphNode<N>, Edge, AnalysisBFSGraphIterator.SearchNodeData>
{

    private static final Logger LOGGER = LogManager.getLogger();


    private final Queue<IAnalysisGraphNode<N>> completeQueue = new ArrayDeque<>();
    private final LinkedList<IAnalysisGraphNode<N>> incompleteQueue = new LinkedList<>();

    public AnalysisBFSGraphIterator(final Graph<IAnalysisGraphNode<N>, Edge> g, final IAnalysisGraphNode<N> sourceGraphNode)
    {
        super(g, sourceGraphNode);
    }

    @Override
    protected boolean isConnectedComponentExhausted()
    {
        return completeQueue.isEmpty() && incompleteQueue.isEmpty();
    }

    @Override
    protected void encounterVertex(final IAnalysisGraphNode<N> vertex, final Edge edge)
    {
        LOGGER.debug(String.format("Initially encountered: %s", vertex));

        int depth = (edge == null ? 0
                       : getSeenData(Graphs.getOppositeVertex(graph, edge, vertex)).depth + 1);
        putSeenData(vertex, new AnalysisBFSGraphIterator.SearchNodeData(edge, depth));

        if (vertex.canResultBeCalculated(getGraph()))
            completeQueue.offer(vertex);
        else
            incompleteQueue.offer(vertex);
    }

    @Override
    protected IAnalysisGraphNode<N> provideNextVertex()
    {
        final IAnalysisGraphNode<N> vertex = innerProvideNextVertex();

        vertex.determineResult(getGraph());
        final Optional<N> result = vertex.getResultingValue();
        LOGGER.debug(String.format("  > Determined result to be: %s", result.isPresent() ? result.get() : "<MISSING>"));
        vertex.onReached(getGraph());

        return vertex;
    }

    private IAnalysisGraphNode<N> innerProvideNextVertex() {
        if (!completeQueue.isEmpty())
        {
            final IAnalysisGraphNode<N> complete = completeQueue.poll();
            LOGGER.debug(String.format("Accessing next complete node: %s", complete));
            return complete;
        }

        final IAnalysisGraphNode<N> incomplete = incompleteQueue.removeFirst();
        LOGGER.debug(String.format("Accessing next incomplete node: %s", incomplete));
        return incomplete;
    }

    @Override
    protected void encounterVertexAgain(final IAnalysisGraphNode<N> vertex, final Edge edge)
    {
        if (!vertex.getResultingValue().isPresent() && incompleteQueue.contains(vertex) && vertex.canResultBeCalculated(getGraph())) {
            LOGGER.debug(String.format("Upgrading completion state from incomplete to complete on the queued vertex: %s", vertex));
            incompleteQueue.remove(vertex);
            completeQueue.offer(vertex);
        }
    }

    static class SearchNodeData
    {
        /**
         * Edge to parent
         */
        final Edge edge;
        /**
         * Depth of node in search tree
         */
        final int  depth;

        SearchNodeData(Edge edge, int depth)
        {
            this.edge = edge;
            this.depth = depth;
        }
    }
}
