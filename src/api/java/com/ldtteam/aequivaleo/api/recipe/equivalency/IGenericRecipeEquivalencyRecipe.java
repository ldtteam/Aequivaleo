package com.ldtteam.aequivaleo.api.recipe.equivalency;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents a generic recipe equivalency recipe.
 * <p>
 *     This is the in-graph and game representation of a generic recipe.
 * </p>
 */
public interface IGenericRecipeEquivalencyRecipe extends IEquivalencyRecipe
{

    /**
     * Gets the recipe name.
     *
     * @return The recipe name.
     */
    ResourceLocation getRecipeName();
}
