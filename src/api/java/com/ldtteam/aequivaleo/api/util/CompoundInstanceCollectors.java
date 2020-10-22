package com.ldtteam.aequivaleo.api.util;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CompoundInstanceCollectors
{

    private CompoundInstanceCollectors() {
        throw new IllegalStateException("Tried to instantiate a utility class");
    }

    public static
    Collector<CompoundInstance, ?, Set<CompoundInstance>> reduceToSet() {
        return Collectors.collectingAndThen(Collectors.toList(), compoundInstances -> GroupingUtils.groupByUsingList(compoundInstances, CompoundInstance::getType)
          .stream()
          .filter(sameType -> !sameType.isEmpty())
          .map(sameType -> new CompoundInstance(sameType.iterator().next().getType(), sameType.stream().mapToDouble(CompoundInstance::getAmount).sum()))
          .collect(Collectors.toSet()));
    }
}
