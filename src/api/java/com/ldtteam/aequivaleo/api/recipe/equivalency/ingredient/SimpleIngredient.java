package com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents a simple ingredient for a recipe.
 */
public class SimpleIngredient implements IRecipeIngredient
{
    private final SortedSet<ICompoundContainer<?>> candidates;
    private final double                           count;

    SimpleIngredient(final Set<ICompoundContainer<?>> candidates, final double count) {
        this.candidates = new TreeSet<>(candidates);
        this.count = count;
    }

    @Override
    public SortedSet<ICompoundContainer<?>> getCandidates()
    {
        return candidates;
    }

    @Override
    public double getRequiredCount()
    {
        return count;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final SimpleIngredient that = (SimpleIngredient) o;
        return Double.compare(that.count, count) == 0 &&
                 getCandidates().equals(that.getCandidates());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getCandidates(), count);
    }
}
