package com.ldtteam.aequivaleo.analyzer.debug;

import com.ldtteam.aequivaleo.analyzer.io.JSONGraphExporter;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.Edge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisGraphNode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;

import java.nio.file.Paths;
import java.util.Set;

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
      @NotNull final Graph<INode, IEdge> recipeGraph
    ) {
        final JSONGraphExporter exporter = new JSONGraphExporter();

        exporter.exportGraph(recipeGraph, Paths.get(".", name).toFile());

        LOGGER.warn(String.format("Exported: %s as recipe graph.", name));
    }

}
