package com.ldtteam.aequivaleo.compound.data.serializers;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.information.datagen.data.CompoundInstanceRef;
import com.ldtteam.aequivaleo.api.compound.information.datagen.data.CompoundInstanceData;
import net.minecraft.server.ReloadableServerResources;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Set;

public final class CompoundInstanceDataSerializer implements JsonSerializer<CompoundInstanceData>, JsonDeserializer<CompoundInstanceData>
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Type HANDLED_TYPE = CompoundInstanceData.class;

    private final ICondition.IContext context;

    public CompoundInstanceDataSerializer(final ICondition.IContext context) {
        this.context = context;
    }

    @Override
    public CompoundInstanceData deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
    {
        if (!json.isJsonObject())
            throw new JsonParseException("Can not deserialize compound instance data. Json is not an Object.");

        final JsonObject object = json.getAsJsonObject();

        if (!CraftingHelper.processConditions(object, "conditions", this.context)) {
            return CompoundInstanceData.DISABLED;
        }

        final CompoundInstanceData.Mode mode = object.has("mode") ? context.deserialize(object.get("mode"), CompoundInstanceDataModeSerializer.HANDLED_TYPE) : CompoundInstanceData.Mode.ADDITIVE;
        Set<ICompoundContainer<?>> container = context.deserialize(object.get("targets"), CompoundContainerSetSerializer.HANDLED_TYPE);
        final Set<CompoundInstanceRef> instances = context.deserialize(object.get("compounds"), CompoundInstanceRefSetSerializer.HANDLED_TYPE);

        if (container == null) {
            throw new JsonParseException("Container data not found");
        }
        final JsonArray conditionsArray = object.has("conditions") ? object.get("conditions").getAsJsonArray() : new JsonArray();
        final Set<ICondition> conditions = Sets.newHashSet();
        for (int i = 0; i < conditionsArray.size(); i++)
        {
            final JsonElement conditionElement = conditionsArray.get(i);
            final JsonObject conditionObject = conditionElement.getAsJsonObject();

            conditions.add(CraftingHelper.getCondition(conditionObject));
        }

        return new CompoundInstanceData(
          mode,
          container,
          instances,
          conditions);
    }

    @Override
    public JsonElement serialize(final CompoundInstanceData src, final Type typeOfSrc, final JsonSerializationContext context)
    {
         final JsonObject result = new JsonObject();

         result.add("mode", context.serialize(src.getMode(), CompoundInstanceDataModeSerializer.HANDLED_TYPE));
         result.add("targets", context.serialize(src.getContainers(), CompoundContainerSetSerializer.HANDLED_TYPE));
         result.add("compounds", context.serialize(src.getCompoundInstances(), CompoundInstanceRefSetSerializer.HANDLED_TYPE));

        if (!src.getConditions().isEmpty()) {
            final JsonArray conditions = new JsonArray();
            src.getConditions().stream()
              .sorted(Comparator.comparing(ICondition::getID))
              .map(CraftingHelper::serialize)
              .forEach(conditions::add);

            result.add("conditions", conditions);
        }

         return result;
    }
}
