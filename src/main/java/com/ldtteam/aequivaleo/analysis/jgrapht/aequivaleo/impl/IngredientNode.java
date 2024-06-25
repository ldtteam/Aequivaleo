package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.impl;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IIngredientNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.base.ResultsOwningNode;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;

import java.util.Objects;

public final class IngredientNode extends ResultsOwningNode implements IIngredientNode {

    private final IRecipeIngredient ingredient;

    public IngredientNode(IRecipeIngredient ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public NodeType type() {
        return NodeType.INGREDIENT;
    }

    @Override
    public IRecipeIngredient ingredient() {
        return ingredient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngredientNode that = (IngredientNode) o;
        return Objects.equals(ingredient, that.ingredient);
    }

    @Override
    protected int calculateHashCode() {
        return Objects.hashCode(ingredient);
    }

    @Override
    protected String calculateStringRepresentation() {
        return "IngredientNode{" +
                "ingredient=" + ingredient +
                '}';
    }
}
