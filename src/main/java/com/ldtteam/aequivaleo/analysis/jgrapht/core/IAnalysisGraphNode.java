package com.ldtteam.aequivaleo.analysis.jgrapht.core;

import com.ldtteam.aequivaleo.analysis.StatCollector;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import org.jgrapht.Graph;

//Marker interface indicating that this is a possible component of a graph.
public interface IAnalysisGraphNode<G extends Graph<S, E>, N, S extends IAnalysisGraphNode<G, N, S, E>, E extends IAnalysisEdge>
{
    /**
     * Analyze the node.
     *
     * @param state The analysis state, which can be used to retrieve information about the analysis.
     */
    void analyze(final IAnalysisState state);

    /**
     * @return The type of the node.
     */
    NodeType type();

    /**
     * The type of the node.
     */
    enum NodeType
    {
        SOURCE,
        CLIQUE,
        CYCLE,
        CONTAINER,
        INGREDIENT,
        RECIPE;

        public void collectStats(StatCollector statCollector, INode node) {
            switch (this) {
                case SOURCE -> {
                    statCollector.onVisitSourceNode(node);
                }
                case CLIQUE -> {
                    statCollector.onVisitCliqueNode(node);
                }
                case CYCLE -> {
                    statCollector.onVisitCycleNode(node);
                }
                case CONTAINER -> {
                    statCollector.onVisitContainerNode(node);
                }
                case INGREDIENT -> {
                    statCollector.onVisitIngredientNode(node);
                }
                case RECIPE -> {
                    statCollector.onVisitRecipeNode(node);
                }
            }
        }
    }
}
