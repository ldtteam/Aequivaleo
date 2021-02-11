package com.ldtteam.aequivaleo.recipe.equivalency.ingredient.data;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.TagIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data.IIngredientSerializer;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.data.serializers.CompoundContainerSetSerializer;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.SortedSet;

public class TagIngredientSerializer implements IIngredientSerializer<TagIngredient>
{
    private static final TagIngredientSerializer INSTANCE = new TagIngredientSerializer();

    public static TagIngredientSerializer getInstance()
    {
        return INSTANCE;
    }

    private TagIngredientSerializer()
    {
    }

    @Override
    public ResourceLocation getId()
    {
        return Constants.TAG_INGREDIENT;
    }

    @Override
    public Class<TagIngredient> getIngredientType()
    {
        return TagIngredient.class;
    }

    @Override
    public TagIngredient deserialize(final JsonObject json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
    {
        final ResourceLocation tagType = new ResourceLocation(json.getAsJsonObject().get("type").getAsString());
        final ResourceLocation tagName = new ResourceLocation(json.getAsJsonObject().get("name").getAsString());
        final double count = json.getAsJsonObject().get("amount").getAsDouble();

        return new TagIngredient(tagType, tagName, count);
    }

    @Override
    public JsonObject serialize(final TagIngredient src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        final JsonObject object = new JsonObject();
        object.add("type", context.serialize(src.getTagType()));
        object.add("name", context.serialize(src.getTagName()));
        object.addProperty("amount", src.getRequiredCount());
        return object;
    }
}
