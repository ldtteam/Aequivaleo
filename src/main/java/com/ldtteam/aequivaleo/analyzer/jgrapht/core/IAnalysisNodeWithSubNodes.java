package com.ldtteam.aequivaleo.analyzer.jgrapht.core;

import java.util.Set;

public interface IAnalysisNodeWithSubNodes<N, S extends IAnalysisGraphNode<N, S, E>, E extends IAnalysisEdge> extends IAnalysisGraphNode<N, S, E>
{

    Set<S> getInnerNodes();

    Set<S> getSourceNeighborOf(final S neighbor);

    Set<S> getTargetNeighborOf(final S neighbor);
}
