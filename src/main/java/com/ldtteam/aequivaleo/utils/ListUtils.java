package com.ldtteam.aequivaleo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListUtils {

    public static boolean containsAny(final List<?> list, final Set<?> targets) {
        for (final Object target : targets) {
            if (list.contains(target)) {
                return true;
            }
        }
        return false;
    }


    public static <T> void replaceAllInWidthWithoutRepetitions(final List<T> list, final Set<T> targets, final T replacement) {
        int lastReplacement = -2;
        List<Integer> toRemove = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            if (targets.contains(list.get(i))) {
                if (i - 1 > lastReplacement) {
                    list.set(i, replacement);
                } else {
                    toRemove.add(i);
                }
                lastReplacement = i;
            }
        }
        for (int i = toRemove.size() - 1; i >= 0; i--) {
            list.remove((int) toRemove.get(i));
        }
    }
}
