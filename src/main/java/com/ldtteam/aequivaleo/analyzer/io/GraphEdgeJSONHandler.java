package com.ldtteam.aequivaleo.analyzer.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.ldtteam.aequivaleo.analyzer.jgrapht.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.IAnalysisGraphNode;
import org.jgrapht.Graph;

import java.lang.reflect.Type;
import java.util.function.Function;

public class GraphEdgeJSONHandler implements JsonSerializer<AccessibleWeightEdge>
{

    private final Function<IAnalysisGraphNode, String>            nodeIdGetter;
    private final Graph<IAnalysisGraphNode, AccessibleWeightEdge> graph;

    public GraphEdgeJSONHandler(
      final Function<IAnalysisGraphNode, String> nodeIdGetter,
      final Graph<IAnalysisGraphNode, AccessibleWeightEdge> graph) {
        this.nodeIdGetter = nodeIdGetter;
        this.graph = graph;
    }

    @Override
    public JsonElement serialize(final AccessibleWeightEdge src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        final IAnalysisGraphNode source = graph.getEdgeSource(src);
        final IAnalysisGraphNode target = graph.getEdgeTarget(src);

        final String sourceId = nodeIdGetter.apply(source);
        final String targetId = nodeIdGetter.apply(target);

        final JsonObject edgeData = new JsonObject();
        edgeData.addProperty("source", sourceId);
        edgeData.addProperty("target", targetId);
        edgeData.addProperty("weight", src.getWeight());
        return edgeData;
    }
}
