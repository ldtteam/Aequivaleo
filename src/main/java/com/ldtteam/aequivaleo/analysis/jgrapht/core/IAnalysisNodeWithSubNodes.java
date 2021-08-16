package com.ldtteam.aequivaleo.analysis.jgrapht.core;

import org.jgrapht.Graph;

import java.util.Set;

public interface IAnalysisNodeWithSubNodes<G extends Graph<S, E>, N, S extends IAnalysisGraphNode<G, N, S, E>, E extends IAnalysisEdge> extends IAnalysisGraphNode<G, N, S, E>
{

    Set<S> getInnerNodes();

    Set<S> getSourceNeighborOf(final S neighbor);

    Set<S> getTargetNeighborOf(final S neighbor);
}
