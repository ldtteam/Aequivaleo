package com.ldtteam.aequivaleo.analysis.debug;

import com.ldtteam.aequivaleo.analysis.io.JSONGraphExporter;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

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
      @NotNull final IGraph recipeGraph
    ) {
        final JSONGraphExporter exporter = new JSONGraphExporter();

        exporter.exportGraph(recipeGraph, Paths.get(".", name).toFile());

        LOGGER.warn(String.format("Exported: %s as recipe graph.", name));
    }

}
