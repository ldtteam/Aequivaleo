package com.ldtteam.aequivaleo.api.analysis;

/**
 * Indicates the state of the analysis engine.
 */
public enum AnalysisState
{
    /**
     * The state when no analysis has ran (game start / before initial world join)
     */
    UNINITIALIZED,

    /**
     * Indicates that the system is loading data for the current world.
     */
    LOADING_DATA,

    /**
     * Indicates that an analysis is running.
     *
     * This state is only available on the logical side that executes the analysis.
     * EG.: On clients which connect to a dedicated server this state will never be available.
     */
    PROCESSING,

    /**
     * Indicates that analysis has been completed, but that the data from this server side is being
     * synced to clients.
     *
     * This state is only available on the logical side that executes the analysis.
     * EG.: On clients which connect to a dedicated server this state will never be available.
     */
    SYNCING,

    /**
     * State reached by the system when syncing completes but other systems are being notified of new data.
     * An example of this state is when plugins are being notified of the results.
     *
     * This state is only available on the logical side that executes the analysis.
     * EG.: On clients which connect to a dedicated server this state will never be available.
     */
    POST_PROCESSING,

    /**
     * Indicates that the analysis successfully completed and data has been synced.
     */
    COMPLETED,

    /**
     * Indicates that the analysis completed with a failure and that no data is available.
     */
    ERRORED;

    /**
     * Checks if the current state allows for the retrieval of data from the results.
     *
     * @return {@code True} when data is available, {@code False} when not.
     */
    public boolean hasData() {
        return this == SYNCING || this == COMPLETED || this == POST_PROCESSING;
    }

    /**
     * Checks if the current state indicates that an error has occurred during analysis.
     *
     * @return {@code True} when an error occurred and either no, or broken data is available.
     */
    public boolean isErrored() {
        return this == ERRORED;
    }

    /**
     * Checks if the current state indicates that an analysis is running.
     *
     * @return {@code True} when an analysis is running, {@code False} when not.
     */
    public boolean isAnalyzing() {
        return this == PROCESSING;
    }
}
