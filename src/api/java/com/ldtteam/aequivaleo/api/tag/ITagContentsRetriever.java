package com.ldtteam.aequivaleo.api.tag;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagKey;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Allows for the retrieval of tag contents.
 */
public interface ITagContentsRetriever {

    /**
     * Gets the instance of the tag contents retriever.
     *
     * @return The instance of the tag contents retriever.
     */
    static ITagContentsRetriever getInstance() {
        return IAequivaleoAPI.getInstance().getTagContentsRetriever();
    }

    /**
     * Registers a handler for a tag.
     * Of all registered handlers: The first one that returns a non-empty set of contents will be used.
     * <p>
     * If no handler is registered for a tag, the tag will be considered empty.
     *
     * @param selector The selector for the tag.
     * @param retriever The retriever for the tag.
     * @param <T> The type of the tag.
     */
    <T> void registerHandler(final Predicate<TagKey<T>> selector, final BiFunction<RegistryAccess, TagKey<T>, Set<ICompoundContainer<?>>> retriever);
}
