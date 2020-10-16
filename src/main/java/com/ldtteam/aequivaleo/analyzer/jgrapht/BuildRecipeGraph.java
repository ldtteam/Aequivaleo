package com.ldtteam.aequivaleo.analyzer.jgrapht;

import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.Edge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisGraphNode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisNodeWithContainer;
import com.ldtteam.aequivaleo.analyzer.jgrapht.node.SourceNode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import org.jgrapht.Graph;

import java.util.Map;
import java.util.Set;

public class BuildRecipeGraph
{

    private final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, Edge> recipeGraph;
    private final Map<ICompoundContainer<?>, Set<CompoundInstance>>      resultingCompounds;
    private final Map<ICompoundContainer<?>, IAnalysisGraphNode<Set<CompoundInstance>>>  compoundNodes;
    private final Map<IRecipeIngredient, IAnalysisGraphNode<Set<CompoundInstance>>>      ingredientNodes;
    private final Set<IAnalysisNodeWithContainer<Set<CompoundInstance>>> notDefinedGraphNodes;
    private final SourceNode                                             sourceNode;

    public BuildRecipeGraph(
      final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, Edge> recipeGraph,
      final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds,
      final Map<ICompoundContainer<?>, IAnalysisGraphNode<Set<CompoundInstance>>> compoundNodes,
      final Map<IRecipeIngredient, IAnalysisGraphNode<Set<CompoundInstance>>> ingredientNodes,
      final Set<IAnalysisNodeWithContainer<Set<CompoundInstance>>> notDefinedGraphNodes,
      final SourceNode sourceNode)
    {
        this.recipeGraph = recipeGraph;
        this.resultingCompounds = resultingCompounds;
        this.compoundNodes = compoundNodes;
        this.ingredientNodes = ingredientNodes;
        this.notDefinedGraphNodes = notDefinedGraphNodes;
        this.sourceNode = sourceNode;
    }

    public Graph<IAnalysisGraphNode<Set<CompoundInstance>>, Edge> getRecipeGraph()
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

    public SourceNode getSourceGraphNode()
    {
        return sourceNode;
    }
}
