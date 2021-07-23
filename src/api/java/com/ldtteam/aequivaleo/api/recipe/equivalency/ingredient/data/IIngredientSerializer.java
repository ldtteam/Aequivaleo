package com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredient;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;

/**
 * Interfaces that describe a serializer for recipe ingredients loaded from datapacks.
 * @param <I> The type of ingredient.
 */
public interface IIngredientSerializer<I extends IRecipeIngredient>
{
    /**
     * The id of the serializer.
     * Used to determine what deserializer to invoke during deserialization.
     *
     * @return The id.
     */
    ResourceLocation getId();

    /**
     * The type of the ingredient to serialize.
     * Used during serialization to determine what serializer to invoke.
     *
     * @return The ingredient type.
     */
    Class<I> getIngredientType();

    /**
     * Deserializer callback used to turn the json object into an instance of the ingredient.
     *
     * @param json The json.
     * @param typeOfT The requested type. In general this will be {@link IRecipeIngredient}, not the type represented by {@link I}.
     * @param context The deserialization context.
     * @return The instance of the ingredient that is stored in the json structure.
     * @throws JsonParseException Thrown when parsing fails.
     */
    I deserialize(final JsonObject json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException;

    /**
     * Serialization callback used to turn the instance of the ingredient into a json object.
     *
     * @param src The ingredient.
     * @param typeOfSrc The requested type. In general this will be {@link IRecipeIngredient}, not the type represented by {@link I}.
     * @param context The serialization context.
     *
     * @return The json object that represents the ingredient.
     */
    JsonObject serialize(final I src, final Type typeOfSrc, final JsonSerializationContext context);
}
