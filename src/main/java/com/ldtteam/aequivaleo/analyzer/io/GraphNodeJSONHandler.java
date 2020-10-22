package com.ldtteam.aequivaleo.analyzer.io;

import com.google.gson.*;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisGraphNode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Function;

public class GraphNodeJSONHandler implements JsonSerializer<INode>
{
    private final Function<INode, String> idGenerator;

    public GraphNodeJSONHandler(final Function<INode, String> idGenerator) {this.idGenerator = idGenerator;}

    @Override
    public JsonElement serialize(final INode src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        final JsonObject data = new JsonObject();
        data.addProperty("id", this.idGenerator.apply(src));
        data.addProperty("displayName", src.toString());
        return data;
    }
}
