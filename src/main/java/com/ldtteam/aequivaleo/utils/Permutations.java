package com.ldtteam.aequivaleo.utils;

import java.util.ArrayList;
import java.util.List;

public class Permutations<T> {

    public List<List<T>> getPermutations(List<T> elements, int n) {
        List<List<T>> permutations = new ArrayList<>();
        generatePermutations(elements, n, new ArrayList<>(), permutations);
        return permutations;
    }

    private void generatePermutations(List<T> elements, int n, List<T> current, List<List<T>> permutations) {
        if (current.size() == n) {
            permutations.add(new ArrayList<>(current));
        } else {
            for (T element : elements) {
                if (!current.contains(element)) {
                    current.add(element);
                    generatePermutations(elements, n, current, permutations);
                    current.remove(current.size() - 1);
                }
            }
        }
    }
}
