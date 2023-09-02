package com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
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
     * Creates a new ingredient from a single source object.
     * This object needs to have an innate count that its container factory is aware of.
     *
     * @param source The source to create a recipe ingredient of.
     * @return The ingredient which represents the given source object.
     */
    static IRecipeIngredient from(Object source) {
        return new SimpleIngredientBuilder().from(
                ICompoundContainerFactoryManager.getInstance().wrapInContainer(source)
        ).createIngredient();
    }

    /**
     * Creates a new ingredient from a single source container.
     *
     * @param source The source to create a recipe ingredient of.
     * @return The ingredient which represents the given source object.
     */
    static IRecipeIngredient from(ICompoundContainer<?> source) {
        return new SimpleIngredientBuilder().from(
                source
        ).createIngredient();
    }

    /**
     * Creates a new ingredient from a single source object.
     *
     * @param source The source to create a recipe ingredient of.
     * @param count The number of source instances that this ingredient represents.
     * @return The ingredient which represents the given source object.
     */
    static IRecipeIngredient from(Object source, int count) {
        return from(source, (double) count);
    }

    /**
     * Creates a new ingredient from a single source object.
     *
     * @param source The source to create a recipe ingredient of.
     * @param count The number of source instances that this ingredient represents.
     * @return The ingredient which represents the given source object.
     */
    static IRecipeIngredient from(Object source, double count) {
        return new SimpleIngredientBuilder().from(
                ICompoundContainerFactoryManager.getInstance().wrapInContainer(source, count)
        ).createIngredient();
    }

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
