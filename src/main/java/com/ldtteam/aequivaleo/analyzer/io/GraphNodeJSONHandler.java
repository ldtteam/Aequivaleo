package com.ldtteam.aequivaleo.analyzer.io;

import com.google.gson.*;
import com.ldtteam.aequivaleo.analyzer.jgrapht.node.IAnalysisGraphNode;

import java.lang.reflect.Type;
import java.util.function.Function;

public class GraphNodeJSONHandler implements JsonSerializer<IAnalysisGraphNode>
{

    private final Function<IAnalysisGraphNode, String> idGenerator;

    public GraphNodeJSONHandler(final Function<IAnalysisGraphNode, String> idGenerator) {this.idGenerator = idGenerator;}

    @Override
    public JsonElement serialize(final IAnalysisGraphNode src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        final JsonObject data = new JsonObject();
        data.addProperty("id", this.idGenerator.apply(src));
        data.addProperty("displayName", src.toString());
        return data;
    }
}
