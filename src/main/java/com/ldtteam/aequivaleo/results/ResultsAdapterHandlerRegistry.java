package com.ldtteam.aequivaleo.results;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.ldtteam.aequivaleo.api.results.IResultsAdapterHandlerRegistry;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.function.Predicate;

public class ResultsAdapterHandlerRegistry implements IResultsAdapterHandlerRegistry
{
    private static final ResultsAdapterHandlerRegistry INSTANCE = new ResultsAdapterHandlerRegistry();

    public static ResultsAdapterHandlerRegistry getInstance()
    {
        return INSTANCE;
    }

    private final Queue<Entry<?>> alternativeHandlers = new ConcurrentLinkedQueue<Entry<?>>();

    private ResultsAdapterHandlerRegistry()
    {
    }

    @Override
    public <T> IResultsAdapterHandlerRegistry registerHandler(final Predicate<Object> canHandlePredicate, final Function<T, Set<?>> alternativesProducer)
    {
        this.alternativeHandlers.add(new Entry<>(canHandlePredicate, alternativesProducer));
        return this;
    }

    public Set<?> produceAlternatives(final Object target) {
        for (final Entry<?> alternativeHandler : alternativeHandlers)
        {
            final Optional<Set<?>> result = handleEntry(target, alternativeHandler);
            if (result.isPresent())
            {
                return result.get();
            }
        }

        return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<Set<?>> handleEntry(final Object target, final Entry<T> entry) {
        if (entry.getCanHandleCallback().test(target)) {
            return Optional.ofNullable(entry.getAlternativesProducer().apply((T) target));
        }

        return Optional.empty();
    }

    private static final class Entry<T> {
        final Predicate<Object> canHandleCallback;
        final Function<T, Set<?>> alternativesProducer;

        private Entry(final Predicate<Object> canHandleCallback, final Function<T, Set<?>> alternativesProducer) {
            this.canHandleCallback = canHandleCallback;
            this.alternativesProducer = alternativesProducer;
        }

        public Predicate<Object> getCanHandleCallback()
        {
            return canHandleCallback;
        }

        public Function<T, Set<?>> getAlternativesProducer()
        {
            return alternativesProducer;
        }
    }
}
