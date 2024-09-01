package com.ldtteam.aequivaleo.analysis.jgrapht.graph;

public class DuplicateEdgeException extends IllegalArgumentException {
    public DuplicateEdgeException(Object source, Object target) {
        super("Edge already exists: source=" + source + ", target=" + target);
    }
}
