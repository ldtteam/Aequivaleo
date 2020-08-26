package com.ldtteam.aequivaleo.api.gameobject.equivalent;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * A registry that manages handlers that control if two game objects (in their wrappers) are equivalent.
 * The checks should not care for the count contained in the wrapper, just the actual target object, and if the target object has a internal size this should be
 * disregarded as well, since the system will take care of this during analysis.
 */
public interface IGameObjectEquivalencyHandlerRegistry
{

    /**
     * Gives access to the current instance of the handler registry.
     *
     * @return The handler registry.
     */
    static IGameObjectEquivalencyHandlerRegistry getInstance() {
        return IAequivaleoAPI.getInstance().getGameObjectEquivalencyHandlerRegistry();
    }

    /**
     * Registers a handler that can validate if two game objects contained in wrapper are actually equal to one another.
     * The callback should not check the wrapper or the internal size of the game object.
     *
     * @param gameObjectClass The class of the game object which is handled by the handler.
     * @param handler The callback to add to the registry, which can help with equivalency checking for game objects.
     * @param <T> The type of game object that the handler can check.
     * @return The registry with the handler added.
     */
    <T> IGameObjectEquivalencyHandlerRegistry registerNewHandler(
      @NotNull final Class<T> gameObjectClass,
      @NotNull final BiFunction<ICompoundContainer<T>, ICompoundContainer<T>, Optional<Boolean>> handler);
}
