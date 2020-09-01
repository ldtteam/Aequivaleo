package com.ldtteam.aequivaleo.analyzer.debug;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ldtteam.aequivaleo.analyzer.jgrapht.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.IAnalysisGraphNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.json.JSONExporter;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;

public class GraphIOHandler
{

    private static final Logger LOGGER = LogManager.getLogger();
    private static final GraphIOHandler INSTANCE = new GraphIOHandler();

    public static GraphIOHandler getInstance()
    {
        return INSTANCE;
    }

    private GraphIOHandler()
    {
    }

    public void export(
      @NotNull final String name,
      @NotNull final Graph<IAnalysisGraphNode, AccessibleWeightEdge> recipeGraph
    ) {
        final JSONExporter<IAnalysisGraphNode, AccessibleWeightEdge> exporter = new JSONExporter<>();
        exporter.setVertexAttributeProvider(this::extractVertexData);
        exporter.setEdgeAttributeProvider(this::extractEdgeData);

        exporter.exportGraph(recipeGraph, Paths.get(".", name).toFile());

        LOGGER.warn(String.format("Exported: %s as recipe graph.", name));
    }

    @NotNull
    private Map<String, Attribute> extractVertexData(@NotNull final IAnalysisGraphNode node) {
        final Map<String, Attribute> results = Maps.newHashMap();
        results.put("display", new DefaultAttribute<>(node.toString(), AttributeType.STRING));
        return results;
    }

    @NotNull
    private Map<String, Attribute> extractEdgeData(@NotNull final AccessibleWeightEdge edge) {
        return ImmutableMap.of("weight", new DefaultAttribute<>(edge.getWeight(), AttributeType.DOUBLE));
    }

}
