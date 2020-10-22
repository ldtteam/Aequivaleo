package com.ldtteam.aequivaleo.analyzer;

import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StatCollector
{
    private static final Logger LOGGER = LogManager.getLogger();

    private final String name;
    private final long   totalNodes;
    private       long             lastReportingTime = 0;
    private       long             visitedNodes      = 0;
    private       long             sourceNodesVisited;
    private       long             containerNodesVisited;
    private       long             ingredientNodesVisited;
    private       long             recipeNodesVisited;
    private       long             subCycleNodesVisited;

    public StatCollector(final String name, final int totalNodes)
    {
        this.name = name;
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

    public void onInnerGraphNode() {
        subCycleNodesVisited++;
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

    protected void logState()
    {

        if (totalNodes > 0)
        {
            final int newPercentage = (int) Math.floorDiv(visitedNodes * 100, totalNodes);

            LOGGER.info(String.format("Visited: %d%% of nodes during analysis of recipe graph for world: %s. (%d/%d/%d/%d/%d of %d)",
              newPercentage,
              name,
              sourceNodesVisited,
              containerNodesVisited,
              ingredientNodesVisited,
              recipeNodesVisited,
              subCycleNodesVisited,
              totalNodes));
        }
        else
        {
            LOGGER.info(String.format("No nodes required visiting for world: %s", name));
        }
    }

    public void onCalculationComplete()
    {
        logState();
    }
}
