package com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisGraphNode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;

import java.util.Set;

/**
 * The base node spec for aequivaleo analysis.
 */
public interface INode extends IAnalysisGraphNode<IGraph, Set<CompoundInstance>, INode, IEdge>
{
    @Override
    default INode getSelf() {
        return this;
    }
}
