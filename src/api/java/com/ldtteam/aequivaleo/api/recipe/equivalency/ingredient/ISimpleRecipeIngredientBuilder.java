package com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;

import java.util.Set;

/**
 * Represents a builder for a simple recipe ingredient.
 */
public interface ISimpleRecipeIngredientBuilder {

    /**
     * Adds a given container as a candidate for the ingredient.
     *
     * @param container The container to add.
     * @return The builder.
     */
    ISimpleRecipeIngredientBuilder withCandidate(ICompoundContainer<?> container);

    /**
     * Adds a given set of containers as candidates for the ingredient.
     *
     * @param candidates The containers to add.
     * @return The builder.
     */
    ISimpleRecipeIngredientBuilder withCandidates(Set<ICompoundContainer<?>> candidates);

    /**
     * Sets the given count for the ingredient.
     *
     * @param count The count to set.
     * @return The builder.
     */
    ISimpleRecipeIngredientBuilder withCount(double count);
}
