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

    private final LinkedList<EquivalencyHandler<?, ?>> handlers = new LinkedList<>();

    public boolean areGameObjectsEquivalent(
      @NotNull final ICompoundContainer<?> left, @NotNull final ICompoundContainer<?> right)
    {
        if (left.contents().getClass() != right.contents().getClass())
            return false;

        for (Iterator<EquivalencyHandler<?, ?>> iterator = handlers.descendingIterator(); iterator.hasNext(); )
        {
            final EquivalencyHandler<?, ?> handler = iterator.next();
            Optional<Boolean> handleResult = attemptHandle(handler, left, right);
            if (handleResult.isPresent())
            {
                return handleResult.get();
            }
        }
        return false;
    }

    @SuppressWarnings(Suppression.UNCHECKED)
    private static <L, R> Optional<Boolean> attemptHandle(
      final EquivalencyHandler<L, R> handler,
      final ICompoundContainer<?> left,
      final ICompoundContainer<?> right
    ) {
        if (handler.getCanHandleLeftPredicate().test(left) && handler.getCanHandleRightPredicate().test(right))
            return handler.getHandler().apply((ICompoundContainer<L>) left, (ICompoundContainer<R>) right);

        return Optional.empty();
    }


    @Override
    public <L, R> IGameObjectEquivalencyHandlerRegistry registerNewHandler(
      @NotNull final Predicate<ICompoundContainer<?>> canHandleLeftPredicate,
      final Predicate<ICompoundContainer<?>> canHandleRightPredicate,
      @NotNull final BiFunction<ICompoundContainer<L>, ICompoundContainer<R>, Optional<Boolean>> handler)
    {
        this.handlers.add(new EquivalencyHandler<>(canHandleLeftPredicate, canHandleRightPredicate, handler));
        return this;
    }

    private static final class EquivalencyHandler<L, R> {
        private final Predicate<ICompoundContainer<?>> canHandleLeftPredicate;
        private final Predicate<ICompoundContainer<?>> canHandleRightPredicate;
        private final BiFunction<ICompoundContainer<L>, ICompoundContainer<R>, Optional<Boolean>> handler;

        private EquivalencyHandler(
          final Predicate<ICompoundContainer<?>> canHandleLeftPredicate,
          final Predicate<ICompoundContainer<?>> canHandleRightPredicate,
          final BiFunction<ICompoundContainer<L>, ICompoundContainer<R>, Optional<Boolean>> handler) {
            this.canHandleLeftPredicate = canHandleLeftPredicate;
            this.canHandleRightPredicate = canHandleRightPredicate;
            this.handler = handler;
        }

        public Predicate<ICompoundContainer<?>> getCanHandleLeftPredicate()
        {
            return canHandleLeftPredicate;
        }

        public Predicate<ICompoundContainer<?>> getCanHandleRightPredicate()
        {
            return canHandleRightPredicate;
        }

        public BiFunction<ICompoundContainer<L>, ICompoundContainer<R>, Optional<Boolean>> getHandler()
        {
            return handler;
        }
    }
}
