package com.ldtteam.aequivaleo.analyzer.jgrapht.core;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;

import java.util.Optional;
import java.util.Set;

public interface IAnalysisNodeWithContainer<N, S extends IAnalysisGraphNode<N, S, E>, E extends IAnalysisEdge> extends IAnalysisGraphNode<N, S, E>
{

    Optional<ICompoundContainer<?>> getWrapper();

    default Set<ICompoundContainer<?>> getTargetedWrapper(final S sourceNeighbor) {return getWrapper().map(Sets::<ICompoundContainer<?>>newHashSet).orElse(Sets.newHashSet());}

    default Set<ICompoundContainer<?>> getSourcedWrapper(final S targetNeighbor) {return getWrapper().map(Sets::<ICompoundContainer<?>>newHashSet).orElse(Sets.newHashSet());}
}
