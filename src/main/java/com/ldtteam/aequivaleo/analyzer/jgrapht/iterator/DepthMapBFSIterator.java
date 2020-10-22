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

public class DepthMapBFSIterator extends CrossComponentIterator<INode, IEdge, DepthMapBFSIterator.SearchNodeData>
{
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
        return getGraph().incomingEdgesOf(node)
          .stream()
          .map(getGraph()::getEdgeSource)
          .allMatch(depthMap::containsKey);
    }

    @Override
    protected INode provideNextVertex()
    {
        final INode vertex = innerProvideNextVertex();

        depthMap.put(
          vertex,
          graph.incomingEdgesOf(vertex)
          .stream().map(graph::getEdgeSource)
          .mapToInt(depthMap::get)
          .max()
          .orElse(-1)
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
