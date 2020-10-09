package com.ldtteam.aequivaleo.analyzer.io;

import com.google.gson.*;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.node.IAnalysisGraphNode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import org.jgrapht.Graph;
import org.jgrapht.nio.BaseExporter;
import org.jgrapht.nio.GraphExporter;
import org.jgrapht.nio.IntegerIdProvider;

import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Function;

public class JSONGraphExporter extends BaseExporter<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> implements GraphExporter<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge>, JsonSerializer<Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge>>
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
    public JSONGraphExporter(final Function<IAnalysisGraphNode<Set<CompoundInstance>>, String> vertexIdProvider)
    {
        super(vertexIdProvider);
    }

    @Override
    public void exportGraph(final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> g, final Writer writer)
    {
        final Gson gson = new GsonBuilder()
                                    .registerTypeAdapter(Graph.class, this)
                                    .registerTypeAdapter(IAnalysisGraphNode.class, new GraphNodeJSONHandler(this::getVertexId))
                                    .registerTypeAdapter(AccessibleWeightEdge.class, new GraphEdgeJSONHandler(
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
      final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        final JsonObject graph = new JsonObject();
        graph.addProperty("creator", "LDTTeam Aequivaleo - Graph Serializer");
        graph.addProperty("version", 1);

        final JsonArray nodes = new JsonArray();
        for (final IAnalysisGraphNode<Set<CompoundInstance>> iAnalysisGraphNode : src.vertexSet())
        {
            nodes.add(context.serialize(iAnalysisGraphNode, IAnalysisGraphNode.class));
        }
        graph.add("nodes", nodes);

        final JsonArray edges = new JsonArray();
        for (final AccessibleWeightEdge accessibleWeightEdge : src.edgeSet())
        {
            edges.add(context.serialize(accessibleWeightEdge, AccessibleWeightEdge.class));
        }
        graph.add("edges", edges);
        return graph;
    }
}
