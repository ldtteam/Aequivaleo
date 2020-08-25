package com.ldtteam.aequivaleo.gameobject.equivalent;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.gameobject.equivalent.IGameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.api.util.TypeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

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

    private final Map<Class<?>, Set<BiFunction<ICompoundContainer<?>, ICompoundContainer<?>, Optional<Boolean>>>> handlers = Maps.newConcurrentMap();

    public <L, R> boolean areGameObjectsEquivalent(
      @NotNull final ICompoundContainer<L> left, @NotNull final ICompoundContainer<R> right)
    {
        if (left.getContents().getClass() != right.getContents().getClass())
            return false;

        final Set<Class<?>> superTypes = TypeUtils.getAllSuperTypesExcludingObject(left.getContents().getClass());

        return handlers
          .entrySet()
          .stream()
          .filter(e -> superTypes.contains(e.getKey()))
          .flatMap(e -> e.getValue().stream())
          .map(handler -> handler.apply(left, right))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .findFirst()
          .orElse(false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> IGameObjectEquivalencyHandlerRegistry registerNewHandler(
      @NotNull final Class<T> gameObjectClass,
      @NotNull final BiFunction<ICompoundContainer<T>, ICompoundContainer<T>, Optional<Boolean>> handler)
    {
        handlers.computeIfAbsent(gameObjectClass, (cls) -> Sets.newConcurrentHashSet()).add((iCompoundContainerWrapper, iCompoundContainerWrapper2) -> handler.apply((ICompoundContainer<T>) iCompoundContainerWrapper, (ICompoundContainer<T>) iCompoundContainerWrapper2));
        return this;
    }
}
