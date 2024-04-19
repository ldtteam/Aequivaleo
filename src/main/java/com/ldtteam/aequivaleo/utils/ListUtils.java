package com.ldtteam.aequivaleo.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ListUtils {

    public record ReplacementResult<T>(boolean sizeChanged, Collection<List<T>> newCycles) {}

    public static <T> ReplacementResult<T> rebuildCycleList(final List<T> list, final Set<T> cycle, final T replacement) {
        boolean sizeChanged = false;
        List<T> newList = new ArrayList<>();
        for (T element : list) {
            if (cycle.contains(element)) {
                newList.add(replacement);
            } else {
                newList.add(element);
            }
        }

        // Remove sequential duplicates
        for (int i = 0; i < newList.size() - 1; i++) {
            if (newList.get(i).equals(newList.get(i + 1))) {
                newList.remove(i + 1);
                i--;
                sizeChanged = true;
            }
        }

        // Remove tail if first and last elements are the same
        if (newList.size() >= 2 && newList.get(0).equals(newList.get(newList.size() - 1))) {
            newList.remove(newList.size() - 1);
            sizeChanged = true;
        }

        // Split the list if the replacement object exists more than once

        //BAC -> BAC
        //BACA -> BA + CA
        //ACAB -> AC + AB
        //BDC -> BDC

        rotateListUntilIsFront(newList, replacement);

        List<List<T>> newCycles = new ArrayList<>();
        List<T> workingList = new ArrayList<>();
        while(!newList.isEmpty()) {
            T currentHead = newList.remove(0);
            if (!currentHead.equals(replacement) || workingList.isEmpty()) {
                workingList.add(currentHead);
                if (currentHead.equals(replacement)) {
                    newCycles.add(workingList);
                }
                continue;
            }

            if (!newCycles.contains(workingList)) {
                newCycles.add(workingList);
            }
            workingList = new ArrayList<>();
            workingList.add(replacement);
        }

        if (!workingList.isEmpty() && !newCycles.contains(workingList)) {
            newCycles.add(workingList);
        }

        return new ReplacementResult<>(sizeChanged, newCycles);
    }

    private static <T> void rotateListUntilIsFront(final List<T> list, final T element) {
        if (!list.contains(element)) {
            return;
        }

        while (!list.get(0).equals(element)) {
            list.add(list.remove(0));
        }
    }
}
