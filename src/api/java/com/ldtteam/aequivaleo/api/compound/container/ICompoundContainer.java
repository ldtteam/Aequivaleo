package com.ldtteam.aequivaleo.api.compound.container;

/**
 * Holds a object that is made up out of compounds.
 * @param <T> The type of game object that is held.
 */
public interface ICompoundContainer<T> extends Comparable<ICompoundContainer<?>>
{

    /**
     * Indicates if a container is valid.
     * A recipe with an invalid container is not considered for evaluation.
     *
     * @return {@code True} when valid, {@code False} when not.
     */
    boolean isValid();

    /**
     * The contents of this container.
     * Set to the 1 unit of the content type {@code T}
     *
     * @return The contents.
     */
    T getContents();

    /**
     * The amount of {@code T}s contained in this wrapper.
     * @return The amount.
     */
    Double getContentsCount();

    /**
     * Indicates if this containers locked information
     * can be loaded from disk.
     *
     * This if for example false for ItemStacks.
     *
     * @return True to indicate that data can be loaded form disk, false when not.
     */
    default boolean canBeLoadedFromDisk() {
        return false;
    }

    /**
     * Gives access to the content as a filename.
     *
     * @return a file name that represents the content.
     */
    default String getContentAsFileName() {
        throw new UnsupportedOperationException();
    }
}
