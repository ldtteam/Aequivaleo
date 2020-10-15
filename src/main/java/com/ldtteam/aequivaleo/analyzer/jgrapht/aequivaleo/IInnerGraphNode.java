package com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisNodeWithSubNodes;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;

import java.util.Set;

/**
 * Represents a graph node which has other nodes contained in itself.
 */
public interface IInnerGraphNode extends INode, IAnalysisNodeWithSubNodes<Set<CompoundInstance>, INode, IEdge>
{
}
