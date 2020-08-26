package com.ldtteam.aequivaleo.analyzer.jgrapht;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;

public class CompoundInstanceCalculationInformation
{

    private final IAnalysisGraphNode source;
    private final CompoundInstance   instance;

    public CompoundInstanceCalculationInformation(final IAnalysisGraphNode source, final CompoundInstance instance) {
        this.source = source;
        this.instance = instance;
    }
}
