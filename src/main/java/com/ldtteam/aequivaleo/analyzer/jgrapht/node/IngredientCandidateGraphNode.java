package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.ldtteam.aequivaleo.analyzer.StatCollector;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import org.jetbrains.annotations.NotNull;

public class IngredientCandidateGraphNode extends AbstractNode
{
    @NotNull
    private final IRecipeIngredient ingredient;

    public IngredientCandidateGraphNode(@NotNull final IRecipeIngredient ingredient) {this.ingredient = ingredient;}

    @NotNull
    public IRecipeIngredient getIngredient()
    {
        return ingredient;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof IngredientCandidateGraphNode))
        {
            return false;
        }

        final IngredientCandidateGraphNode that = (IngredientCandidateGraphNode) o;

        return getIngredient().equals(that.getIngredient());
    }

    @Override
    public int hashCode()
    {
        return getIngredient().hashCode();
    }

    @Override
    public String toString()
    {
        return "IngredientCandidateGraphNode{" +
                 "ingredient=" + ingredient +
                 '}';
    }

    @Override
    public void collectStats(final StatCollector statCollector)
    {
        statCollector.onVisitIngredientNode();
    }
}
