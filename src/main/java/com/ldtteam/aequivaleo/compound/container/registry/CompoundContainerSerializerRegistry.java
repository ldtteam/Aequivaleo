package com.ldtteam.aequivaleo.compound.container.registry;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.dummy.Dummy;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerSerializerRegistry;
import com.ldtteam.aequivaleo.api.compound.container.serialization.ICompoundContainerSerializer;
import com.ldtteam.aequivaleo.api.util.CompositeType;
import com.ldtteam.aequivaleo.api.util.Configuration;
import com.ldtteam.aequivaleo.api.util.AequivaleoLogger;
import com.ldtteam.aequivaleo.api.util.Suppression;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;

import static org.apache.commons.lang3.Validate.notNull;

public class CompoundContainerSerializerRegistry implements ICompoundContainerSerializerRegistry
{
    private static final CompoundContainerSerializerRegistry INSTANCE = new CompoundContainerSerializerRegistry();

    public static CompoundContainerSerializerRegistry getInstance()
    {
        return INSTANCE;
    }

    private final Set<ExactTypedRegistryEntry<?>> typedRegistryEntries = Sets.newConcurrentHashSet();

    private CompoundContainerSerializerRegistry()
    {
    }

    @Override
    public <T> ICompoundContainerSerializerRegistry register(@NotNull final ICompoundContainerSerializer<T> serializer)
    {
        this.typedRegistryEntries.add(new ExactTypedRegistryEntry<T>(serializer.getType(), serializer));
        return this;
    }

    /**
     * Internal method to get a serializer of a given type.
     *
     * @param input The class of the type as input to get the serializer for.
     * @param <T>   The type to get the wrapping serializer for.
     * @return An optional, possibly containing the requested serializer if registered.
     */
    @NotNull
    @SuppressWarnings(Suppression.UNCHECKED)
    private <T> Optional<? extends ICompoundContainerSerializer<T>> getSerializerForType(@NotNull final Class<T> input)
    {
        notNull(input);
        return this.typedRegistryEntries.stream()
                 .filter(e -> e.getType() == input)
                 .map(CompoundContainerSerializerRegistry.ExactTypedRegistryEntry::getSerializer)
                 .map(f -> (ICompoundContainerSerializer<T>) f)
                 .findFirst();
    }

    /**
     * Returns a JSON handler that is capable of reading and writing, all instances to disk.
     * Both collections as well as individual instances are supported.
     *
     * @return The JSON handler.
     */
    @NotNull
    public Gson getJsonHandler()
    {
        final GsonBuilder builder = new GsonBuilder()
          .setLenient()
          .registerTypeAdapter(ICompoundContainer.class, new JSONWrapperHandler(this));

        if (Configuration.persistence.prettyPrint)
            builder.setPrettyPrinting();

        return builder.create();
    }

    private final class JSONWrapperHandler implements JsonSerializer<ICompoundContainer<?>>, JsonDeserializer<ICompoundContainer<?>>
    {

        private final CompoundContainerSerializerRegistry registry;

        private JSONWrapperHandler(final CompoundContainerSerializerRegistry registry) {this.registry = registry;}

        /**
         * Gson invokes this call-back method during deserialization when it encounters a field of the
         * specified type.
         * <p>In the implementation of this call-back method, you should consider invoking
         * {@link JsonDeserializationContext#deserialize(JsonElement, Type)} method to create objects
         * for any non-trivial field of the returned object. However, you should never invoke it on the
         * the same type passing {@code json} since that will cause an infinite loop (Gson will call your
         * call-back method again).
         *
         * @param json    The Json data being deserialized
         * @param typeOfT The type of the Object to deserialize to
         * @return a deserialized object of the specified type typeOfT which is a subclass of {@code T}
         *
         * @throws JsonParseException if json is not in the expected format of {@code typeofT}
         */
        @Override
        public ICompoundContainer<?> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
        {
            if (!json.isJsonObject())
                throw new IllegalArgumentException("Json is not an object");

            final JsonObject jsonObject = (JsonObject) json;
            if (!jsonObject.has("type"))
                throw new IllegalArgumentException("Json does not contain wrapper type");
            if (!jsonObject.has("data"))
                throw new IllegalArgumentException("Json does not contain wrapper data");

            final Class<?> clz;
            try
            {
                 clz = Class.forName(jsonObject.get("type").getAsString());
            }
            catch (ClassNotFoundException e)
            {
                AequivaleoLogger.bigWarningSimple("Unknown class: %s. Maybe save types have changed. Storing in dummy.", jsonObject.get("type"));
                return new Dummy(jsonObject);
            }

            final ICompoundContainerSerializer<?> factory = registry.getSerializerForType(clz).orElse(null);
            if (factory == null)
            {
                AequivaleoLogger.bigWarningWithStackTrace("Unknown factory: %s. No one registered a factory. Storing in dummy.", jsonObject.get("type"));
                return new Dummy(jsonObject);
            }

            final CompositeType compositeType = new CompositeType(ICompoundContainer.class, clz);
            final Gson partSerializer = buildJsonForClass(compositeType, factory);

            return partSerializer.fromJson(jsonObject.get("data"), compositeType);
        }

        /**
         * Gson invokes this call-back method during serialization when it encounters a field of the
         * specified type.
         *
         * <p>In the implementation of this call-back method, you should consider invoking
         * {@link JsonSerializationContext#serialize(Object, Type)} method to create JsonElements for any
         * non-trivial field of the {@code src} object. However, you should never invoke it on the
         * {@code src} object itself since that will cause an infinite loop (Gson will call your
         * call-back method again).</p>
         *
         * @param src       the object that needs to be converted to Json.
         * @param typeOfSrc the actual type (fully genericized version) of the source object.
         * @return a JsonElement corresponding to the specified object.
         */
        @Override
        public JsonElement serialize(final ICompoundContainer<?> src, final Type typeOfSrc, final JsonSerializationContext context)
        {
            //In case of old dummy data. We just return the original data.
            if (src instanceof Dummy)
                return ((Dummy) src).getOriginalData();

            Validate.notNull(src.getContents());
            final Class<?> clz = src.getContents().getClass();
            final CompositeType compositeType = new CompositeType(ICompoundContainer.class, clz);
            final ICompoundContainerSerializer<?> factory = registry.getSerializerForType(clz).orElseThrow(() -> new IllegalArgumentException(String.format(
              "No known factory for type: %s",
              clz.getName())));

            final Gson partSerializer = buildJsonForClass(compositeType, factory);

            final JsonElement data = partSerializer.toJsonTree(src, compositeType);

            final JsonObject object = new JsonObject();
            object.addProperty("type", clz.getName());
            object.add("data", data);
            return object;
        }

        /**
         * Builds a serializer for a given composite type and factory.
         *
         * @param tClass The composite type containing the correct type information for the wrapper and its generic type.
         * @param jsonHandler The factory that functions as json handler.
         *
         * @return The json handler.
         */
        private Gson buildJsonForClass(@NotNull final CompositeType tClass, @NotNull final ICompoundContainerSerializer<?> jsonHandler)
        {
            final GsonBuilder builder = new GsonBuilder()
              .registerTypeAdapter(tClass, jsonHandler)
              .setLenient();

            if (Configuration.persistence.prettyPrint)
            {
                builder.setPrettyPrinting();
            }

            return builder.create();
        }
    }

    private static final class ExactTypedRegistryEntry<T>
    {
        @NotNull
        private final Class<T> type;

        @NotNull
        private final ICompoundContainerSerializer<T> serializer;

        private ExactTypedRegistryEntry(@NotNull final Class<T> type, @NotNull final ICompoundContainerSerializer<T> serializer) {
            this.type = type;
            this.serializer = serializer;
        }

        @NotNull
        public Class<T> getType()
        {
            return type;
        }

        @NotNull
        public ICompoundContainerSerializer<T> getSerializer()
        {
            return serializer;
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

            final ExactTypedRegistryEntry<?> that = (ExactTypedRegistryEntry<?>) o;

            return getType().equals(that.getType());
        }

        @Override
        public int hashCode()
        {
            return getType().hashCode();
        }

        @Override
        public String toString()
        {
            return "ExactTypedRegistryEntry{" +
                     "type=" + type +
                     '}';
        }
    }
}
