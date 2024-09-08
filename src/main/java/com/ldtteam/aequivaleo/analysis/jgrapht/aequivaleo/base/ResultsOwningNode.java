package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.base;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.ICoreNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IResultsOwningNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.CompoundInstanceSet;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.IResultsContainer;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.SimulateableResultsContainer;
import com.ldtteam.aequivaleo.analysis.jgrapht.core.IAnalysisState;

import java.util.Map;

public abstract class ResultsOwningNode extends CoreNode implements IResultsOwningNode {

    private final IResultsContainer results = new SimulateableResultsContainer();

    @Override
    public IResultsContainer results() {
        return results;
    }

    @Override
    public void analyze(IAnalysisState state) {
        super.analyze(state);

        if (results().hasResults()) {
            propagateResults(results().results());
            return;
        }

        final CompoundInstanceSet compounds = state.doWhen(
                () -> results().simulate(),
                () -> results().results()
        );

        propagateResults(compounds);
    }

    private void propagateResults(CompoundInstanceSet compounds) {
        for (Map.Entry<ICoreNode, Double> entry : outputs.object2DoubleEntrySet()) {
            ICoreNode node = entry.getKey();
            Double weight = entry.getValue();
            if (node instanceof IResultsOwningNode resultsNode) {
                resultsNode.results().offer(compounds.scaled(weight));
            }
        }
    }
}
