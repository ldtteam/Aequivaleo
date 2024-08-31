package com.ldtteam.aequivaleo.analysis;

import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.api.util.AequivaleoLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StatCollector {
    private static final Logger LOGGER = LogManager.getLogger();

    private final String name;
    private final long totalNodes;
    private long lastReportingTime = 0;
    private long visitedNodes = 0;
    private long sourceNodesVisited;
    private long containerNodesVisited;
    private long ingredientNodesVisited;
    private long recipeNodesVisited;
    private long cycleNodesVisited;
    private long cliqueNodesVisited;

    public StatCollector(final String name, final int totalNodes) {
        this.name = name;
        this.totalNodes = totalNodes;
        LOGGER.info(String.format("Starting analysis of recipe graph for world: %s. (%d nodes)", name, totalNodes));
    }

    public void onVisitSourceNode(INode node) {
        sourceNodesVisited++;
        onVisitNode(node);
    }

    public void onVisitContainerNode(INode node) {
        containerNodesVisited++;
        onVisitNode(node);
    }

    public void onVisitIngredientNode(INode node) {
        ingredientNodesVisited++;
        onVisitNode(node);
    }

    public void onVisitRecipeNode(INode node) {
        recipeNodesVisited++;
        onVisitNode(node);
    }

    public void onVisitCycleNode(INode node) {
        cycleNodesVisited++;
        onVisitNode(node);
    }

    public void onVisitCliqueNode(INode node) {
        cliqueNodesVisited++;
        onVisitNode(node);
    }

    private void onVisitNode(INode node) {
        visitedNodes++;
        final long now = System.currentTimeMillis();

        if (now >= lastReportingTime + (5 * 1000)) {
            lastReportingTime = now;
            logState();
        }

        if (Aequivaleo.getInstance().getConfiguration().getCommon().debugAnalysisLog.get()) {
            AequivaleoLogger.info("Visited node: %s".formatted(node));
        }
    }

    protected void logState() {

        if (totalNodes > 0) {
            final int newPercentage = (int) Math.floorDiv(visitedNodes * 100, totalNodes);

            LOGGER.info(String.format("Visited: %d%% of nodes during analysis of recipe graph for world: %s. (%d/%d/%d/%d/%d/%d of %d)",
                    newPercentage,
                    name,
                    sourceNodesVisited,
                    containerNodesVisited,
                    ingredientNodesVisited,
                    recipeNodesVisited,
                    cycleNodesVisited,
                    cliqueNodesVisited,
                    totalNodes));
        } else {
            LOGGER.info(String.format("No nodes required visiting for world: %s", name));
        }
    }

    public void onCalculationComplete() {
        logState();
    }
}
