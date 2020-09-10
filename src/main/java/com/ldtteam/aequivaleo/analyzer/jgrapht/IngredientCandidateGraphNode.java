package com.ldtteam.aequivaleo.analyzer.jgrapht;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.TreeSet;

public class IngredientCandidateGraphNode implements IAnalysisGraphNode
{

    @NotNull
    private final Set<IAnalysisGraphNode> analyzedInputNodes = Sets.newConcurrentHashSet();

    @NotNull
    private final Set<CompoundInstance> compoundInstances = new TreeSet<>();

    @NotNull
    private final Set<ICompoundContainer<?>> candidates;

    public IngredientCandidateGraphNode(@NotNull final Set<ICompoundContainer<?>> candidates) {this.candidates = candidates;}

    @NotNull
    public Set<ICompoundContainer<?>> getCandidates()
    {
        return candidates;
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
        if (!(o instanceof IngredientCandidateGraphNode))
        {
            return false;
        }

        final IngredientCandidateGraphNode that = (IngredientCandidateGraphNode) o;

        return getCandidates().equals(that.getCandidates());
    }

    @Override
    public int hashCode()
    {
        return getCandidates().hashCode();
    }

    @NotNull
    public Set<IAnalysisGraphNode> getAnalyzedInputNodes()
    {
        return analyzedInputNodes;
    }
}
