package com.ldtteam.aequivaleo.analysis.jgrapht;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IContainerNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IIngredientNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.impl.SourceNode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;

import java.util.Map;
import java.util.Set;

public record BuildRecipeGraph(IGraph recipeGraph, Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds,
                               Map<ICompoundContainer<?>, IContainerNode> compoundNodes,
                               Map<IRecipeIngredient, IIngredientNode> ingredientNodes, Set<INode> notDefinedGraphNodes,
                               SourceNode sourceNode) { }
