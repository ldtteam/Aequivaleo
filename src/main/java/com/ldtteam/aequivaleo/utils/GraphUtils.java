package com.ldtteam.aequivaleo.utils;

import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.graph.AequivaleoGraph;

public class GraphUtils
{

    private GraphUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: GraphUtils. This is a utility class");
    }

    public static IGraph mergeGraphs(final IGraph... graphs) {
        final IGraph targetGraph = new AequivaleoGraph();

        for (final IGraph graph : graphs)
        {
            for (final INode node : graph.vertexSet())
            {
                targetGraph.addVertex(node);
            }

            for (final IEdge edge : graph.edgeSet())
            {
                targetGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge), edge);
            }
        }

        return targetGraph;
    }
}
