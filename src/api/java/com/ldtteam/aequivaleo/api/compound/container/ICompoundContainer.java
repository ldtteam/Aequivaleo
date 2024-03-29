package com.ldtteam.aequivaleo.api.compound.container;

import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.mojang.serialization.Codec;

/**
 * Holds an object that is made up out of compounds.
 * @param <T> The type of game object that is held.
 */
public interface ICompoundContainer<T> extends Comparable<ICompoundContainer<?>>
{
    /**
     * The codec that can be used to serialize and deserialize a container.
     */
    Codec<ICompoundContainer<?>> CODEC = ModRegistries.CONTAINER_FACTORY.byNameCodec()
                                                 .dispatch(
                                                        ICompoundContainer::type,
                                                        ICompoundContainerType::codec
                                                 );
    
    /**
     * Creates a container from the given source object.
     * The source object needs to have an innate count that its factory is aware of.
     *
     * @param object The to wrap in a container.
     * @return The container which represents the given object.
     */
    static ICompoundContainer<?> from(Object object) {
        return ICompoundContainerFactoryManager.getInstance().wrapInContainer(object);
    }

    /**
     * Creates a container from the given source object.
     *
     * @param object The to wrap in a container.
     * @param count The count of objects in the container.
     * @return The container which represents the given object.
     */
    static ICompoundContainer<?> from(Object object, int count) {
        return from(object, (double) count);
    }

    /**
     * Creates a container from the given source object.
     *
     * @param object The to wrap in a container.
     * @param count The count of objects in the container.
     * @return The container which represents the given object.
     */
    static ICompoundContainer<?> from(Object object, double count) {
        return ICompoundContainerFactoryManager.getInstance().wrapInContainer(object, count);
    }

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
    T contents();

    /**
     * The amount of {@code T}s contained in this wrapper.
     * @return The amount.
     */
    Double contentsCount();

    /**
     * Indicates if this container's locked information
     * can be loaded from disk.
     * <br/>
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
    
    /**
     * The type of container this is.
     *
     * @return The type of container this is.
     */
    ICompoundContainerType<T> type();
}
