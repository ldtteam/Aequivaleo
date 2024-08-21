package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results;

public interface ISimulationManager {
    /**
     * Push a new simulation state.
     *
     */
    void push();

    /**
     * Pop the current simulation state.
     */
    void pop();

    /**
     * Complete the simulation.
     * This will simulate the current simulate and store the current results as final.
     *
     */
    void complete();

    /**
     * Simulates the current simulation state stores its results in the simulation state after a pop.
     *
     */
    void commit();
}
