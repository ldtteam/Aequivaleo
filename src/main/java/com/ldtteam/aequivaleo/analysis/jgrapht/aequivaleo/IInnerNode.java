package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.analysis.jgrapht.core.IAnalysisNodeWithSubNodes;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;

import java.util.Set;

/**
 * Represents a graph node which has other nodes contained in itself.
 */
public interface IInnerNode extends INode, IAnalysisNodeWithSubNodes<IGraph, Set<CompoundInstance>, INode, IEdge>
{
}
