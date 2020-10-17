package com.ldtteam.aequivaleo.compound.data.serializers;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.information.datagen.CompoundInstanceData;

import java.lang.reflect.Type;

public final class CompoundInstanceDataModeSerializer implements JsonSerializer<CompoundInstanceData.Mode>, JsonDeserializer<CompoundInstanceData.Mode>
{
    public static final Type HANDLED_TYPE = CompoundInstanceData.Mode.class;

    @Override
    public CompoundInstanceData.Mode deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
    {
        if (json == null)
            return CompoundInstanceData.Mode.ADDITIVE;

        if (!json.isJsonPrimitive())
            return CompoundInstanceData.Mode.ADDITIVE;

        return CompoundInstanceData.Mode.valueOf(json.getAsString());
    }

    @Override
    public JsonElement serialize(final CompoundInstanceData.Mode src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        return new JsonPrimitive(src.name());
    }
}
