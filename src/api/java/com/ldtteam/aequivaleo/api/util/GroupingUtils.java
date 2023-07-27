package com.ldtteam.aequivaleo.api.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.antlr.v4.runtime.misc.MultiMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GroupingUtils
{

    private GroupingUtils()
    {
        throw new IllegalStateException("Tried to initialize: GroupingUtils but this is a Utility class.");
    }


    public static <T, O> Collection<Collection<T>> groupByUsingSet(final Iterable<T> source, Function<T, O> extractor) {
        return groupBy(HashMultimap.create(), source, extractor);
    }

    public static <T, O> Collection<Collection<T>> groupByUsingList(final Iterable<T> source, Function<T, O> extractor) {
        return groupBy(ArrayListMultimap.create(), source, extractor);
    }

    private static <T, O> Collection<Collection<T>> groupBy(final Multimap<O, T> groups, final Iterable<T> source, Function<T, O> extractor) {
        source.forEach(
          e -> {
              groups.put(extractor.apply(e), e);
          }
        );

        return groups
                 .keySet()
                 .stream()
                 .map(groups::get)
                 .collect(Collectors.toList());
    }

    public static <T, O> Map<O, Collection<T>> groupByUsingSetToMap(final Iterable<T> source, Function<T, O> extractor) {
        return groupByToMap(HashMultimap.create(), source, extractor);
    }

    public static <T, O> Map<O, Collection<T>> groupByUsingListToMap(final Iterable<T> source, Function<T, O> extractor) {
        return groupByToMap(ArrayListMultimap.create(), source, extractor);
    }

    private static <T, O> Map<O, Collection<T>> groupByToMap(final Multimap<O, T> groups, final Iterable<T> source, Function<T, O> extractor) {
        source.forEach(
          e -> {
              groups.put(extractor.apply(e), e);
          }
        );

        return groups.asMap();
    }
}
