package com.ldtteam.aequivaleo.compound.data.serializers;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ldtteam.aequivaleo.api.compound.information.datagen.CompoundInstanceRef;

import java.lang.reflect.Type;
import java.util.Set;

public final class CompoundInstanceRefSetSerializer implements JsonSerializer<Set<CompoundInstanceRef>>, JsonDeserializer<Set<CompoundInstanceRef>>
{
    public static final Type HANDLED_TYPE = new TypeToken<Set<CompoundInstanceRef>>(){}.getType();

    @Override
    public Set<CompoundInstanceRef> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
    {
        if (!json.isJsonArray())
            throw new JsonParseException("For a Set<CompoundInstance> an array is required.");

        final JsonArray array = json.getAsJsonArray();
        final Set<CompoundInstanceRef> result = Sets.newHashSet();
        array.forEach(jsonElement -> {
            result.add(
              context.deserialize(jsonElement, CompoundInstanceRefSerializer.HANDLED_TYPE)
            );
        });
        return result;
    }

    @Override
    public JsonElement serialize(final Set<CompoundInstanceRef> src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        final JsonArray result = new JsonArray();
        src.forEach(instance -> {
            result.add(context.serialize(instance));
        });
        return result;
    }
}
