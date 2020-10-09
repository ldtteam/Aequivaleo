package com.ldtteam.aequivaleo.analyzer.io;

import com.google.gson.*;
import com.ldtteam.aequivaleo.analyzer.jgrapht.node.IAnalysisGraphNode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Function;

public class GraphNodeJSONHandler implements JsonSerializer<IAnalysisGraphNode<Set<CompoundInstance>>>
{

    private final Function<IAnalysisGraphNode<Set<CompoundInstance>>, String> idGenerator;

    public GraphNodeJSONHandler(final Function<IAnalysisGraphNode<Set<CompoundInstance>>, String> idGenerator) {this.idGenerator = idGenerator;}

    @Override
    public JsonElement serialize(final IAnalysisGraphNode<Set<CompoundInstance>> src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        final JsonObject data = new JsonObject();
        data.addProperty("id", this.idGenerator.apply(src));
        data.addProperty("displayName", src.toString());
        return data;
    }
}
