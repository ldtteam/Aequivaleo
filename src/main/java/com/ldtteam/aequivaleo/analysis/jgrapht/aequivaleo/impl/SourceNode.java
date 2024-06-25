package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.impl;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.base.Node;

public final class SourceNode extends Node
{
    @Override
    public NodeType type() {
        return NodeType.SOURCE;
    }

    @Override
    protected int calculateHashCode() {
        return 0;
    }

    @Override
    protected String calculateStringRepresentation() {
        return "SourceNode{}";
    }
}
