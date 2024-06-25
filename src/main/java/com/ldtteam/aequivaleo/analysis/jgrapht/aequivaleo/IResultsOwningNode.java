package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.IResultsContainer;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.ISimulationManager;

/**
 * Represents a node that has results associated with it.
 */
public interface IResultsOwningNode extends ICoreNode, ISimulateableNode {

    /**
     * @return The results container for this node.
     */
    IResultsContainer results();

    /**
     * @return The simulation manager for this node.
     * @implSpec Should be the {@link #results()}.
     */
    @Override
    default ISimulationManager simulationManager() {
        return results();
    }
}
