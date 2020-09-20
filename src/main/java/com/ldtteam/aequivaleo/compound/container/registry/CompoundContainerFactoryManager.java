package com.ldtteam.aequivaleo.compound.container.registry;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.api.util.Suppression;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static org.apache.commons.lang3.Validate.notNull;

public class CompoundContainerFactoryManager implements ICompoundContainerFactoryManager
{

    public static final Type HANDLED_TYPE = ICompoundContainer.class;

    private static final CompoundContainerFactoryManager INSTANCE = new CompoundContainerFactoryManager();

    public static CompoundContainerFactoryManager getInstance()
    {
        return INSTANCE;
    }

    @Override
    public IForgeRegistry<ICompoundContainerFactory<?>> getRegistry()
    {
        return ModRegistries.CONTAINER_FACTORY;
    }

    private final LinkedList<ExactTypedRegistryEntry<?>> typedRegistryEntries = new LinkedList<>();

    private CompoundContainerFactoryManager()
    {
    }

    public void bake() {
        typedRegistryEntries.clear();
        for (final ICompoundContainerFactory<?> iCompoundContainerFactory : getRegistry())
        {
            typedRegistryEntries.add(
              new ExactTypedRegistryEntry<>(iCompoundContainerFactory.getCanHandlePredicate(), iCompoundContainerFactory)
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
    @SuppressWarnings(Suppression.UNCHECKED)
    @NotNull
    public <T> ICompoundContainer<T> wrapInContainer(@NotNull final T gameObject, @NotNull final double count) throws IllegalArgumentException
    {
        notNull(gameObject);
        return getFactoryFor(gameObject).map(factory -> factory.create(gameObject, count))
                 .orElseThrow(() -> new IllegalArgumentException("Unknown wrapping type: " + gameObject.getClass()));
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
    private <T> Optional<? extends ICompoundContainerFactory<T>> getFactoryFor(@NotNull final T input)
    {
        for (Iterator<ExactTypedRegistryEntry<?>> iterator = this.typedRegistryEntries.descendingIterator(); iterator.hasNext(); )
        {
            final ExactTypedRegistryEntry<?> e = iterator.next();
            if (e.getCanHandlePredicate().test(input))
            {
                ICompoundContainerFactory<?> f = e.getFactory();
                ICompoundContainerFactory<T> tiCompoundContainerFactory = (ICompoundContainerFactory<T>) f;
                return Optional.of(tiCompoundContainerFactory);
            }
        }
        return Optional.empty();
    }

    @Override
    public ICompoundContainer<?> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
    {
        if (json.isJsonObject())
            throw new JsonParseException("The given container object is not a json object.");

        final JsonPrimitive typeElement = json.getAsJsonObject().getAsJsonPrimitive("type");
        final JsonElement dataElement = json.getAsJsonObject().get("data");

        final ResourceLocation typeName = new ResourceLocation(typeElement.getAsString());
        final ICompoundContainerFactory<?> containerFactory = getRegistry().getValue(typeName);
        if (containerFactory == null)
            throw new JsonParseException(String.format("The given container type is unknown: %s", typeName));

        final ICompoundContainer<?> container = containerFactory.deserialize(dataElement, typeOfT, context);
        return container;
    }

    @Override
    public JsonElement serialize(final ICompoundContainer<?> src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        return serializeInternally(src, typeOfSrc, context);
    }

    @SuppressWarnings(Suppression.UNCHECKED)
    private <T> JsonElement serializeInternally(final ICompoundContainer<T> src, final Type typeOfSrc, final JsonSerializationContext context) {
        final ICompoundContainerFactory<T> containerFactory = this.getFactoryFor(src.getContents()).orElseThrow(() -> new JsonParseException("The given container can not be serialized. Its contained type: " + src.getContents().getClass().getCanonicalName() + " has no registered factory."));

        final JsonObject resultData = new JsonObject();
        resultData.addProperty("type", Objects.requireNonNull(containerFactory.getRegistryName()).toString());
        resultData.add("data", containerFactory.serialize(src, ICompoundContainer.class, context));

        return resultData;
    }

    @Override
    public void write(final ICompoundContainer<?> object, final PacketBuffer buffer)
    {
        writeInternally(object, buffer);
    }

    @SuppressWarnings(Suppression.UNCHECKED)
    private <T> void writeInternally(final ICompoundContainer<T> object, final PacketBuffer buffer)
    {
        final ICompoundContainerFactory<T> containerFactory = this.getFactoryFor(object.getContents()).orElseThrow(() -> new JsonParseException("The given container can not be serialized. Its contained type: " + object.getContents().getClass().getCanonicalName() + " has no registered factory."));

        final ForgeRegistry<ICompoundContainerFactory<?>> internalRegistry = (ForgeRegistry<ICompoundContainerFactory<?>>) getRegistry();

        buffer.writeVarInt(internalRegistry.getID(containerFactory));
        containerFactory.write(object, buffer);
    }

    @Override
    public ICompoundContainer<?> read(final PacketBuffer buffer)
    {
        final ForgeRegistry<ICompoundContainerFactory<?>> internalRegistry = (ForgeRegistry<ICompoundContainerFactory<?>>) getRegistry();
        return internalRegistry.getValue(buffer.readVarInt()).read(buffer);
    }

    private static class ExactTypedRegistryEntry<T>
    {
        @NotNull
        private final Predicate<Object> canHandlePredicate;

        @NotNull
        private final ICompoundContainerFactory<T> factory;

        private ExactTypedRegistryEntry(
          @NotNull final Predicate<Object> canHandlePredicate, @NotNull final ICompoundContainerFactory<T> factory)
        {
            this.canHandlePredicate = canHandlePredicate;
            this.factory = factory;
        }

        @NotNull
        public Predicate<Object> getCanHandlePredicate()
        {
            return canHandlePredicate;
        }

        @NotNull
        ICompoundContainerFactory<T> getFactory()
        {
            return factory;
        }
    }
}
