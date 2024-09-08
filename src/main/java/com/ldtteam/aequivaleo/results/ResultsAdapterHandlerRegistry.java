package com.ldtteam.aequivaleo.results;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.results.IResultsAdapterHandlerRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ResultsAdapterHandlerRegistry implements IResultsAdapterHandlerRegistry
{
    private static final ResultsAdapterHandlerRegistry INSTANCE = new ResultsAdapterHandlerRegistry();

    public static ResultsAdapterHandlerRegistry getInstance()
    {
        return INSTANCE;
    }

    private final Queue<Entry<?>> alternativeHandlers = new ConcurrentLinkedQueue<Entry<?>>();

    private ResultsAdapterHandlerRegistry()
    {
        registerHandler(Item.class::isInstance, (Function<Item, Set<?>>) item -> Set.of(item.getDefaultInstance()));
        registerHandler(ItemStack.class::isInstance, (Function<ItemStack, Set<?>>) itemStack -> Set.of(itemStack.getItem(), itemStack.getItem().getDefaultInstance()));

        registerHandler(Fluid.class::isInstance, (Function<Fluid, Set<?>>) fluid -> Set.of(new FluidStack(fluid, 1)));
        registerHandler(FluidStack.class::isInstance, (Function<FluidStack, Set<?>>) fluidStack -> Set.of(fluidStack.getFluid()));
    }

    @Override
    public <T> IResultsAdapterHandlerRegistry registerHandler(final Predicate<T> canHandlePredicate, final Function<T, Set<?>> alternativesProducer)
    {
        this.alternativeHandlers.add(new Entry<>(canHandlePredicate, alternativesProducer));
        return this;
    }

    public Set<ICompoundContainer<?>> produceAlternatives(final Object target) {
        for (final Entry<?> alternativeHandler : alternativeHandlers)
        {
            final Optional<Set<?>> result = handleEntry(target, alternativeHandler);
            if (result.isPresent())
            {
                return result.get().stream()
                        .map(obj -> ICompoundContainer.from(obj, 1))
                        .collect(Collectors.toSet());
            }
        }

        return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<Set<?>> handleEntry(final Object target, final Entry<T> entry) {
        if (entry.canHandle(target)) {
            return Optional.ofNullable(entry.alternativesProducer().apply((T) target));
        }

        return Optional.empty();
    }

    private record Entry<T>(Predicate<T> canHandleCallback, Function<T, Set<?>> alternativesProducer) {

        @SuppressWarnings("unchecked")
        public boolean canHandle(final Object target) {
            try {
                return canHandleCallback.test((T) target);
            } catch (final ClassCastException e) {
                return false;
            }
        }
    }
}
