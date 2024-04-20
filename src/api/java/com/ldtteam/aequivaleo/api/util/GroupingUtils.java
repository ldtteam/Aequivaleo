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

/**
 * Utility class for grouping collections.
 */
public final class GroupingUtils
{

    private GroupingUtils()
    {
        throw new IllegalStateException("Tried to initialize: GroupingUtils but this is a Utility class.");
    }

    /**
     * Groups the source collection by the extractor function and returns the groups as a collection of collections.
     * The inner collection uses a {@link Set} as the backing collection.
     *
     * @param source The source collection.
     * @param extractor The extractor function.
     * @param <T> The type of the source collection.
     * @param <O> The type of the extracted value.
     * @return The grouped collection.
     */
    public static <T, O> Collection<Collection<T>> groupByUsingSet(final Iterable<T> source, Function<T, O> extractor) {
        return groupBy(HashMultimap.create(), source, extractor);
    }

    /**
     * Groups the source collection by the extractor function and returns the groups as a collection of collections.
     * The inner collection uses a {@link List} as the backing collection.
     *
     * @param source The source collection.
     * @param extractor The extractor function.
     * @param <T> The type of the source collection.
     * @param <O> The type of the extracted value.
     * @return The grouped collection.
     */
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

    /**
     * Groups the source collection by the extractor function and returns the groups as a map.
     * The inner collection uses a {@link Set} as the backing collection.
     *
     * @param source The source collection.
     * @param extractor The extractor function.
     * @param <T> The type of the source collection.
     * @param <O> The type of the extracted value.
     * @return The grouped map.
     */
    public static <T, O> Map<O, Collection<T>> groupByUsingSetToMap(final Iterable<T> source, Function<T, O> extractor) {
        return groupByToMap(HashMultimap.create(), source, extractor);
    }

    /**
     * Groups the source collection by the extractor function and returns the groups as a map.
     * The inner collection uses a {@link List} as the backing collection.
     *
     * @param source The source collection.
     * @param extractor The extractor function.
     * @param <T> The type of the source collection.
     * @param <O> The type of the extracted value.
     * @return The grouped map.
     */
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
