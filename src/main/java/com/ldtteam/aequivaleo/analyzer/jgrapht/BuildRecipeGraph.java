package com.ldtteam.aequivaleo.analyzer.jgrapht;

import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.INode;
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

    private final IGraph                               recipeGraph;
    private final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds;
    private final Map<ICompoundContainer<?>, INode>  compoundNodes;
    private final Map<IRecipeIngredient, INode>      ingredientNodes;
    private final Set<INode> notDefinedGraphNodes;
    private final SourceNode                                             sourceNode;

    public BuildRecipeGraph(
      final IGraph recipeGraph,
      final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds,
      final Map<ICompoundContainer<?>, INode> compoundNodes,
      final Map<IRecipeIngredient, INode> ingredientNodes,
      final Set<INode> notDefinedGraphNodes,
      final SourceNode sourceNode)
    {
        this.recipeGraph = recipeGraph;
        this.resultingCompounds = resultingCompounds;
        this.compoundNodes = compoundNodes;
        this.ingredientNodes = ingredientNodes;
        this.notDefinedGraphNodes = notDefinedGraphNodes;
        this.sourceNode = sourceNode;
    }

    public IGraph getRecipeGraph()
    {
        return recipeGraph;
    }

    public Map<ICompoundContainer<?>, Set<CompoundInstance>> getResultingCompounds()
    {
        return resultingCompounds;
    }

    public Map<ICompoundContainer<?>, INode> getCompoundNodes()
    {
        return compoundNodes;
    }

    public Map<IRecipeIngredient, INode> getIngredientNodes()
    {
        return ingredientNodes;
    }

    public Set<INode> getNotDefinedGraphNodes()
    {
        return notDefinedGraphNodes;
    }

    public SourceNode getSourceGraphNode()
    {
        return sourceNode;
    }
}
