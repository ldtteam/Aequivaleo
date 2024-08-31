package com.ldtteam.aequivaleo.api.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.antlr.v4.runtime.misc.MultiMap;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    /**
     * Groups the source collection by the extractor function and returns the groups as a stream of streams.
     *
     * @param items The source collection.
     * @param extractor The extractor function.
     * @param <T> The type of the source collection.
     * @param <O> The type of the extracted value.
     * @return The grouped stream.
     */
    public static <T, O> Stream<Stream<T>> groupByDynamically(final Stream<T> items, Function<T, O> extractor) {
        record GroupedStreams<T, O>(Map<O, Stream.Builder<T>> streams) {}

        return items.collect(new Collector<T, GroupedStreams<T, O>, Stream<Stream<T>>>() {
            @Override
            public Supplier<GroupedStreams<T, O>> supplier() {
                return () -> new GroupedStreams<T, O>(new HashMap<>());
            }

            @Override
            public BiConsumer<GroupedStreams<T, O>, T> accumulator() {
                return (toGroupedStreams, t) -> {
                    final O key = extractor.apply(t);
                    toGroupedStreams.streams.computeIfAbsent(key, k -> Stream.<T>builder()).accept(t);
                };
            }

            @Override
            public BinaryOperator<GroupedStreams<T, O>> combiner() {
                return (left, right) -> {
                    right.streams.forEach((key, value) -> {
                        final Stream.Builder<T> builder = left.streams.computeIfAbsent(key, k -> Stream.<T>builder());
                        value.build().forEach(builder);
                    });

                    return left;
                };
            }

            @Override
            public Function<GroupedStreams<T, O>, Stream<Stream<T>>> finisher() {
                return toGroupedStreams -> toGroupedStreams.streams.values().stream().map(Stream.Builder::build);
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        });
    }
}
