package com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.util.SortedSetComparator;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

/**
 * Represents an ingredient to a recipe.
 * This basically defines all variants that are allows to go into one slot of a recipe.
 */
public interface IRecipeIngredient extends Comparable<IRecipeIngredient>
{

    /**
     * Indicates if this ingredient is valid.
     *
     * @return {@code True} when valid.
     */
    default boolean isValid() {
        return !getCandidates().isEmpty() && getCandidates().stream().allMatch(ICompoundContainer::isValid);
    }

    /**
     * The candidates who match this ingredient.
     *
     * @return The candidates.
     */
    SortedSet<ICompoundContainer<?>> getCandidates();

    /**
     * The required count of this ingredient.
     *
     * @return The required count.
     */
    Double getRequiredCount();


    @Override
    default int compareTo(@NotNull final IRecipeIngredient iRecipeIngredient)
    {
        final int candidateComparison = SortedSetComparator.<ICompoundContainer<?>>getInstance().compare(getCandidates(), iRecipeIngredient.getCandidates());
        if (candidateComparison != 0)
            return candidateComparison;

        return (int) (getRequiredCount() - iRecipeIngredient.getRequiredCount());
    }
}
