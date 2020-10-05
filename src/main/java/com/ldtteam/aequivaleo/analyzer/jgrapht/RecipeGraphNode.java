package com.ldtteam.aequivaleo.analyzer.jgrapht;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class RecipeGraphNode implements IAnalysisGraphNode
{

    @NotNull
    private final Set<IAnalysisGraphNode> analyzedInputNodes = Sets.newConcurrentHashSet();

    @NotNull
    private final IEquivalencyRecipe recipe;

    public RecipeGraphNode(@NotNull final IEquivalencyRecipe recipe) {this.recipe = recipe;}

    @NotNull
    public IEquivalencyRecipe getRecipe()
    {
        return recipe;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RecipeGraphNode))
        {
            return false;
        }

        final RecipeGraphNode that = (RecipeGraphNode) o;

        return getRecipe().equals(that.getRecipe());
    }

    @Override
    public int hashCode()
    {
        return getRecipe().hashCode();
    }

    @NotNull
    public Set<IAnalysisGraphNode> getAnalyzedInputNodes()
    {
        return analyzedInputNodes;
    }

    @Override
    public String toString()
    {
        return "RecipeGraphNode{" +
                 "recipe=" + recipe +
                 '}';
    }
}
