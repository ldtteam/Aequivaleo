package com.ldtteam.aequivaleo.api.results;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public interface IResultsInformationCache
{

    /**
     * Gives access to the current instance of the cache.
     *
     * @param worldKey The key for the world for which the instance is retrieved.
     *
     * @return The cache.
     */
    static IResultsInformationCache getInstance(@NotNull final RegistryKey<World> worldKey) {
        return IAequivaleoAPI.getInstance().getResultsInformationCache(worldKey);
    }

    /**
     * Gives access to the calculation result of a single container.
     * If the container is not in unit form, he will be turned into a container that is in unit form, by using a duplicate.
     *
     * @param container The container in question.
     * @return A sets containing the results if present, else an empty set is returned.
     */
    @NotNull
    Set<CompoundInstance> getFor(@NotNull final ICompoundContainer<?> container);

    /**
     * Gives access to the calculation result of a single in game object.
     *
     * @param object The in game object in question.
     * @param <T> The type of object you wish to get the compound instances for.
     * @return A sets containing the results if present, else an empty set is returned.
     */
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
    @NotNull
    <R> Optional<R> getCacheFor(@NotNull final ICompoundTypeGroup group, @NotNull final ICompoundContainer<?> container);

    /**
     * Gives access to the calculation result of a single in game object, in the form of the processed cache result.
     *
     * @param group The group that determines the processed cached result.
     * @param object The in game object in question.
     * @param <T> The type of object you wish to get the processed cache result for.
     * @param <R> The type of the processed cache result.
     * @return An optional containing the processed results if present, else an empty optional is returned.
     */
    @NotNull
    default <R, T> Optional<R> getCacheFor(@NotNull final ICompoundTypeGroup group, @NotNull final T object) throws IllegalArgumentException {
        final ICompoundContainer<?> unitContainer = IAequivaleoAPI.Holder.getInstance().getCompoundContainerFactoryManager().wrapInContainer(object, 1d);
        return getCacheFor(group, unitContainer);
    }
}
