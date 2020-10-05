package com.ldtteam.aequivaleo.gameobject.equivalent;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.gameobject.equivalent.IGameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.api.util.Suppression;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class GameObjectEquivalencyHandlerRegistry implements IGameObjectEquivalencyHandlerRegistry
{
    private static final GameObjectEquivalencyHandlerRegistry INSTANCE = new GameObjectEquivalencyHandlerRegistry();

    public static GameObjectEquivalencyHandlerRegistry getInstance()
    {
        return INSTANCE;
    }

    private GameObjectEquivalencyHandlerRegistry()
    {
    }

    private final LinkedList<EquivalencyHandler<?>> handlers = new LinkedList<>();

    public boolean areGameObjectsEquivalent(
      @NotNull final ICompoundContainer<?> left, @NotNull final ICompoundContainer<?> right)
    {
        if (left.getContents().getClass() != right.getContents().getClass())
            return false;

        for (Iterator<EquivalencyHandler<?>> iterator = handlers.descendingIterator(); iterator.hasNext(); )
        {
            final EquivalencyHandler<?> handler = iterator.next();
            Optional<Boolean> handleResult = attemptHandle(handler, left, right);
            if (handleResult.isPresent())
            {
                return handleResult.get();
            }
        }
        return false;
    }

    @SuppressWarnings(Suppression.UNCHECKED)
    private static <T> Optional<Boolean> attemptHandle(
      final EquivalencyHandler<T> handler,
      final ICompoundContainer<?> left,
      final ICompoundContainer<?> right
    ) {
        if (handler.getCanHandlePredicate().test((ICompoundContainer<T>) left) && handler.getCanHandlePredicate().test((ICompoundContainer<T>) right))
            return handler.getHandler().apply((ICompoundContainer<T>) left, (ICompoundContainer<T>) right);

        return Optional.empty();
    }


    @Override
    public <T> IGameObjectEquivalencyHandlerRegistry registerNewHandler(
      @NotNull final Predicate<ICompoundContainer<?>> canHandlePredicate, @NotNull final BiFunction<ICompoundContainer<T>, ICompoundContainer<T>, Optional<Boolean>> handler)
    {
        this.handlers.add(new EquivalencyHandler<>(canHandlePredicate, handler));
        return this;
    }

    private static final class EquivalencyHandler<T> {
        private final Predicate<ICompoundContainer<?>> canHandlePredicate;
        private final BiFunction<ICompoundContainer<T>, ICompoundContainer<T>, Optional<Boolean>> handler;

        private EquivalencyHandler(
          final Predicate<ICompoundContainer<?>> canHandlePredicate,
          final BiFunction<ICompoundContainer<T>, ICompoundContainer<T>, Optional<Boolean>> handler) {
            this.canHandlePredicate = canHandlePredicate;
            this.handler = handler;
        }

        public Predicate<ICompoundContainer<?>> getCanHandlePredicate()
        {
            return canHandlePredicate;
        }

        public BiFunction<ICompoundContainer<T>, ICompoundContainer<T>, Optional<Boolean>> getHandler()
        {
            return handler;
        }
    }
}
