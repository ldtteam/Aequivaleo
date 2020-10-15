package com.ldtteam.aequivaleo.analyzer.jgrapht;

import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisGraphNode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisNodeWithContainer;
import com.ldtteam.aequivaleo.analyzer.jgrapht.node.SourceGraphNode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import org.jgrapht.Graph;

import java.util.Map;
import java.util.Set;

public class BuildRecipeGraph
{

    private final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> recipeGraph;
    private final Map<ICompoundContainer<?>, Set<CompoundInstance>>                      resultingCompounds;
    private final Map<ICompoundContainer<?>, IAnalysisGraphNode<Set<CompoundInstance>>>  compoundNodes;
    private final Map<IRecipeIngredient, IAnalysisGraphNode<Set<CompoundInstance>>>      ingredientNodes;
    private final Set<IAnalysisNodeWithContainer<Set<CompoundInstance>>>                                         notDefinedGraphNodes;
    private final SourceGraphNode                                                        sourceGraphNode;

    public BuildRecipeGraph(
      final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> recipeGraph,
      final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds,
      final Map<ICompoundContainer<?>, IAnalysisGraphNode<Set<CompoundInstance>>> compoundNodes,
      final Map<IRecipeIngredient, IAnalysisGraphNode<Set<CompoundInstance>>> ingredientNodes,
      final Set<IAnalysisNodeWithContainer<Set<CompoundInstance>>> notDefinedGraphNodes,
      final SourceGraphNode sourceGraphNode)
    {
        this.recipeGraph = recipeGraph;
        this.resultingCompounds = resultingCompounds;
        this.compoundNodes = compoundNodes;
        this.ingredientNodes = ingredientNodes;
        this.notDefinedGraphNodes = notDefinedGraphNodes;
        this.sourceGraphNode = sourceGraphNode;
    }

    public Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> getRecipeGraph()
    {
        return recipeGraph;
    }

    public Map<ICompoundContainer<?>, Set<CompoundInstance>> getResultingCompounds()
    {
        return resultingCompounds;
    }

    public Map<ICompoundContainer<?>, IAnalysisGraphNode<Set<CompoundInstance>>> getCompoundNodes()
    {
        return compoundNodes;
    }

    public Map<IRecipeIngredient, IAnalysisGraphNode<Set<CompoundInstance>>> getIngredientNodes()
    {
        return ingredientNodes;
    }

    public Set<IAnalysisNodeWithContainer<Set<CompoundInstance>>> getNotDefinedGraphNodes()
    {
        return notDefinedGraphNodes;
    }

    public SourceGraphNode getSourceGraphNode()
    {
        return sourceGraphNode;
    }
}
