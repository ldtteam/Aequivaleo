package com.ldtteam.aequivaleo.analysis.jgrapht.graph;

public class DuplicateEdgeException extends IllegalArgumentException {
    public DuplicateEdgeException() {
        super("Edge already exists");
    }
}
