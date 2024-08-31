package com.ldtteam.aequivaleo.api.recipe.equivalency.calculator;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;

import java.util.Collection;

/**
 * Represents a recipe variant.
 */
public final class RecipeVariant {
    private Collection<IRecipeIngredient> ingredients;
    private Collection<IRecipeIngredient> remainders;
    private ICompoundContainer<?> output;

    /**
     * Creates a new recipe variant.
     *
     * @param output The output of the recipe.
     */
    public RecipeVariant(ICompoundContainer<?> output) {
        this.output = output;
    }

    public Collection<IRecipeIngredient> ingredients() {
        return ingredients;
    }

    public Collection<IRecipeIngredient> remainders() {
        return remainders;
    }

    void setIngredients(Collection<IRecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    void setRemainders(Collection<IRecipeIngredient> remainders) {
        this.remainders = remainders;
    }

    public ICompoundContainer<?> output() {
        return output;
    }

    @Override
    public String toString() {
        return "RecipeVariant[" +
                "ingredients=" + ingredients + ", " +
                "remainders=" + remainders + ", " +
                "output=" + output + ']';
    }

}
