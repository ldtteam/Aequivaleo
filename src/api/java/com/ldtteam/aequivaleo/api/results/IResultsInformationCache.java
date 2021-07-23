package com.ldtteam.aequivaleo.api.results;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A instance of a cache that contains the results of a calculation.
 *
 * Generally a single instance per world exists, however they might or might not be recycled when resource and datapacks reload.
 */

@Deprecated
public interface IResultsInformationCache extends IEquivalencyResults
{
    /**
     * Gives access to the current instance of the cache.
     *
     * @param worldKey The key for the world for which the instance is retrieved.
     *
     * @return The cache.
     */
    @Deprecated
    static IResultsInformationCache getInstance(@NotNull final ResourceKey<Level> worldKey) {
        return IAequivaleoAPI.getInstance().getResultsInformationCache(worldKey);
    }

    /**
     * Gives access to the calculation result of a single container.
     * If the container is not in unit form, he will be turned into a container that is in unit form, by using a duplicate.
     *
     * @param container The container in question.
     * @return A sets containing the results if present, else an empty set is returned.
     */
    @Deprecated
    @NotNull
    default Set<CompoundInstance> getFor(@NotNull final ICompoundContainer<?> container){
        return dataFor(container);
    }

    /**
     * Gives access to the calculation result of a single in game object.
     *
     * @param object The in game object in question.
     * @param <T> The type of object you wish to get the compound instances for.
     * @return A sets containing the results if present, else an empty set is returned.
     */
    @Deprecated
    @NotNull
    default <T> Set<CompoundInstance> getFor(@NotNull final T object) throws IllegalArgumentException {
        final ICompoundContainer<?> unitContainer = IAequivaleoAPI.Holder.getInstance().getCompoundContainerFactoryManager().wrapInContainer(object, 1d);
        return getFor(unitContainer);
    }

    /**
     * Gives access to the calculation result of a single container, in the form of the processed cache result.
     * If the container is not in unit form, he will be turned into a container that is in unit form, by using a duplicate.
     *
     * @param group The group that determines the processed cached result.
     * @param container The container in question.
     * @param <R> The type of the processed cache result.
     * @return An optional containing the processed results if present, else an empty optional is returned.
     */
    @Deprecated
    @NotNull
    default <R> Optional<R> getCacheFor(@NotNull final ICompoundTypeGroup group, @NotNull final ICompoundContainer<?> container) {
        return mappedDataFor(group, container);
    }

    /**
     * Gives access to the calculation result of a single in game object, in the form of the processed cache result.
     *
     * @param group The group that determines the processed cached result.
     * @param object The in game object in question.
     * @param <T> The type of object you wish to get the processed cache result for.
     * @param <R> The type of the processed cache result.
     * @return An optional containing the processed results if present, else an empty optional is returned.
     */
    @Deprecated
    @NotNull
    default <R, T> Optional<R> getCacheFor(@NotNull final ICompoundTypeGroup group, @NotNull final T object) throws IllegalArgumentException {
        final ICompoundContainer<?> unitContainer = IAequivaleoAPI.Holder.getInstance().getCompoundContainerFactoryManager().wrapInContainer(object, 1d);
        return getCacheFor(group, unitContainer);
    }

    /**
     * Returns all cached data for a given group.
     * If a container is not contained in this map, then no value was calculated for it.
     *
     * @param group The group to get the data of.
     * @return An unmodifiable map that returns all the cached results. Don't modify its contents.
     */
    @Deprecated
    default <R> Map<ICompoundContainer<?>, R> getAllCachedDataOf(ICompoundTypeGroup group) {
        return getAllMappedDataOf(group);
    }
}
