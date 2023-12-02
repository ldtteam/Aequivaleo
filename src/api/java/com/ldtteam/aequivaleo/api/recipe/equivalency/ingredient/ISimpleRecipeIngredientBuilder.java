package com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;

import java.util.Set;

public interface ISimpleRecipeIngredientBuilder {
    ISimpleRecipeIngredientBuilder withCandidate(ICompoundContainer<?> container);
    
    ISimpleRecipeIngredientBuilder withCandidates(Set<ICompoundContainer<?>> candidates);
    
    ISimpleRecipeIngredientBuilder withCount(double count);
}
