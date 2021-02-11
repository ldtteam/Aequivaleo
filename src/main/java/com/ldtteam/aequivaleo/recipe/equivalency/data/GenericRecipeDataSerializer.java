package com.ldtteam.aequivaleo.recipe.equivalency.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.data.GenericRecipeData;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.compound.data.serializers.CompoundContainerSetSerializer;
import com.ldtteam.aequivaleo.recipe.equivalency.ingredient.data.IngredientSetSerializer;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

public class GenericRecipeDataSerializer implements JsonSerializer<GenericRecipeData>, JsonDeserializer<GenericRecipeData>
{
    public static final Type HANDLED_TYPE = new TypeToken<GenericRecipeData>(){}.getType();

    @Override
    public GenericRecipeData deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
    {
        if (!json.isJsonObject())
            throw new JsonParseException("Recipe needs to be an object.");

        final JsonObject object = json.getAsJsonObject();
        if (!object.has("input"))
            throw new JsonParseException("Recipe needs to have inputs");

        if (!object.has("output"))
            throw new JsonParseException("Recipe needs to have outputs");

        final Set<IRecipeIngredient> inputs = context.deserialize(object.get("input"), IngredientSetSerializer.HANDLED_TYPE);
        final Set<ICompoundContainer<?>> requiredKnownOutputs = object.has("residue") ? context.deserialize(object.get("residue"), CompoundContainerSetSerializer.HANDLED_TYPE) : Collections
                                                                                                                                                                                    .emptySet();
        final Set<ICompoundContainer<?>> outputs = context.deserialize(object.get("output"), CompoundContainerSetSerializer.HANDLED_TYPE);

        return new GenericRecipeData(inputs, requiredKnownOutputs, outputs);
    }

    @Override
    public JsonElement serialize(final GenericRecipeData src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        final JsonObject object = new JsonObject();

        object.add("input", context.serialize(src.getInputs(), IngredientSetSerializer.HANDLED_TYPE));
        if (!src.getRequiredKnownOutputs().isEmpty())
            object.add("residue", context.serialize(src.getRequiredKnownOutputs(), CompoundContainerSetSerializer.HANDLED_TYPE));
        object.add("output", context.serialize(src.getOutputs(), CompoundContainerSetSerializer.HANDLED_TYPE));

        return object;
    }
}
