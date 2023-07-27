package com.ldtteam.aequivaleo.recipe.equivalency.ingredient.data;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.TagIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data.IIngredientSerializer;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;

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
        object.add("type", context.serialize(src.getRegistryName()));
        object.add("name", context.serialize(src.getTagName()));
        object.addProperty("amount", src.getRequiredCount());
        return object;
    }
}
