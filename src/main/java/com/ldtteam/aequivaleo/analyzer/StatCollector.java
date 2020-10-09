package com.ldtteam.aequivaleo.analyzer;

import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StatCollector
{
    private static final Logger LOGGER = LogManager.getLogger();

    private final ResourceLocation worldName;
    private final long             totalNodes;
    private       long             lastReportingTime = 0;
    private       long             visitedNodes      = 0;
    private       long sourceNodesVisited;
    private       long containerNodesVisited;
    private       long ingredientNodesVisited;
    private       long             recipeNodesVisited;

    public StatCollector(final ResourceLocation worldName, final int totalNodes)
    {
        this.worldName = worldName;
        this.totalNodes = totalNodes;
    }

    public void onVisitSourceNode()
    {
        sourceNodesVisited++;
        onVisitNode();
    }

    public void onVisitContainerNode()
    {
        containerNodesVisited++;
        onVisitNode();
    }

    public void onVisitIngredientNode()
    {
        ingredientNodesVisited++;
        onVisitNode();
    }

    public void onVisitRecipeNode()
    {
        recipeNodesVisited++;
        onVisitNode();
    }

    private void onVisitNode()
    {
        visitedNodes++;
        final long now = System.currentTimeMillis();

        if (now >= lastReportingTime + (5 * 1000))
        {
            lastReportingTime = now;
            logState();
        }
    }

    private void logState()
    {
        final int newPercentage = (int) Math.floorDiv(visitedNodes * 100, totalNodes);

        LOGGER.info(String.format("Visited: %d%% of nodes during analysis of recipe graph for world: %s. (%d/%d/%d/%d of %d)",
          newPercentage,
          worldName,
          sourceNodesVisited,
          containerNodesVisited,
          ingredientNodesVisited,
          recipeNodesVisited,
          totalNodes));
    }

    public void onCalculationComplete()
    {
        logState();
    }
}
