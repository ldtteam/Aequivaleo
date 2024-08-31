package com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct;

import java.util.*;

public final class Path<V> {

    private final Set<V> contents = new HashSet<>();
    private final LinkedList<V> path = new LinkedList<>();

    public void addFirst(V vertex) {
        path.addFirst(vertex);
        contents.add(vertex);
    }

    public boolean contains(V edgeTarget) {
        return contents.contains(edgeTarget);
    }

    public int indexOfOr(V value, int i) {
        if (!contains(value))
            return i;

        return path.size() - path.indexOf(value);
    }

    public List<V> getSubPathUntil(V target) {
        if (!contains(target))
            throw new IllegalStateException("Path does not contain target vertex");

        List<V> subPath = new ArrayList<>();

        for (V vertex : path) {
            subPath.add(vertex);
            if (vertex.equals(target))
                break;
        }

        Collections.reverse(subPath);

        return subPath;
    }

    public void clean(V vertex) {
        if (!Objects.equals(path.peekFirst(), vertex))
            throw new IllegalStateException("Path does not start with vertex");

        path.removeFirst();
        contents.remove(vertex);
    }

    public V peekFirst() {
        return path.peekFirst();
    }
}
