package com.ldtteam.aequivaleo.utils;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;

import java.util.*;
import java.util.stream.Collectors;

public class CompoundInstanceUtils {

    private CompoundInstanceUtils() {
        throw new IllegalStateException("Can not instantiate an instance of: CompoundInstanceUtils. This is a utility class");
    }

    @SafeVarargs
    public static Set<CompoundInstance> merge(final Set<CompoundInstance> left, final Set<CompoundInstance>... right) {
        if (right.length == 0)
            return new HashSet<>(left);

        final Map<ICompoundType, Double> sumCount = new HashMap<>();
        for (CompoundInstance i : left) {
            if (sumCount.containsKey(i.getType())) {
                sumCount.put(i.getType(), sumCount.get(i.getType()) + i.getAmount());
            } else {
                sumCount.put(i.getType(), i.getAmount());
            }
        }

        for (Set<CompoundInstance> mergeSource : right) {
            for (CompoundInstance i : mergeSource) {
                if (sumCount.containsKey(i.getType())) {
                    sumCount.put(i.getType(), sumCount.get(i.getType()) + i.getAmount());
                } else {
                    sumCount.put(i.getType(), i.getAmount());
                }
            }
        }

        return sumCount.entrySet().stream()
                .map(e -> new CompoundInstance(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
    }
}
