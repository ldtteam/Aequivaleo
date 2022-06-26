package com.ldtteam.aequivaleo.api.registry;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A view of a given registry.
 * @param <T>
 */
public interface IRegistryView<T extends IRegistryEntry> extends Iterable<T>
{

    /**
     * Gets the entry with the given name from the registry or an empty optional if no entry with the given name exists.
     *
     * @param name The name to get the entry for.
     * @return The entry with the given name or an empty optional if no entry with the given name exists.
     */
    Optional<T> get(final ResourceLocation name);

    /**
     * Gets the name of the given registry entry.
     *
     * @param entry The entry to get the name from.
     * @return The name of the entry.
     */
    default ResourceLocation getRegistryNameOf(T entry) {
        return entry.getRegistryName();
    }

    /**
     * Returns a stream of all entries in this registry.
     *
     * @return The stream of all entries in this registry.
     */
    Stream<T> stream();

    /**
     * Creates a filtered view of this registry.
     * Note: This is a view, the returned instances of the entries in the view might not be the same between calls,
     * if synchronization happens between said calls.
     *
     * @param viewFilter The view filter.
     * @return The registry view with the filter applied.
     */
    <E extends IRegistryEntry> IRegistryView<E> createView(Function<T, Optional<E>> viewFilter);
}
