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
    private final ICompoundContainer<?> output;

    /**
     * Creates a new recipe variant.
     *
     * @param output The output of the recipe.
     */
    public RecipeVariant(ICompoundContainer<?> output) {
        this.output = output;
    }

    /**
     * The ingredients that make up this variant.
     *
     * @return The ingredients in this recipe variant.
     */
    public Collection<IRecipeIngredient> ingredients() {
        return ingredients;
    }

    /**
     * The remainders of this recipe that are left over when the ingredients are consumed for this variant to produce the output.
     *
     * @return The remainders of this recipe.
     */
    public Collection<IRecipeIngredient> remainders() {
        return remainders;
    }

    void setIngredients(Collection<IRecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    void setRemainders(Collection<IRecipeIngredient> remainders) {
        this.remainders = remainders;
    }

    /**
     * The result of this variant when it is crafted.
     *
     * @return The output.
     */
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
