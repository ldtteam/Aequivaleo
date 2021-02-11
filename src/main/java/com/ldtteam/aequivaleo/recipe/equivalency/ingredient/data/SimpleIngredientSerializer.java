package com.ldtteam.aequivaleo.recipe.equivalency.ingredient.data;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.data.serializers.CompoundContainerSetSerializer;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.SortedSet;

public class SimpleIngredientSerializer implements JsonSerializer<IRecipeIngredient>, JsonDeserializer<IRecipeIngredient>
{
    public static final Type HANDLED_TYPE = new TypeToken<SimpleIngredient>(){}.getType();

    @Override
    public IRecipeIngredient deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
    {
        final SortedSet<ICompoundContainer<?>> containers = Sets.newTreeSet();
        if (json.isJsonArray()) {
            containers.addAll(context.deserialize(json, CompoundContainerSetSerializer.HANDLED_TYPE));
        } else if (json.isJsonObject()) {
            containers.add(context.deserialize(json, CompoundContainerFactoryManager.HANDLED_TYPE));
        } else //noinspection StatementWithEmptyBody
            if (json.isJsonNull()) {
            //NOOP Empty ingredient.
        }
        else {
            throw new JsonParseException("Ingredient needs to be either an array of object of compound containers or an object of compound container");
        }

        final SimpleIngredientBuilder builder = new SimpleIngredientBuilder();
        builder.from(containers);
        return builder.createIngredient();
    }

    @Override
    public JsonElement serialize(final IRecipeIngredient src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        if (src.getCandidates().size() == 0)
            return JsonNull.INSTANCE;

        if (src.getCandidates().size() == 1) {
            return context.serialize(src.getCandidates().first(), CompoundContainerFactoryManager.HANDLED_TYPE);
        }

        return context.serialize(src.getCandidates(), CompoundContainerSetSerializer.HANDLED_TYPE);
    }
}
