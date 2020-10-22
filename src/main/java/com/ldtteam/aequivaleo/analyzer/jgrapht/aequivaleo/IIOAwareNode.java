package com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo;

import org.jgrapht.Graph;

/**
 * Represents a node in the recipe graph that is aware of its
 * surroundings and can produce a subgraph to use for IO related
 * calculation like edge weight detection etc, used in recipes to determine
 * the multiplier of the compound instance value.
 */
public interface IIOAwareNode extends INode
{
    /**
     * Gets the IO Graph from the given complete graph for this node.
     *
     * @param graph The complete graph in question.
     * @return The IO graph.
     */
    IGraph getIOGraph(final IGraph graph);
}
