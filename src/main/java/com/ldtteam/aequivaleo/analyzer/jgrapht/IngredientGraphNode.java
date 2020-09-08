package com.ldtteam.aequivaleo.analyzer.jgrapht;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.TreeSet;

public class IngredientGraphNode implements IAnalysisGraphNode
{

    @NotNull
    private final Set<IAnalysisGraphNode> analyzedInputNodes = Sets.newConcurrentHashSet();

    @NotNull
    private final Set<CompoundInstance> compoundInstances = new TreeSet<>();

    @NotNull
    private final IRecipeIngredient ingredient;

    public IngredientGraphNode(@NotNull final IRecipeIngredient ingredient) {this.ingredient = ingredient;}

    @NotNull
    public IRecipeIngredient getIngredient()
    {
        return ingredient;
    }

    @NotNull
    public Set<CompoundInstance> getCompoundInstances()
    {
        return compoundInstances;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof IngredientGraphNode))
        {
            return false;
        }

        final IngredientGraphNode that = (IngredientGraphNode) o;

        return getIngredient().equals(that.getIngredient());
    }

    @Override
    public int hashCode()
    {
        return getIngredient().hashCode();
    }

    @NotNull
    public Set<IAnalysisGraphNode> getAnalyzedInputNodes()
    {
        return analyzedInputNodes;
    }

    @Override
    public String toString()
    {
        return "IngredientGraphNode{" +
                 "recipe=" + ingredient +
                 '}';
    }
}
