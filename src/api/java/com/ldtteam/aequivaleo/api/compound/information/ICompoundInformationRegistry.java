package com.ldtteam.aequivaleo.api.compound.information;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * A registry that contains information with regards to fixed compound types for a given wrapper.
 * These fixed compound types help the system as a starting point during analysis, as such it should be noted that
 * every "root" ingredient (an ingredient that can not be created by the player by crafting, but only by collecting for example)
 * should as such be included in this registry so that the entire recipe tree can be calculated from it.
 *
 * Additionally none "root" ingredients can use this registry to force the analysis engine to assign a given compound container a set of instances.
 */
public interface ICompoundInformationRegistry
{

    /**
     * Gives access to the current instance of the locked information registry.
     *
     * @param worldKey The key for the world for which the instance is retrieved.
     *
     * @return The locked information registry.
     */
    static ICompoundInformationRegistry getInstance(@NotNull final ResourceKey<Level> worldKey) {
        return IAequivaleoAPI.getInstance().getLockedCompoundWrapperToTypeRegistry(worldKey);
    }


    /**
     * Registers a given set of compound instances to a given wrapper.
     * Forces the given wrapper to have the compound instances at the start of the calculation.
     * If the calculation determines that a different value is more appropriate then the given value will be overridden.
     *
     * @param wrapper The wrapper to assign the compound instances to.
     * @param compounds The instances to assign to the given wrapper.
     * @return The registry with the locking assigned.
     */
    ICompoundInformationRegistry registerValue(@NotNull final ICompoundContainer<?> wrapper, @NotNull final Set<CompoundInstance> compounds);

    /**
     * Registers a given set of compound instances to a given game object.
     * Forces the given wrapper to have the compound instances at the start of the calculation.
     * If the calculation determines that a different value is more appropriate then the given value will be overridden.
     *
     * Equivalency will need to know how to turn the game object into a compound container wrapper, so a factory for it will need to be registered.
     *
     * @param gameObjectInstanceToAssign The game object to assign the compound instances to.
     * @param compounds The instances to assign to the given game object.
     * @param <T> The type of the game object to assign the instances to.
     * @return The registry with the locking assigned.
     */
    <T> ICompoundInformationRegistry registerValue(@NotNull final T gameObjectInstanceToAssign, @NotNull final Set<CompoundInstance> compounds);

    /**
     * Registers a given set of compound instances to a given wrapper.
     * Forces the given wrapper to have the compound instances regardless of analysis results.
     *
     * @param wrapper The wrapper to assign the compound instances to.
     * @param compounds The instances to assign to the given wrapper.
     * @return The registry with the locking assigned.
     */
    ICompoundInformationRegistry registerLocking(@NotNull final ICompoundContainer<?> wrapper, @NotNull final Set<CompoundInstance> compounds);

    /**
     * Registers a given set of compound instances to a given game object.
     *
     * Equivalency will need to know how to turn the game object into a compound container wrapper, so a factory for it will need to be registered.
     *
     * @param gameObjectInstanceToLock The game object to assign the compound instances to.
     * @param compounds The instances to assign to the given game object.
     * @param <T> The type of the game object to assign the instances to.
     * @return The registry with the locking assigned.
     */
    <T> ICompoundInformationRegistry registerLocking(@NotNull final T gameObjectInstanceToLock, @NotNull final Set<CompoundInstance> compounds);


    /**
     * Registers a given set of compound instances to a given wrapper.
     * Base values are values which will be added to the calculated results if the objects ever gets visited, but if it is not visited then the values are discarded.
     *
     * @param wrapper The wrapper to assign the compound instances to.
     * @param compounds The instances to assign to the given wrapper.
     * @return The registry with the locking assigned.
     */
    ICompoundInformationRegistry registerBase(@NotNull final ICompoundContainer<?> wrapper, @NotNull final Set<CompoundInstance> compounds);

    /**
     * Registers a given set of compound instances to a given game object.
     * Base values are values which will be added to the calculated results if the objects ever gets visited, but if it is not visited then the values are discarded.
     * Equivalency will need to know how to turn the game object into a compound container wrapper, so a factory for it will need to be registered.
     *
     * @param gameObjectInstanceToLock The game object to assign the compound instances to.
     * @param compounds The instances to assign to the given game object.
     * @param <T> The type of the game object to assign the instances to.
     * @return The registry with the locking assigned.
     */
    <T> ICompoundInformationRegistry registerBase(@NotNull final T gameObjectInstanceToLock, @NotNull final Set<CompoundInstance> compounds);

    /**
     * Gives access to value data.
     * @return The value data.
     */
    ImmutableMap<ICompoundContainer<?>, ImmutableSet<CompoundInstance>> getValueInformation();

    /**
     * Gives access to locking data.
     * @return The locking data.
     */
    ImmutableMap<ICompoundContainer<?>, ImmutableSet<CompoundInstance>> getLockingInformation();
}
