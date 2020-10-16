package com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisEdge;

/**
 * Represents an edge in the recipe graph.
 * Used to represent a relation between objects.
 * Like an ingredient being used in a recipe.
 * Or a subgraph connection.
 */
public interface IEdge extends IAnalysisEdge
{

    /**
     * Gives access to the identifier of the edge.
     *
     * @return The edge identifier.
     */
    long getEdgeIdentifier();
}
