package com.ldtteam.aequivaleo.api.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.antlr.v4.runtime.misc.MultiMap;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GroupingUtils
{

    private GroupingUtils()
    {
        throw new IllegalStateException("Tried to initialize: GroupingUtils but this is a Utility class.");
    }


    public static <T, O> Collection<Collection<T>> groupBy(final Iterable<T> source, Function<T, O> extractor) {
        final Multimap<O, T> groups = HashMultimap.create();

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
}
