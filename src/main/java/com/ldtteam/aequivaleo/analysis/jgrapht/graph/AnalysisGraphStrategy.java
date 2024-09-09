package com.ldtteam.aequivaleo.analysis.jgrapht.graph;

import org.jgrapht.graph.EdgeSetFactory;
import org.jgrapht.graph.FastLookupGraphSpecificsStrategy;

import java.util.LinkedHashSet;
import java.util.Set;

public class AnalysisGraphStrategy<V, E> extends FastLookupGraphSpecificsStrategy<V, E> {

    @Override
    public EdgeSetFactory<V, E> getEdgeSetFactory() {
        return new HighSpeedEdgeSetFactory<>();
    }

    public static final class HighSpeedEdgeSetFactory<V, E> implements EdgeSetFactory<V, E> {
        @Override
        public Set<E> createEdgeSet(V vertex) {
            return new LinkedHashSet<>();
        }
    }
}
