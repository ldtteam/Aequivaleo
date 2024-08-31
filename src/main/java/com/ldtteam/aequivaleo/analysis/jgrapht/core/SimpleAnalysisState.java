package com.ldtteam.aequivaleo.analysis.jgrapht.core;

import com.ldtteam.aequivaleo.analysis.StatCollector;

import java.util.function.Supplier;

public record SimpleAnalysisState(StatCollector statCollector, boolean isSimulation) implements IAnalysisState {

    @Override
    public IAnalysisState simulate() {
        return new SimpleAnalysisState(statCollector, true);
    }

    @Override
    public void doWhen(Runnable forSimulation, Runnable forAnalysis) {
        if (isSimulation()) {
            forSimulation.run();
        } else {
            forAnalysis.run();
        }
    }

    @Override
    public <T> T doWhen(Supplier<T> forSimulation, Supplier<T> forAnalysis) {
        if (isSimulation()) {
            return forSimulation.get();
        } else {
            return forAnalysis.get();
        }
    }
}
