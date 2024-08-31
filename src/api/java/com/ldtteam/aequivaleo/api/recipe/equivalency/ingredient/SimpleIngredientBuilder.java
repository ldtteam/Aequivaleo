package com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import org.apache.commons.lang3.Validate;

import java.util.Set;
import java.util.SortedSet;

/**
 * A builder for simple ingredients.
 * <p>
 *     This builder is implemented as a mutable builder.
 * </p>
 */
public class SimpleIngredientBuilder {

    /**
     * Creates a simple ingredient from a given container.
     *
     * @param container The container to create an ingredient from.
     * @return The simple ingredient.
     */
    public static IRecipeIngredient simple(ICompoundContainer<?> container) {
        return new SimpleIngredientBuilder().from(container).createIngredient();
    }

    private SortedSet<ICompoundContainer<?>> candidates = Sets.newTreeSet();
    private Double                           count;

    /**
     * Creates a new simple ingredient builder from a given ingredient.
     *
     * @param ingredient The ingredient.
     * @return The simple ingredient builder.
     */
    public SimpleIngredientBuilder from(final IRecipeIngredient ingredient) {
        return from(ingredient.getCandidates()).withCount(ingredient.getRequiredCount());
    }

    /**
     * Creates a new simple ingredient builder from a given container.
     *
     * @param container The container.
     * @return The simple ingredient builder.
     */
    public SimpleIngredientBuilder from(final ICompoundContainer<?> container) {
        return withCandidate(container).withCount(container.getContentsCount());
    }

    /**
     * Sets the candidates of the ingredient.
     *
     * @param containers The candidates.
     * @return This builder.
     */
    public SimpleIngredientBuilder from(final SortedSet<ICompoundContainer<?>> containers) {
        this.candidates = containers.stream().map(container -> ICompoundContainer.from(container.getContents(), 1)).collect(Sets::newTreeSet, Set::add, Set::addAll);
        return this;
    }

    /**
     * Sets the candidate of the ingredient.
     *
     * @param container The candidate.
     * @return This builder.
     */
    public SimpleIngredientBuilder withCandidate(final ICompoundContainer<?> container) {
        this.candidates.clear();
        this.candidates.add(container);
        return this;
    }

    /**
     * Sets the candidates of the ingredient.
     *
     * @param candidates The candidates.
     * @return This builder.
     */
    public SimpleIngredientBuilder withCandidates(final Set<ICompoundContainer<?>> candidates)
    {
        this.candidates.clear();
        this.candidates.addAll(candidates);
        return this;
    }

    /**
     * Sets the count of the ingredient.
     *
     * @param count The count.
     * @return This builder.
     */
    public SimpleIngredientBuilder withCount(final double count)
    {
        this.count = count;
        return this;
    }

    /**
     * Creates a simple ingredient.
     *
     * @return The simple ingredient.
     */
    public SimpleIngredient createSimpleIngredient()
    {
        Validate.notEmpty(candidates);
        Validate.noNullElements(candidates);
        Validate.notNull(count);
        Validate.notNaN(count);

        if (count <= 0)
            throw new IllegalArgumentException("The given count has to be greater then 0");

        return new SimpleIngredient(candidates, count);
    }

    /**
     * Creates a simple ingredient.
     *
     * @return The simple ingredient.
     */
    public IRecipeIngredient createIngredient() {
        return createSimpleIngredient();
    }
}