package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.trace;

import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search.ISearchAction;
import org.jgrapht.Graph;

import java.util.Collection;

public interface ICycleReducingTracer<G extends Graph<V, E>, V, E> {

    static <T extends Graph<N, S>, N, S> ICycleReducingTracer<T, N, S> noop() {
        return new ICycleReducingTracer<T, N, S>() {
            @Override
            public void onEncounterVertex(N vertex) {

            }

            @Override
            public void onExitVertex(N vertex) {

            }

            @Override
            public void onCycleFound(Collection<N> cycle) {

            }

            @Override
            public void onActionAdded(ISearchAction<T, N, S> action) {

            }

            @Override
            public void onActionRemoved(ISearchAction<T, N, S> action) {

            }
        };
    }

    void onEncounterVertex(V vertex);

    void onExitVertex(V vertex);

    void onCycleFound(Collection<V> cycle);

    void onActionAdded(ISearchAction<G, V, E> action);

    void onActionRemoved(ISearchAction<G, V, E> action);
}
