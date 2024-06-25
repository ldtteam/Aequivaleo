package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results;

public interface ISimulationManager {
    /**
     * Push a new simulation state.
     *
     * @throws ResultsAlreadyPolledException if results have already been polled and as such no new simulation state can be pushed.
     */
    void push() throws ResultsAlreadyPolledException;

    /**
     * Pop the current simulation state.
     */
    void pop() throws ResultsAlreadyPolledException;

    /**
     * Complete the simulation.
     * This will simulate the current simulate and store the current results as final.
     *
     * @throws ResultsAlreadyPolledException if results have already been polled and as such no new simulation state can be pushed.
     */
    void complete() throws ResultsAlreadyPolledException;

    /**
     * Simulates the current simulation state stores its results in the simulation state after a pop.
     *
     * @throws ResultsAlreadyPolledException if results have already been polled and as such no new simulation state can be pushed.
     */
    void commit() throws ResultsAlreadyPolledException;
}
