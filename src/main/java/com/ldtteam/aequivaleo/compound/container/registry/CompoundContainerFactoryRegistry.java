package com.ldtteam.aequivaleo.compound.container.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryRegistry;
import com.ldtteam.aequivaleo.api.util.Suppression;
import com.ldtteam.aequivaleo.api.util.TypeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.Validate.notNull;

public class CompoundContainerFactoryRegistry implements ICompoundContainerFactoryRegistry
{

    private static final CompoundContainerFactoryRegistry INSTANCE = new CompoundContainerFactoryRegistry();

    public static CompoundContainerFactoryRegistry getInstance()
    {
        return INSTANCE;
    }

    private final Set<ExactTypedRegistryEntry<?, ?>> typedRegistryEntries = Sets.newConcurrentHashSet();

    private CompoundContainerFactoryRegistry()
    {
    }

    @Override
    public <T, R> ICompoundContainerFactoryRegistry register(@NotNull final ICompoundContainerFactory<T, R> factory)
    {
        this.typedRegistryEntries.add(new ExactTypedRegistryEntry<>(factory.getInputType(), factory.getOutputType(), factory));
        return this;
    }

    /**
     * Utility method to check if a given class of a compound container can be wrapped properly.
     *
     * @param inputType The class to check
     * @param <T>   The type of the compound container to check.
     * @return True when a factory for type T is registered false when not.
     */
    public <T> boolean canBeWrapped(@NotNull final Class<T> inputType)
    {
        notNull(inputType);
        return getFactoryForType(inputType).isPresent();
    }

    /**
     * Utility method to check if a given instance of a possible compound container can be wrapped properly.
     *
     * @param gameObject The instance to check
     * @param <T>       The type of the compound container to check.
     * @return True when a factory for type T is registered false when not.
     */
    public <T> boolean canBeWrapped(@NotNull final T gameObject)
    {
        notNull(gameObject);
        return canBeWrapped(gameObject.getClass());
    }

    /**
     * Utility method to check if a given class of a compound container can be wrapped properly.
     *
     * @param inputType The class to check
     * @param outputType The class of the output to check
     * @param <T>   The type of the compound container to check.
     * @param <R>   The type of the factory output to check.
     * @return True when a factory for type T is registered false when not.
     */
    public <T, R> boolean canBeWrapped(@NotNull final Class<T> inputType, @NotNull final Class<R> outputType)
    {
        notNull(inputType);
        notNull(outputType);
        return getFactoryForType(inputType, outputType).isPresent();
    }

    /**
     * Utility method to check if a given instance of a possible compound container can be wrapped properly.
     *
     * @param gameObject The instance to check
     * @param outputType The class of the output to check
     * @param <T>   The type of the compound container to check.
     * @param <R>   The type of the factory output to check.
     * @return True when a factory for type T is registered false when not.
     */
    public <T, R> boolean canBeWrapped(@NotNull final T gameObject, @NotNull final Class<R> outputType)
    {
        notNull(gameObject);
        notNull(outputType);
        return canBeWrapped(gameObject.getClass(), outputType);
    }

    /**
     * Wraps the given compound container.
     *
     * @param gameObject The instance of T to wrap. Will be brought to unit length by the factory (ItemStacks will be copied and have stack size 1).
     * @param count     The count to store in the container. For ItemStacks this will be the stacks size.
     * @param <T>       The type of the compound container to create.
     * @return The wrapped instance.
     * @throws IllegalArgumentException When T can not be wrapped properly {@code canBeWrapped(tInstance) == false;}
     */
    @SuppressWarnings(Suppression.UNCHECKED)
    @NotNull
    public <T> ICompoundContainer<?> wrapInContainer(@NotNull final T gameObject, @NotNull final double count) throws IllegalArgumentException
    {
        notNull(gameObject);
        final Class<T> inputType = (Class<T>) gameObject.getClass();
        return getFactoryForType(inputType).map(factory -> factory.create(gameObject, count))
                 .orElseThrow(() -> new IllegalArgumentException("Unknown wrapping type: " + gameObject.getClass()));
    }

    /**
     * Wraps the given compound container.
     *
     * @param gameObject The instance of T to wrap. Will be brought to unit length by the factory (ItemStacks will be copied and have stack size 1).
     * @param count     The count to store in the container. For ItemStacks this will be the stacks size.
     * @param <T>       The type of the compound container to create.
     * @return The wrapped instance.
     * @throws IllegalArgumentException When T can not be wrapped properly {@code canBeWrapped(tInstance) == false;}
     */
    @SuppressWarnings(Suppression.UNCHECKED)
    @NotNull
    public <T, R> ICompoundContainer<R> wrapInContainer(@NotNull final T gameObject, @NotNull final double count, @NotNull final Class<R> outputType) throws IllegalArgumentException
    {
        notNull(gameObject);
        notNull(outputType);
        final Class<T> inputType = (Class<T>) gameObject.getClass();
        return getFactoryForType(inputType, outputType).map(factory -> factory.create(gameObject, count)).orElseThrow(() -> new IllegalArgumentException("Unknown wrapping type."));
    }

    /**
     * Internal method to get a factory of a given type.
     *
     * @param input The class of the type as input to get the factory for.
     * @param <T>   The type to get the wrapping factory for.
     * @return An optional, possibly containing the requested factory if registered.
     */
    @NotNull
    @SuppressWarnings(Suppression.UNCHECKED)
    private <T> Optional<? extends ICompoundContainerFactory<? super T, ?>> getFactoryForType(@NotNull final Class<T> input)
    {
        notNull(input);
        final Set<Class<?>> superTypes = TypeUtils.getAllSuperTypesExcludingObject(input);

        return this.typedRegistryEntries.stream()
                 .filter(e -> superTypes.contains(e.getInputType()))
                 .map(ExactTypedRegistryEntry::getFactory)
                 .map(f -> (ICompoundContainerFactory<? super T, ?>) f)
                 .findFirst();
    }

    /**
     * Internal method to get a factory of a given type.
     *
     * @param input The class of the type as input to get the factory for.
     * @param output The class that the factory produces an output for.
     * @param <T>   The type to get the wrapping factory for.
     * @param <R>   The type that the factory produces and output for.
     * @return An optional, possibly containing the requested factory if registered.
     */
    @NotNull
    @SuppressWarnings(Suppression.UNCHECKED)
    private <T, R> Optional<? extends ICompoundContainerFactory<? super T, R>> getFactoryForType(@NotNull final Class<T> input, @NotNull final Class<R> output)
    {
        notNull(input);

        final Set<Class<?>> inputSuperTypes = TypeUtils.getAllSuperTypesExcludingObject(input);

        return this.typedRegistryEntries.stream()
                 .filter(e -> inputSuperTypes.contains(e.getInputType()) && e.getOutputType() == output)
                 .map(ExactTypedRegistryEntry::getFactory)
                 .map(f -> (ICompoundContainerFactory<T, R>) f)
                 .findFirst();
    }

    @NotNull
    public ImmutableList<ICompoundContainerFactory<?, ?>> getAllKnownFactories()
    {
        return ImmutableList.<ICompoundContainerFactory<?, ?>>builder().addAll(
          this.typedRegistryEntries.stream().map(ExactTypedRegistryEntry::getFactory).collect(Collectors.toList())
        ).build();
    }

    private static class ExactTypedRegistryEntry<T, R>
    {
        @NotNull
        private final Class<T> inputType;

        @NotNull
        private final Class<R> outputType;

        @NotNull
        private final ICompoundContainerFactory<T, R> factory;

        private ExactTypedRegistryEntry(
          @NotNull final Class<T> inputType,
          @NotNull final Class<R> outputType,
          @NotNull final ICompoundContainerFactory<T, R> factory)
        {
            this.inputType = inputType;
            this.outputType = outputType;
            this.factory = factory;
        }

        @NotNull
        Class<T> getInputType()
        {
            return inputType;
        }

        @NotNull
        Class<R> getOutputType()
        {
            return outputType;
        }

        @NotNull
        ICompoundContainerFactory<T, R> getFactory()
        {
            return factory;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof ExactTypedRegistryEntry))
            {
                return false;
            }

            final ExactTypedRegistryEntry<?, ?> that = (ExactTypedRegistryEntry<?, ?>) o;

            if (!getInputType().equals(that.getInputType()))
            {
                return false;
            }
            return getOutputType().equals(that.getOutputType());
        }

        @Override
        public int hashCode()
        {
            int result = getInputType().hashCode();
            result = 31 * result + getOutputType().hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return "ExactTypedRegistryEntry{" +
                     "inputType=" + inputType +
                     ", outputType=" + outputType +
                     '}';
        }
    }
}
