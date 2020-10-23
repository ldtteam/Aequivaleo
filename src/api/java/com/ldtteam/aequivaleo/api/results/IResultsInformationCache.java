package com.ldtteam.aequivaleo.api.results;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
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
     * Returns the calculated and cached resulting information.
     * @return The calculated and cached data, if no information is available, then an empty map is returned.
     */
    Map<ICompoundContainer<?>, Set<CompoundInstance>> getAll();

    /**
     * Gives access to the calculation result of a single container.
     * If the container is not in unit form, he will be turned into a container that is in unit form, by using a duplicate.
     *
     * @param container The container in question.
     * @return A sets containing the results if present, else an empty set is returned.
     */
    @NotNull
    default Set<CompoundInstance> getFor(@NotNull final ICompoundContainer<?> container) {
       final ICompoundContainer<?> unitContainer = container.getContentsCount() == 1d ? container :
                 IAequivaleoAPI.Holder.getInstance().getCompoundContainerFactoryManager().wrapInContainer(container.getContents(), 1d);

       return getAll().getOrDefault(unitContainer, Collections.emptySet());
    }

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
        return getAll().getOrDefault(unitContainer, Collections.emptySet());
    }
}
