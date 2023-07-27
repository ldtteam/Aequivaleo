package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.analysis.jgrapht.core.IAnalysisNodeWithContainer;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;

import java.util.Set;

/**
 * Represents a node that represents a container (like Item, ItemStack, etc) in the recipe graph.
 */
public interface IContainerNode extends INode, IAnalysisNodeWithContainer<IGraph, Set<CompoundInstance>, INode, IEdge>
{
}
