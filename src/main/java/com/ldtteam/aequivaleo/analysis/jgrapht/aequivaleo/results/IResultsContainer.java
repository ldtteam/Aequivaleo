package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results;

/**
 * A container for results of a simulation.
 * This container can be used to push and pop simulation states, and to offer and poll results.
 * This allows for the simulation to be run in a stateful manner.
 * <p>
 * Each node in the graph potentially has its own results' container.
 * Nodes that contain other nodes generally won't have their own results' container.
 */
public interface IResultsContainer extends ISimulationManager {

    /**
     * Offer a set of results.
     * @param offer the results to offer.
     */
    void offer(CompoundInstanceSet offer);

    /**
     * Sets the base values for the simulation.
     * These compound instances will always be included in the results.
     * <p>
     *     If no candidates are available then base is not included, but as soon as at least one candidate is available, base is included.
     * </p>
     * <p>
     *     Note that different simulation states can have different base values.
     * </p>
     * @param base the base values. 
     */
    void base(CompoundInstanceSet base);

    /**
     * Sets the results. 
     */
    void force(CompoundInstanceSet results);

    /**
     * Simulate the results.
     * <p>
     *     This will simulate the current simulation state and return the results.
     *     If the results have already been determined, the previously determined results will be returned for the given simulation state.
     * </p>
     *
     * @return the results.
     */
    CompoundInstanceSet simulate();

    /**
     * Determines the results if not previously determined.
     * <p>
     *     If the results have already been determined, the previously determined results will be returned.
     * </p>
     * <p>
     *     Polling the results will automatically complete the simulation and prevent pushing new simulation states.
     * </p>
     *
     * @return the results.
     */
    CompoundInstanceSet results();

    /**
     * Check if the results have already been determined.
     * @return true if the results have already been determined.
     */
    boolean hasResults();
}
