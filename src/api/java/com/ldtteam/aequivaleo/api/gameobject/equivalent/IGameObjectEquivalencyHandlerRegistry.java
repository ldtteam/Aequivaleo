package com.ldtteam.aequivaleo.api.gameobject.equivalent;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

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
     * @param <L> The left type of game object that the handler can check.
     * @param <R> The right type of game object that the handler can check.
     * @param canHandleLeftPredicate Invoked by the system to figure out if a given compound container can be handled by the left side of this handler.
     * @param canHandleRightPredicate Invoked by the system to figure out if a given compound container can be handled by the right side of this handler.
     * @param handler The callback to add to the registry, which can help with equivalency checking for game objects.
     * @return The registry with the handler added.
     */
    <L, R> IGameObjectEquivalencyHandlerRegistry registerNewHandler(
      @NotNull final Predicate<ICompoundContainer<?>> canHandleLeftPredicate,
      final Predicate<ICompoundContainer<?>> canHandleRightPredicate,
      @NotNull final BiFunction<ICompoundContainer<L>, ICompoundContainer<R>, Optional<Boolean>> handler);

    /**
     * Registers a handler that can validate if two game objects contained in wrapper are actually equal to one another.
     * The callback should not check the wrapper or the internal size of the game object.
     *
     * @param <T> The type of game object that the handler can check.
     * @param canHandlePredicate Invoked by the system to figure out if a given compound container can be handled by this handler.
     * @param handler The callback to add to the registry, which can help with equivalency checking for game objects.
     * @return The registry with the handler added.
     */
    default <T> IGameObjectEquivalencyHandlerRegistry registerNewHandler(
      @NotNull final Predicate<ICompoundContainer<?>> canHandlePredicate,
      @NotNull final BiFunction<ICompoundContainer<T>, ICompoundContainer<T>, Optional<Boolean>> handler) {
        return this.registerNewHandler(
          canHandlePredicate,
          canHandlePredicate,
          handler
        );
    }

}
