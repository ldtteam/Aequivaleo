package com.ldtteam.aequivaleo.analyzer.jgrapht.iterator;

import com.ldtteam.aequivaleo.analyzer.jgrapht.node.IAnalysisGraphNode;
import org.jgrapht.Graph;
import org.jgrapht.traverse.CrossComponentIterator;

public class AnalysisBFSGraphIterator<V extends IAnalysisGraphNode<N>, E, N> extends CrossComponentIterator<V, E, AnalysisBFSGraphIterator.SearchNodeData<E>>
{

    public AnalysisBFSGraphIterator(final Graph<V, E> g)
    {
        super(g);
    }

    public AnalysisBFSGraphIterator(final Graph<V, E> g, final V startVertex)
    {
        super(g, startVertex);
    }

    public AnalysisBFSGraphIterator(final Graph<V, E> g, final Iterable<V> startVertices)
    {
        super(g, startVertices);
    }

    @Override
    protected boolean isConnectedComponentExhausted()
    {
        return false;
    }

    @Override
    protected void encounterVertex(final V vertex, final E edge)
    {

    }

    @Override
    protected V provideNextVertex()
    {
        return null;
    }

    @Override
    protected void encounterVertexAgain(final V vertex, final E edge)
    {

    }

    static class SearchNodeData<E>
    {
        /**
         * Edge to parent
         */
        final E edge;
        /**
         * Depth of node in search tree
         */
        final int depth;

        SearchNodeData(E edge, int depth)
        {
            this.edge = edge;
            this.depth = depth;
        }
    }
}
