package com.ldtteam.aequivaleo.compound.container.registry;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.api.util.Suppression;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Predicate;

import static org.apache.commons.lang3.Validate.notNull;

public class CompoundContainerFactoryManager implements ICompoundContainerFactoryManager
{

    private static final CompoundContainerFactoryManager INSTANCE = new CompoundContainerFactoryManager();

    public static CompoundContainerFactoryManager getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Registry<ICompoundContainerType<?>> getRegistry()
    {
        return ModRegistries.CONTAINER_FACTORY;
    }

    private final LinkedList<ExactTypedRegistryEntry<?>> typedRegistryEntries = new LinkedList<>();

    private CompoundContainerFactoryManager()
    {
    }

    public void bake() {
        typedRegistryEntries.clear();
        for (final ICompoundContainerType<?> iCompoundContainerType : getRegistry())
        {
            typedRegistryEntries.add(
              new ExactTypedRegistryEntry<>(iCompoundContainerType.getCanHandlePredicate(), iCompoundContainerType)
            );
        }
    }

    /**
     * Utility method to check if a given class of a compound container can be wrapped properly.
     *
     * @param inputType The class to check
     * @param <T>   The type of the compound container to check.
     * @return True when a factory for type T is registered false when not.
     */
    @Override
    public <T> boolean canBeWrapped(@NotNull final Class<T> inputType)
    {
        notNull(inputType);
        return getFactoryFor(inputType).isPresent();
    }

    /**
     * Utility method to check if a given instance of a possible compound container can be wrapped properly.
     *
     * @param gameObject The instance to check
     * @param <T>       The type of the compound container to check.
     * @return True when a factory for type T is registered false when not.
     */
    @Override
    public <T> boolean canBeWrapped(@NotNull final T gameObject)
    {
        notNull(gameObject);
        return canBeWrapped(gameObject.getClass());
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
    @Override
    @NotNull
    public <T> ICompoundContainer<T> wrapInContainer(@NotNull final T gameObject, final double count) throws IllegalArgumentException
    {
        notNull(gameObject);
        return getFactoryFor(gameObject).map(factory -> factory.create(gameObject, count))
                 .orElseThrow(() -> new IllegalArgumentException("Unknown wrapping type: " + gameObject.getClass()));
    }

    @Override
    public @NotNull <T> ICompoundContainer<T> wrapInContainer(@NotNull T gameObject) throws IllegalArgumentException {
        notNull(gameObject);

        return getFactoryFor(gameObject)
                .map(factory -> factory.create(gameObject, factory.getInnateCount(gameObject)))
                .orElseThrow(() -> new IllegalArgumentException("Unknown wrapping type: " + gameObject.getClass()));
    }
    
    @Override
    public @NotNull <T> Optional<Double> getInnateCount(@NotNull T gameObject) {
        notNull(gameObject);
        
        return getFactoryFor(gameObject)
                       .map(factory -> factory.getInnateCount(gameObject));
    }
    
    /**
     * Internal method to get a factory of a given type.
     *
     * @param input The input to get the factory for.
     * @param <T>   The type to get the wrapping factory for.
     * @return An optional, possibly containing the requested factory if registered.
     */
    @NotNull
    @SuppressWarnings(Suppression.UNCHECKED)
    private <T> Optional<? extends ICompoundContainerType<T>> getFactoryFor(@NotNull final T input)
    {
        for (Iterator<ExactTypedRegistryEntry<?>> iterator = this.typedRegistryEntries.descendingIterator(); iterator.hasNext(); )
        {
            final ExactTypedRegistryEntry<?> e = iterator.next();
            if (e.canHandlePredicate().test(input))
            {
                ICompoundContainerType<?> f = e.factory();
                ICompoundContainerType<T> tiCompoundContainerType = (ICompoundContainerType<T>) f;
                return Optional.of(tiCompoundContainerType);
            }
        }
        return Optional.empty();
    }
    
    public void write(ICompoundContainer<?> key, FriendlyByteBuf buf) {
        buf.writeJsonWithCodec(ICompoundContainer.CODEC, key);
    }
    
    public ICompoundContainer<?> read(FriendlyByteBuf buf) {
        return buf.readJsonWithCodec(ICompoundContainer.CODEC);
    }
    
    private record ExactTypedRegistryEntry<T>(@NotNull Predicate<Object> canHandlePredicate,
                                              @NotNull ICompoundContainerType<T> factory) {
            private ExactTypedRegistryEntry(
                    @NotNull final Predicate<Object> canHandlePredicate, @NotNull final ICompoundContainerType<T> factory) {
                this.canHandlePredicate = canHandlePredicate;
                this.factory = factory;
            }
        }
}
