package com.ldtteam.aequivaleo.analyzer.jgrapht.iterator;

import com.google.common.collect.Maps;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
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


    public AnalysisBFSGraphIterator(final IGraph g, final INode sourceGraphNode)
    {
        super(g, sourceGraphNode);

        final DepthMapBFSIterator depthMapBFSIterator = new DepthMapBFSIterator(g, sourceGraphNode);
        while(depthMapBFSIterator.hasNext())
        {
            depthMapBFSIterator.next();
        }
        depthMap.putAll(depthMapBFSIterator.getDepthMap());
        startNode = sourceGraphNode;
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
        LOGGER.debug(String.format("Initially encountered: %s", vertex));

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

        vertex.determineResult(getGraph());
        final Optional<Set<CompoundInstance>> result = vertex.getResultingValue();
        LOGGER.debug(String.format("  > Determined result to be: %s", result.isPresent() ? result.get() : "<MISSING>"));
        vertex.onReached(getGraph());

        return vertex;
    }

    private INode innerProvideNextVertex() {
        if (!completeQueue.isEmpty())
        {
            final INode complete = completeQueue.poll();
            LOGGER.debug(String.format("Accessing next complete node: %s", complete));
            return complete;
        }

        final INode incomplete = incompleteQueue.poll();
        LOGGER.debug(String.format("Accessing next incomplete node: %s", incomplete));
        return incomplete;
    }

    @Override
    protected void encounterVertexAgain(final INode vertex, final IEdge edge)
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
