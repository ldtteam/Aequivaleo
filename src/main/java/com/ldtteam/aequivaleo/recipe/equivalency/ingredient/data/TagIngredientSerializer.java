package com.ldtteam.aequivaleo.recipe.equivalency.ingredient.data;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.TagIngredient;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.data.serializers.CompoundContainerSetSerializer;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.SortedSet;

public class TagIngredientSerializer implements JsonSerializer<IRecipeIngredient>, JsonDeserializer<IRecipeIngredient>
{
    public static final Type HANDLED_TYPE = new TypeToken<TagIngredient>(){}.getType();

    @Override
    public IRecipeIngredient deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
    {
        final SortedSet<ICompoundContainer<?>> containers = Sets.newTreeSet();
        if (!json.isJsonObject()) {
            throw new JsonParseException("Ingredient needs to be either an object to be target the tag.");
        }

        final ResourceLocation tagType = new ResourceLocation(json.getAsJsonObject().get("type").getAsString());
        final ResourceLocation tagName = new ResourceLocation(json.getAsJsonObject().get("name").getAsString());
        final double count = json.getAsJsonObject().get("amount").getAsDouble();

        return new TagIngredient(tagType, tagName, count);
    }

    @Override
    public JsonElement serialize(final IRecipeIngredient src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        final TagIngredient ingredient = (TagIngredient) src;

        final JsonObject object = new JsonObject();
        object.add("type", context.serialize(ingredient.getTagType()));
        object.add("name", context.serialize(ingredient.getTagName()));
        object.addProperty("amount", ingredient.getRequiredCount());
        return object;
    }
}
