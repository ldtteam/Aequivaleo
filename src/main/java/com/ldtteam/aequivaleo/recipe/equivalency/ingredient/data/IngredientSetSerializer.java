package com.ldtteam.aequivaleo.recipe.equivalency.ingredient.data;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import org.lwjgl.system.CallbackI;

import java.lang.reflect.Type;
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
            if (object.has("type") && object.get("type").isJsonPrimitive() && object.has("data")) {
                //With a type flag we consider this a switchable target.
                final String type = object.get("type").getAsString();
                final JsonElement dataElement = object.get("data");
                if (type.equals("tag")) {
                    ingredients.add(context.deserialize(dataElement, TagIngredientSerializer.HANDLED_TYPE));
                } else {
                    ingredients.add(context.deserialize(dataElement, SimpleIngredientSerializer.HANDLED_TYPE));
                }
            } else {
                ingredients.add(context.deserialize(json, SimpleIngredientSerializer.HANDLED_TYPE));
            }
            return ingredients;
        }

        if (!json.isJsonArray())
            throw new JsonParseException("Recipe ingredient set needs to be null, object or array");

        final JsonArray entries = json.getAsJsonArray();
        StreamSupport.stream(entries.spliterator(), false)
                 .map(e -> {
                     if (e.isJsonObject()) {
                         final JsonObject object = e.getAsJsonObject();
                         if (object.has("type") && object.get("type").isJsonPrimitive() && object.has("data"))
                         {
                             //With a type flag we consider this a switchable target.
                             final String type = object.get("type").getAsString();
                             final JsonElement dataElement = object.get("data");
                             if (type.equals("tag"))
                             {
                                 return context.deserialize(dataElement, TagIngredientSerializer.HANDLED_TYPE);
                             }
                             else
                             {
                                 return context.deserialize(dataElement, SimpleIngredientSerializer.HANDLED_TYPE);
                             }
                         }
                     }

                     return context.deserialize(e, SimpleIngredientSerializer.HANDLED_TYPE);
                 })
                 .filter(IRecipeIngredient.class::isInstance)
                 .map(e -> (IRecipeIngredient) e)
                 .forEach(ingredients::add);
        return ingredients;
    }

    @Override
    public JsonElement serialize(final Set<IRecipeIngredient> src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        //TODO: Patch writer!
        if (src.isEmpty())
            return JsonNull.INSTANCE;

        if (src.size() == 1) {
            return context.serialize(src.iterator().next(), SimpleIngredientSerializer.HANDLED_TYPE);
        }

        final JsonArray elements = new JsonArray();
        src.stream().map(e -> context.serialize(e, SimpleIngredientSerializer.HANDLED_TYPE)).forEach(elements::add);
        return elements;
    }
}
