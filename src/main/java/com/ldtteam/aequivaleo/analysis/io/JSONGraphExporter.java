package com.ldtteam.aequivaleo.analysis.io;

import com.google.gson.*;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import org.jgrapht.Graph;
import org.jgrapht.nio.BaseExporter;
import org.jgrapht.nio.GraphExporter;
import org.jgrapht.nio.IntegerIdProvider;

import java.io.Writer;
import java.lang.reflect.Type;
import java.util.function.Function;

public class JSONGraphExporter extends BaseExporter<INode, IEdge> implements GraphExporter<INode, IEdge>, JsonSerializer<Graph<INode, IEdge>>
{



    public JSONGraphExporter()
    {
        this(new IntegerIdProvider<>());
    }

    /**
     * Constructor
     *
     * @param vertexIdProvider the vertex id provider to use. Cannot be null.
     */
    public JSONGraphExporter(final Function<INode, String> vertexIdProvider)
    {
        super(vertexIdProvider);
    }

    @Override
    public void exportGraph(final Graph<INode, IEdge> g, final Writer writer)
    {
        final Gson gson = new GsonBuilder()
                                    .registerTypeAdapter(Graph.class, this)
                                    .registerTypeAdapter(INode.class, new GraphNodeJSONHandler(this::getVertexId))
                                    .registerTypeAdapter(IEdge.class, new GraphEdgeJSONHandler(
                                      this::getVertexId,
                                      g
                                    ))
                                    .setPrettyPrinting()
                                    .create();

        final JsonElement graphData = gson.toJsonTree(g, Graph.class);
        gson.toJson(graphData, writer);
    }

    @Override
    public JsonElement serialize(
      final Graph<INode, IEdge> src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        final JsonObject graph = new JsonObject();
        graph.addProperty("creator", "LDTTeam Aequivaleo - Graph Serializer");
        graph.addProperty("version", 1);

        final JsonArray nodes = new JsonArray();
        for (final INode iAnalysisGraphNode : src.vertexSet())
        {
            nodes.add(context.serialize(iAnalysisGraphNode, INode.class));
        }
        graph.add("nodes", nodes);

        final JsonArray edges = new JsonArray();
        for (final IEdge edge : src.edgeSet())
        {
            edges.add(context.serialize(edge, IEdge.class));
        }
        graph.add("edges", edges);
        return graph;
    }
}
