package com.ldtteam.aequivaleo.analysis.jgrapht.iterator;

import com.google.common.collect.Maps;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.builder.depth.IDepthMapBuilder;
import org.jgrapht.Graphs;
import org.jgrapht.traverse.CrossComponentIterator;

import java.util.*;

@Deprecated(forRemoval = true)
public class DepthMapBFSIterator extends CrossComponentIterator<INode, IEdge, DepthMapBFSIterator.SearchNodeData> implements IDepthMapBuilder {
    private final Queue<INode>      completeQueue   = new ArrayDeque<>();
    private final LinkedList<INode> incompleteQueue = new LinkedList<>();

    private final Map<INode, Integer> depthMap = Maps.newHashMap();

    public DepthMapBFSIterator(final IGraph g, final INode sourceGraphNode)
    {
        super(g, sourceGraphNode);
    }

    @Override
    protected boolean isConnectedComponentExhausted()
    {
        if (completeQueue.isEmpty() && !incompleteQueue.isEmpty())
            throw new IllegalStateException("Missing complete queue analysis.");

        return completeQueue.isEmpty();
    }

    public IGraph getGraph() {
        return (IGraph) super.getGraph();
    }

    @Override
    protected void encounterVertex(final INode vertex, final IEdge edge)
    {
        int depth = (edge == null ? 0
                       : getSeenData(Graphs.getOppositeVertex(graph, edge, vertex)).depth + 1);
        putSeenData(vertex, new DepthMapBFSIterator.SearchNodeData(edge, depth));

        if (isComplete(vertex))
            completeQueue.offer(vertex);
        else
            incompleteQueue.offer(vertex);
    }

    private boolean isComplete(final INode node) {
        IGraph iGraph = getGraph();
        for (IEdge iEdge : getGraph().incomingEdgesOf(node))
        {
            INode edgeSource = iGraph.getEdgeSource(iEdge);
            if (!depthMap.containsKey(edgeSource))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    protected INode provideNextVertex()
    {
        final INode vertex = innerProvideNextVertex();

        boolean seen = false;
        int best = 0;
        for (IEdge iEdge : graph.incomingEdgesOf(vertex))
        {
            INode edgeSource = graph.getEdgeSource(iEdge);
            int i = depthMap.get(edgeSource);
            if (!seen || i > best)
            {
                seen = true;
                best = i;
            }
        }
        depthMap.put(
          vertex,
          (seen ? best : -1)
          +1
        );

        return vertex;
    }

    private INode innerProvideNextVertex() {
        return completeQueue.poll();
    }

    @Override
    protected void encounterVertexAgain(final INode vertex, final IEdge edge)
    {
        if (depthMap.containsKey(vertex))
            throw new IllegalStateException("Circle detected. Graph needs ot be circle free");

        if (incompleteQueue.contains(vertex) && isComplete(vertex)) {
            incompleteQueue.remove(vertex);
            completeQueue.offer(vertex);
        }
    }

    public Map<INode, Integer> getDepthMap()
    {
        return depthMap;
    }

    @Override
    public Map<INode, Integer> calculateDepthMap() {
        while(hasNext())
        {
            next();
        }

        return getDepthMap();
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
