package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.ISimulationManager;

/**
 * Represents a node that can be simulated.
 */
public interface ISimulateableNode extends INode {

    /**
     * @return The simulation manager for this node.
     */
    ISimulationManager simulationManager();
}
