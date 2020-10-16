package com.ldtteam.aequivaleo.analyzer.jgrapht.core;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import org.jgrapht.Graph;

import java.util.Optional;
import java.util.Set;

public interface IAnalysisNodeWithContainer<G extends Graph<S, E>, N, S extends IAnalysisGraphNode<G, N, S, E>, E extends IAnalysisEdge> extends IAnalysisGraphNode<G, N, S, E>
{

    Optional<ICompoundContainer<?>> getWrapper();

    default Set<ICompoundContainer<?>> getTargetedWrapper(final S sourceNeighbor) {return getWrapper().map(Sets::<ICompoundContainer<?>>newHashSet).orElse(Sets.newHashSet());}

    default Set<ICompoundContainer<?>> getSourcedWrapper(final S targetNeighbor) {return getWrapper().map(Sets::<ICompoundContainer<?>>newHashSet).orElse(Sets.newHashSet());}
}
