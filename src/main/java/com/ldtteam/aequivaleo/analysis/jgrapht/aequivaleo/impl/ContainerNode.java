package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.impl;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IContainerNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.base.ResultsOwningNode;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;

import java.util.Objects;

public final class ContainerNode extends ResultsOwningNode implements IContainerNode {

    private final ICompoundContainer<?> contents;

    public ContainerNode(ICompoundContainer<?> contents) {
        this.contents = contents;
    }

    @Override
    public ICompoundContainer<?> contents() {
        return contents;
    }

    @Override
    public NodeType type() {
        return NodeType.CONTAINER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContainerNode that = (ContainerNode) o;
        return Objects.equals(contents, that.contents);
    }

    @Override
    protected int calculateHashCode() {
        return Objects.hash(contents);
    }

    @Override
    protected String calculateStringRepresentation() {
        return "ContainerNode{" +
                "contents=" + contents +
                '}';
    }
}
