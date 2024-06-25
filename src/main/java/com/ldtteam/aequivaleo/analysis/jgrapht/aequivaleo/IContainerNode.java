package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;

import java.util.Optional;

/**
 * Represents a node that represents a container (like Item, ItemStack, etc) in the recipe graph.
 */
public interface IContainerNode extends IResultsOwningNode
{
    ICompoundContainer<?> contents();
}
