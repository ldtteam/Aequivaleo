package com.ldtteam.aequivaleo.recipe.equivalency.ingredient;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import org.apache.commons.lang3.Validate;

import java.util.Set;
import java.util.SortedSet;

public class SimpleIngredientBuilder implements com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.ISimpleRecipeIngredientBuilder {
    private SortedSet<ICompoundContainer<?>> candidates = Sets.newTreeSet();
    private Double                           count;

    public SimpleIngredientBuilder from(final IRecipeIngredient ingredient) {
        return from(ingredient.getCandidates()).withCount(ingredient.getRequiredCount());
    }

    public SimpleIngredientBuilder from(final ICompoundContainer<?> container) {
        return withCandidate(container).withCount(container.contentsCount());
    }

    public SimpleIngredientBuilder from(final SortedSet<ICompoundContainer<?>> containers) {
        this.candidates = containers;
        return this;
    }

    @Override
    public SimpleIngredientBuilder withCandidate(final ICompoundContainer<?> container) {
        this.candidates.clear();
        this.candidates.add(container);
        return this;
    }

    @Override
    public SimpleIngredientBuilder withCandidates(final Set<ICompoundContainer<?>> candidates)
    {
        this.candidates.clear();
        this.candidates.addAll(candidates);
        return this;
    }

    @Override
    public SimpleIngredientBuilder withCount(final double count)
    {
        this.count = count;
        return this;
    }

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

    public IRecipeIngredient createIngredient() {
        return createSimpleIngredient();
    }
}