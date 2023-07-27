package com.ldtteam.aequivaleo.compound.data.serializers;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CompoundContainerSetSerializer implements JsonSerializer<Set<ICompoundContainer<?>>>, JsonDeserializer<Set<ICompoundContainer<?>>>
{
    public static final Type HANDLED_TYPE = new TypeToken<Set<ICompoundContainer<?>>>(){}.getType();

    @Override
    public Set<ICompoundContainer<?>> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
    {
        if (json.isJsonNull())
            return Collections.emptySet();

        if (json.isJsonObject()) {
            return ImmutableSet.of(context.deserialize(json, CompoundContainerFactoryManager.HANDLED_TYPE));
        }

        if (!json.isJsonArray())
            throw new JsonParseException("Compound container set needs to be an array, null or object.");

        final JsonArray entries = json.getAsJsonArray();
        return StreamSupport.stream(entries.spliterator(), false)
          .map(e -> context.deserialize(e, CompoundContainerFactoryManager.HANDLED_TYPE))
          .filter(ICompoundContainer.class::isInstance)
          .map(e -> (ICompoundContainer<?>) e)
          .collect(Collectors.toSet());
    }

    @Override
    public JsonElement serialize(final Set<ICompoundContainer<?>> src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        if (src.isEmpty())
            return JsonNull.INSTANCE;

        if (src.size() == 1)
            return context.serialize(src.iterator().next(), CompoundContainerFactoryManager.HANDLED_TYPE);

        final JsonArray data = new JsonArray();
        src.stream()
          .map(e -> context.serialize(e, CompoundContainerFactoryManager.HANDLED_TYPE))
          .sorted(Comparator.comparing(jsonElement -> {
              if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("type"))
                  return jsonElement.getAsJsonObject().get("type").getAsString();

              return jsonElement.toString();
          }))
          .forEach(data::add);

        return data;
    }
}
