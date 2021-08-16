package com.ldtteam.aequivaleo.analysis.jgrapht.node;

import com.ldtteam.aequivaleo.analysis.StatCollector;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IRecipeInputNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IRecipeNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IStartAnalysisNode;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class IngredientNode extends AbstractNode implements IRecipeInputNode, IStartAnalysisNode
{
    @NotNull
    private final IRecipeIngredient ingredient;
    private final int hashCode;

    public IngredientNode(@NotNull final IRecipeIngredient ingredient) {
        this.ingredient = ingredient;
        this.hashCode = ingredient.hashCode();
    }

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
        if (!(o instanceof IngredientNode))
        {
            return false;
        }

        final IngredientNode that = (IngredientNode) o;

        return getIngredient().equals(that.getIngredient());
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    @Override
    public String toString()
    {
        return "IngredientNode{" + ingredient +
                 '}';
    }

    @Override
    public void collectStats(final StatCollector statCollector)
    {
        statCollector.onVisitIngredientNode();
    }

    @Override
    public Set<CompoundInstance> getInputInstances(final IRecipeNode recipeNode)
    {
        return getResultingValue().orElse(Collections.emptySet());
    }

}
