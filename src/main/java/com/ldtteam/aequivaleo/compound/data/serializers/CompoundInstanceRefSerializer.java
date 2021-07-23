package com.ldtteam.aequivaleo.compound.data.serializers;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.information.datagen.data.CompoundInstanceRef;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;

public final class CompoundInstanceRefSerializer implements JsonSerializer<CompoundInstanceRef>, JsonDeserializer<CompoundInstanceRef>
{

    public static final Type HANDLED_TYPE = CompoundInstanceRef.class;

    @Override
    public CompoundInstanceRef deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
    {
        if (!json.isJsonObject())
        {
            throw new JsonParseException("CompoundInstance requires a JsonObject.");
        }

        final JsonObject object = json.getAsJsonObject();
        final ResourceLocation location = context.deserialize(object.get("type"), ResourceLocation.class);
        final Double count = object.getAsJsonPrimitive("amount").getAsDouble();

        if (!ModRegistries.COMPOUND_TYPE.containsKey(location))
        {
            return null;
        }

        return new CompoundInstanceRef(
          location,
          count
        );
    }

    @Override
    public JsonElement serialize(final CompoundInstanceRef src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        final JsonObject object = new JsonObject();
        object.addProperty("amount", src.getAmount());
        object.add("type", context.serialize(src.getType()));

        return object;
    }
}
