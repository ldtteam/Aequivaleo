package com.ldtteam.aequivaleo.compound.data.serializers;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.information.datagen.CompoundInstanceRef;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.compound.information.datagen.CompoundInstanceData;

import java.lang.reflect.Type;
import java.util.Set;

public final class CompoundInstanceDataSerializer implements JsonSerializer<CompoundInstanceData>, JsonDeserializer<CompoundInstanceData>
{
    public static final Type HANDLED_TYPE = CompoundInstanceData.class;

    @Override
    public CompoundInstanceData deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
    {
        if (!json.isJsonObject())
            throw new JsonParseException("Can not deserialize compound instance data. Json is not an Object.");

        final JsonObject object = json.getAsJsonObject();
        final CompoundInstanceData.Mode mode = object.has("mode") ? context.deserialize(object.get("mode"), CompoundInstanceDataModeSerializer.HANDLED_TYPE) : CompoundInstanceData.Mode.ADDITIVE;
        final Set<ICompoundContainer<?>> container = context.deserialize(object.get("targets"), CompoundContainerSetSerializer.HANDLED_TYPE);
        final Set<CompoundInstanceRef> instances = context.deserialize(object.get("compounds"), CompoundInstanceRefSetSerializer.HANDLED_TYPE);

        return new CompoundInstanceData(
          mode,
          container,
          instances
        );
    }

    @Override
    public JsonElement serialize(final CompoundInstanceData src, final Type typeOfSrc, final JsonSerializationContext context)
    {
         final JsonObject result = new JsonObject();

         result.add("mode", context.serialize(src.getMode(), CompoundInstanceDataModeSerializer.HANDLED_TYPE));
         result.add("targets", context.serialize(src.getContainers(), CompoundContainerSetSerializer.HANDLED_TYPE));
         result.add("compounds", context.serialize(src.getCompoundInstances(), CompoundInstanceRefSetSerializer.HANDLED_TYPE));

         return result;
    }
}
