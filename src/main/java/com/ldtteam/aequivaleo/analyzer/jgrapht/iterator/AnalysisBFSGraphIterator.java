package com.ldtteam.aequivaleo.analyzer.jgrapht.iterator;

import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.node.IAnalysisGraphNode;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.traverse.CrossComponentIterator;

import java.util.*;
import java.util.stream.Collectors;

public class AnalysisBFSGraphIterator<N> extends CrossComponentIterator<IAnalysisGraphNode<N>, AccessibleWeightEdge, AnalysisBFSGraphIterator.SearchNodeData>
{

    private final Queue<IAnalysisGraphNode<N>> completeQueue = new ArrayDeque<>();
    private final LinkedList<IAnalysisGraphNode<N>> incompleteQueue = new LinkedList<>();

    public AnalysisBFSGraphIterator(final Graph<IAnalysisGraphNode<N>, AccessibleWeightEdge> g, final Set<? extends IAnalysisGraphNode<N>> startVertices)
    {
        super(g, new HashSet<>(startVertices));
    }

    @Override
    protected boolean isConnectedComponentExhausted()
    {
        return completeQueue.isEmpty() && incompleteQueue.isEmpty();
    }

    @Override
    protected void encounterVertex(final IAnalysisGraphNode<N> vertex, final AccessibleWeightEdge edge)
    {
        int depth = (edge == null ? 0
                       : getSeenData(Graphs.getOppositeVertex(graph, edge, vertex)).depth + 1);
        putSeenData(vertex, new AnalysisBFSGraphIterator.SearchNodeData(edge, depth));

        vertex.determineResult();
        vertex.onReached(getGraph());

        if (vertex.isComplete(getGraph()))
            completeQueue.offer(vertex);
        else
            incompleteQueue.offer(vertex);
    }

    @Override
    protected IAnalysisGraphNode<N> provideNextVertex()
    {
        if (!completeQueue.isEmpty())
            return completeQueue.poll();

        return incompleteQueue.removeFirst();
    }

    @Override
    protected void encounterVertexAgain(final IAnalysisGraphNode<N> vertex, final AccessibleWeightEdge edge)
    {
        //For now lets noop.
        //Need to do some thinking.
    }

    static class SearchNodeData
    {
        /**
         * Edge to parent
         */
        final AccessibleWeightEdge edge;
        /**
         * Depth of node in search tree
         */
        final int depth;

        SearchNodeData(AccessibleWeightEdge edge, int depth)
        {
            this.edge = edge;
            this.depth = depth;
        }
    }
}
