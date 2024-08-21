package com.ldtteam.aequivaleo.analysis.jgrapht.clique;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class NodeMergerTest {

    @Test
    public void multipleSetsWithOverlapGetMerged() {
        final Set<Set<String>> input = Set.of(Set.of("a", "b"), Set.of("b", "c"), Set.of("c", "d"));
        final Set<Set<String>> result = JGraphTCliqueReducer.NodeMerger.mergeSets(input);

        assertEquals(Set.of(Set.of("a", "b", "c", "d")), result);
    }

    @Test
    public void multipleDistinctSetsGetNotMerged() {
        final Set<Set<String>> input = Set.of(Set.of("a", "b"), Set.of("c", "d"));
        final Set<Set<String>> result = JGraphTCliqueReducer.NodeMerger.mergeSets(input);

        assertEquals(input, result);
    }

    @Test
    public void multipleSetsWithPartialOverlapGetPartiallyMerged() {
        final Set<Set<String>> input = Set.of(Set.of("a", "b"), Set.of("b", "c"), Set.of("c", "d"), Set.of("e", "f"));
        final Set<Set<String>> result = JGraphTCliqueReducer.NodeMerger.mergeSets(input);

        assertEquals(Set.of(Set.of("a", "b", "c", "d"), Set.of("e", "f")), result);
    }
}