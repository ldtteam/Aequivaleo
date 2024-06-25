package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.base;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.core.IAnalysisState;

public abstract class Node implements INode {

    private Integer hashCode;
    private String representation;

    @Override
    public void analyze(IAnalysisState state) {
        type().collectStats(state.statCollector(), this);
    }

    protected abstract int calculateHashCode();

    protected abstract String calculateStringRepresentation();

    @Override
    public final int hashCode() {
        if (hashCode == null) {
            hashCode = calculateHashCode();
        }

        return hashCode;
    }

    public final String toString() {
        if (representation == null) {
            representation = calculateStringRepresentation();
        }

        return representation;
    }
}
