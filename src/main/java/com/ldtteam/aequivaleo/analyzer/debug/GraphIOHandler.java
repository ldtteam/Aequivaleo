package com.ldtteam.aequivaleo.analyzer.debug;

import com.ldtteam.aequivaleo.analyzer.io.JSONGraphExporter;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.node.IAnalysisGraphNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;

import java.nio.file.Paths;

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
        final JSONGraphExporter exporter = new JSONGraphExporter();

        exporter.exportGraph(recipeGraph, Paths.get(".", name).toFile());

        LOGGER.warn(String.format("Exported: %s as recipe graph.", name));
    }

}
