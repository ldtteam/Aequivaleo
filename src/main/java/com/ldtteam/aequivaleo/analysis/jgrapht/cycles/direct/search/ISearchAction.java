package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search;

import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.Context;
import org.jgrapht.Graph;

import java.util.Set;

public interface ISearchAction<G extends Graph<V, E>, V, E> {
    void perform(Context<G, V, E> context);

    boolean actionTouchesAnyOf(Set<V> vertices);
}
