package com.ldtteam.aequivaleo.analyzer.jgrapht.edge;

import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

public class Edge extends DefaultWeightedEdge implements IEdge
{

    private final long id;

    public Edge(final long id)
    {
        this.id = id;
    }

    @Override
    public double getWeight()
    {
        return super.getWeight();
    }

    @Override
    public long getEdgeIdentifier()
    {
        return id;
    }
}
