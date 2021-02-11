package com.ldtteam.aequivaleo.recipe.equivalency.ingredient.data;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data.IIngredientSerializer;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.data.serializers.CompoundContainerSetSerializer;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.SortedSet;

public class SimpleIngredientSerializer implements IIngredientSerializer<SimpleIngredient>
{
    private static final SimpleIngredientSerializer INSTANCE = new SimpleIngredientSerializer();

    public static SimpleIngredientSerializer getInstance()
    {
        return INSTANCE;
    }

    private SimpleIngredientSerializer()
    {
    }

    public static final Type HANDLED_TYPE = new TypeToken<SimpleIngredient>(){}.getType();

    @Override
    public ResourceLocation getId()
    {
        return Constants.SIMPLE_INGREDIENT;
    }

    @Override
    public Class<SimpleIngredient> getIngredientType()
    {
        return SimpleIngredient.class;
    }

    @Override
    public SimpleIngredient deserialize(final JsonObject json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
    {
        return deserializeElement(json, typeOfT, context);
    }

    @Override
    public JsonObject serialize(final SimpleIngredient src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        if (src.getCandidates().size() == 1) {
            //Always an object since it is just one.
            return (JsonObject) context.serialize(src.getCandidates().first(), CompoundContainerFactoryManager.HANDLED_TYPE);
        }

        throw new IllegalArgumentException("The given ingredient has: " + src.getCandidates().size() + " but only 1 is supported when serializing directly to an object!");
    }

    public SimpleIngredient deserializeElement(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
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
        return builder.createSimpleIngredient();
    }

    public JsonElement serializeElement(final SimpleIngredient src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        return this.serializeIngredient(src, typeOfSrc, context);
    }

    JsonElement serializeIngredient(final IRecipeIngredient src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        if (src.getCandidates().size() == 0)
            return JsonNull.INSTANCE;

        if (src.getCandidates().size() == 1) {
            return context.serialize(src.getCandidates().first(), CompoundContainerFactoryManager.HANDLED_TYPE);
        }

        return context.serialize(src.getCandidates(), CompoundContainerSetSerializer.HANDLED_TYPE);
    }
}