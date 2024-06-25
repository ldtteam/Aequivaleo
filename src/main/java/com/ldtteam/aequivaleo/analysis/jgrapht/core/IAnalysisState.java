package com.ldtteam.aequivaleo.analysis.jgrapht.core;

import com.ldtteam.aequivaleo.analysis.StatCollector;

import java.util.function.Supplier;

/**
 * Represents the state of the analysis.
 */
public interface IAnalysisState {

    /**
     * @return The {@link StatCollector} for this analysis.
     */
    StatCollector statCollector();

    /**
     * @return Whether this is a simulation.
     */
    boolean isSimulation();

    /**
     * @return This state as a simulation.
     */
    IAnalysisState simulate();

    /**
     * Invoking this method will either run the simulation or the analysis code, depending on the state of this object.
     */
    void doWhen(Runnable forSimulation, Runnable forAnalysis);

    /**
     * Invoking this method will either run the simulation or the analysis code, depending on the state of this object.
     */
    <T> T doWhen(Supplier<T> forSimulation, Supplier<T> forAnalysis);
}
