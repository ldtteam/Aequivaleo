package com.ldtteam.aequivaleo.analyzer.jgrapht;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class IngredientCandidateGraphNode implements IAnalysisGraphNode
{

    @NotNull
    private final Set<IAnalysisGraphNode> analyzedInputNodes = Sets.newConcurrentHashSet();

    @NotNull
    private ImmutableSet<CompoundInstance> compoundInstances = ImmutableSet.of();

    @NotNull
    private final IRecipeIngredient ingredient;

    public IngredientCandidateGraphNode(@NotNull final IRecipeIngredient ingredient) {this.ingredient = ingredient;}

    @NotNull
    public IRecipeIngredient getIngredient()
    {
        return ingredient;
    }

    @NotNull
    public ImmutableSet<CompoundInstance> getCompoundInstances()
    {
        return compoundInstances;
    }

    public void addCompound(final CompoundInstance instance) {
        final Set<CompoundInstance> instances = new HashSet<>(getCompoundInstances());
        instances.add(instance);

        this.compoundInstances = ImmutableSet.copyOf(instances);
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

    @NotNull
    public Set<IAnalysisGraphNode> getAnalyzedInputNodes()
    {
        return analyzedInputNodes;
    }

    @Override
    public String toString()
    {
        return "IngredientCandidateGraphNode{" +
                 "ingredient=" + ingredient +
                 '}';
    }
}
