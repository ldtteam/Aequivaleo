package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.base;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IResultsOwningNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.CompoundInstanceSet;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.IResultsContainer;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.SimulateableResultsContainer;
import com.ldtteam.aequivaleo.analysis.jgrapht.core.IAnalysisState;

import java.util.function.Supplier;

public abstract class ResultsOwningNode extends CoreNode implements IResultsOwningNode {

    private final IResultsContainer results = new SimulateableResultsContainer(this);

    @Override
    public IResultsContainer results() {
        return results;
    }

    @Override
    public void analyze(IAnalysisState state) {
        super.analyze(state);

        final CompoundInstanceSet compounds = state.doWhen(
                () -> results().simulate(),
                () -> results().results()
        );

        outputs.forEach((node, weight) -> {
            if (node instanceof IResultsOwningNode resultsNode) {
                resultsNode.results().offer(compounds.scaled(weight));
            }
        });
    }
}
