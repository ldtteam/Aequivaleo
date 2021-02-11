package com.ldtteam.aequivaleo.recipe.equivalency.ingredient.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data.IIngredientSerializer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data.IIngredientSerializerRegistry;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class IngredientSerializerRegistry implements IIngredientSerializerRegistry, JsonDeserializer<IRecipeIngredient>, JsonSerializer<IRecipeIngredient>
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final IngredientSerializerRegistry INSTANCE = new IngredientSerializerRegistry();
    public static final Type HANDLED_TYPE = new TypeToken<IRecipeIngredient>(){}.getType();

    private final Map<ResourceLocation, IIngredientSerializer<?>> idToSerializerMap    = new ConcurrentHashMap<>();
    private final List<IngredientSerializerEntry<?, ?>>           typeToSerializerList = new CopyOnWriteArrayList<>();

    private static final IngredientSerializerEntry<?, ?> DEFAULT_ENTRY = new IngredientSerializerEntry<>(SimpleIngredientSerializer.getInstance());

    public static IngredientSerializerRegistry getInstance()
    {
        return INSTANCE;
    }

    private IngredientSerializerRegistry()
    {
        this.register(SimpleIngredientSerializer.getInstance());
        this.register(TagIngredientSerializer.getInstance());
    }

    @Override
    public <S extends IIngredientSerializer<I>, I extends IRecipeIngredient> IIngredientSerializerRegistry register(
      final S serializer)
    {
        if (this.idToSerializerMap.putIfAbsent(serializer.getId(), serializer) != null)
        {
            throw new IllegalArgumentException("The given id for the serializer: " + serializer.getId() + " is already in use!");
        }

        synchronized (this.typeToSerializerList)
        {
            if (this.typeToSerializerList.stream().anyMatch(ingredientSerializerEntry -> ingredientSerializerEntry.serializer.getIngredientType().equals(serializer.getIngredientType())))
            {
                throw new IllegalArgumentException("The given input type for the serializer is already in use: " + serializer.getIngredientType().getName());
            }

            this.typeToSerializerList.add(new IngredientSerializerEntry<>(serializer));
        }

        return this;
    }

    @Override
    public IRecipeIngredient deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
    {
        if (json.isJsonObject()) {
            final JsonObject object = json.getAsJsonObject();
            if (object.has("type") && object.get("type").isJsonPrimitive()) {
                //With a type flag we consider this a switchable target.
                final String type = object.get("type").getAsString();
                if (type.equals("tag"))
                {
                    final IIngredientSerializer<?> innerSerializer = idToSerializerMap.getOrDefault(
                      new ResourceLocation(type),
                      SimpleIngredientSerializer.getInstance()
                    );

                    return innerSerializer.deserialize(object, typeOfT, context);
                }
            }
        }

        return SimpleIngredientSerializer.getInstance().deserializeElement(json, typeOfT, context);
    }

    @Override
    public JsonElement serialize(final IRecipeIngredient src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        //Special case simple ingredients, since they short cirquit the whole shabang anyway.
        if (src instanceof SimpleIngredient)
            return SimpleIngredientSerializer.getInstance().serializeIngredient(src, typeOfSrc, context);

        final Class<? extends IRecipeIngredient> ingredientClass = src.getClass();
        final IngredientSerializerEntry<?,?> serializer = typeToSerializerList.stream().filter(e -> e.serializer.getIngredientType().equals(ingredientClass)).findFirst()
          .orElse(DEFAULT_ENTRY);

        //Check for the default entry.
        if (serializer.serializer instanceof SimpleIngredientSerializer)
        {
            LOGGER.error("Missing ingredient serializer for type: " + ingredientClass.getName());
            LOGGER.error("Serializing using default simple ingredient. This might not work when loaded again since this hardcodes containers.");
            //And serialize raw.
            return SimpleIngredientSerializer.getInstance().serializeIngredient(src, typeOfSrc, context);
        }

        return serializer.serializeIngredient(src, typeOfSrc, context);
    }

    private static class IngredientSerializerEntry<S extends IIngredientSerializer<I>, I extends IRecipeIngredient>
    {
        private final S serializer;

        private IngredientSerializerEntry(final S serializer) {this.serializer = serializer;}

        public JsonElement serializeIngredient(final IRecipeIngredient src, final Type typeOfSrc, final JsonSerializationContext context)
        {
            final I ingredient = serializer.getIngredientType().cast(src);
            return serializer.serialize(ingredient, typeOfSrc, context);
        }
    }

}
