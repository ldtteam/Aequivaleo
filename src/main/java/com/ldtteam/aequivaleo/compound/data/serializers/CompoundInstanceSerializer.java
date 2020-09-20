package com.ldtteam.aequivaleo.compound.data.serializers;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;

public final class CompoundInstanceSerializer implements JsonSerializer<CompoundInstance>, JsonDeserializer<CompoundInstance>
{

    public static final Type HANDLED_TYPE = CompoundInstance.class;

    @Override
    public CompoundInstance deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
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

        return new CompoundInstance(
          ModRegistries.COMPOUND_TYPE.getValue(location),
          count
        );
    }

    @Override
    public JsonElement serialize(final CompoundInstance src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        final JsonObject object = new JsonObject();
        object.addProperty("amount", src.getAmount());
        object.add("type", context.serialize(src.getType().getRegistryName()));

        return object;
    }
}
