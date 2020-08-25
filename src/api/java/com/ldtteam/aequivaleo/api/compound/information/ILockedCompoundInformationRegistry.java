package com.ldtteam.aequivaleo.api.compound.information;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.ldtteam.aequivaleo.api.compound.ICompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
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
public interface ILockedCompoundInformationRegistry
{

    /**
     * Registers a given set of compound instances to a given wrapper.
     * Forces the given wrapper to have the compound instances regardless of analysis results.
     *
     * @param wrapper The wrapper to assign the compound instances to.
     * @param compounds The instances to assign to the given wrapper.
     * @return The registry with the locking assigned.
     */
    ILockedCompoundInformationRegistry registerLocking(@NotNull final ICompoundContainer<?> wrapper, @NotNull final Set<ICompoundInstance> compounds);

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
    <T> ILockedCompoundInformationRegistry registerLocking(@NotNull final T gameObjectInstanceToLock, @NotNull final Set<ICompoundInstance> compounds);

    /**
     * Gives access to locking data.
     * @return The locking data.
     */
    ImmutableMap<ICompoundContainer<?>, ImmutableSet<ICompoundInstance>> get();
}
