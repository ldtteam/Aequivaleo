package com.ldtteam.aequivaleo.analysis.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import org.jgrapht.Graph;

import java.lang.reflect.Type;
import java.util.function.Function;

public class GraphEdgeJSONHandler implements JsonSerializer<IEdge>
{

    private final Function<INode, String>                                                nodeIdGetter;
    private final Graph<INode, IEdge> graph;

    public GraphEdgeJSONHandler(
      final Function<INode, String> nodeIdGetter,
      final Graph<INode, IEdge> graph) {
        this.nodeIdGetter = nodeIdGetter;
        this.graph = graph;
    }

    @Override
    public JsonElement serialize(final IEdge src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        final INode source = graph.getEdgeSource(src);
        final INode target = graph.getEdgeTarget(src);

        final String sourceId = nodeIdGetter.apply(source);
        final String targetId = nodeIdGetter.apply(target);

        final JsonObject edgeData = new JsonObject();
        edgeData.addProperty("source", sourceId);
        edgeData.addProperty("target", targetId);
        edgeData.addProperty("weight", graph.getEdgeWeight(src));
        return edgeData;
    }
}
