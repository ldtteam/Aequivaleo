package com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data;

import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.mojang.serialization.Codec;

/**
 * Interfaces that describes a type for recipe ingredients loaded from datapacks.
 */
public interface IRecipeIngredientType
{
    /**
     * Defines the codec to use during serialization and deserialization.
     *
     * @return The codec.
     */
    Codec<? extends IRecipeIngredient> codec();
}
