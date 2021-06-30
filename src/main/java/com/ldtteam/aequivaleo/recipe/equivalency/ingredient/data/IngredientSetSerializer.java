package com.ldtteam.aequivaleo.recipe.equivalency.ingredient.data;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import org.lwjgl.system.CallbackI;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class IngredientSetSerializer implements JsonSerializer<Set<IRecipeIngredient>>, JsonDeserializer<Set<IRecipeIngredient>>
{
    public static final Type HANDLED_TYPE = new TypeToken<Set<IRecipeIngredient>>(){}.getType();

    @Override
    public Set<IRecipeIngredient> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
    {
        final Set<IRecipeIngredient> ingredients = Sets.newHashSet();

        if (json.isJsonNull())
            return ingredients;

        if (json.isJsonObject()) {
            final JsonObject object = json.getAsJsonObject();
            ingredients.add(context.deserialize(object, IngredientSerializerRegistry.HANDLED_TYPE));
            return ingredients;
        }

        if (!json.isJsonArray())
            throw new JsonParseException("Recipe ingredient set needs to be null, object or array");

        final JsonArray entries = json.getAsJsonArray();
        StreamSupport.stream(entries.spliterator(), false)
                 .map(e -> context.deserialize(e, IngredientSerializerRegistry.HANDLED_TYPE))
                 .filter(IRecipeIngredient.class::isInstance)
                 .map(e -> (IRecipeIngredient) e)
                 .forEach(ingredients::add);
        return ingredients;
    }

    @Override
    public JsonElement serialize(final Set<IRecipeIngredient> src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        if (src.isEmpty())
            return JsonNull.INSTANCE;

        if (src.size() == 1) {
            return context.serialize(src.iterator().next(), IngredientSerializerRegistry.HANDLED_TYPE);
        }

        final JsonArray elements = new JsonArray();
        src.stream()
          .map(e -> context.serialize(e, IngredientSerializerRegistry.HANDLED_TYPE))
          .sorted(Comparator.comparing(JsonElement::toString))
          .forEach(elements::add);
        return elements;
    }
}
