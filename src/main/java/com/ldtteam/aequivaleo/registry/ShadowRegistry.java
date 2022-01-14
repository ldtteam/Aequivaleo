package com.ldtteam.aequivaleo.registry;

import com.ldtteam.aequivaleo.api.registry.IRegistryEntry;
import com.ldtteam.aequivaleo.api.registry.IRegistryView;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class ShadowRegistry<T extends IRegistryEntry, E extends IRegistryEntry> implements IRegistryView<E>
{
    private final IRegistryView<T> source;
    private final Function<T, Optional<E>> filter;

    public ShadowRegistry(final IRegistryView<T> source, final Function<T, Optional<E>> viewFilter) {
        this.source = source;
        this.filter = viewFilter;
    }

    @Override
    public Optional<E> get(final ResourceLocation name)
    {
        return source.get(name)
                 .map(filter)
                 .filter(Optional::isPresent)
                 .map(Optional::get);
    }

    @Override
    public Stream<E> stream()
    {
        return source.stream()
          .map(filter)
          .filter(Optional::isPresent)
          .map(Optional::get);
    }

    @Override
    public void forEach(final BiConsumer<ResourceLocation, E> consumer)
    {
        source.forEach((name, entry) -> {
           final Optional<E> filtered = filter.apply(entry);
            filtered.ifPresent(e -> consumer.accept(e.getRegistryName(), e));
        });
    }

    @Override
    public <F extends IRegistryEntry> IRegistryView<F> createView(Function<E, Optional<F>> viewFilter)
    {
        return new ShadowRegistry<>(
          source,
          t -> {
              final Optional<E> filtered = filter.apply(t);
              if (filtered.isPresent())
              {
                  return viewFilter.apply(filtered.get());
              }

              return Optional.empty();
          }
        );
    }

    @NotNull
    @Override
    public Iterator<E> iterator()
    {
        return new FilteredIterator<>(source, filter);
    }

    private static class FilteredIterator<T extends IRegistryEntry, E extends IRegistryEntry> implements Iterator<E> {

        private final Function<T, Optional<E>> filter;
        private final Iterator<T> sourceIterator;
        private E nextSourceElement = null;
        private boolean potentiallyHasNext = true;

        private FilteredIterator(final IRegistryView<T> source, final Function<T, Optional<E>> filter) {
            this.filter = filter;
            this.sourceIterator = source.iterator();

            setupNextSourceElement();
        }

        private void setupNextSourceElement() {
            while(nextSourceElement == null && sourceIterator.hasNext()) {
                final T next = sourceIterator.next();
                final Optional<E> filtered = filter.apply(next);
                if (filtered.isPresent()) {
                    nextSourceElement = filtered.get();
                    break;
                }
            }

            potentiallyHasNext = sourceIterator.hasNext();
        }

        @Override
        public boolean hasNext()
        {
            return potentiallyHasNext;
        }

        @Override
        public E next()
        {
            final E next = nextSourceElement;
            nextSourceElement = null;
            setupNextSourceElement();
            return next;
        }
    }
}
