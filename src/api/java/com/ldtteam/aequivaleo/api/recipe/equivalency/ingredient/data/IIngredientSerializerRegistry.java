package com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a registry which manages the different serializers for data driven loading of the
 * ingredients of a recipe.
 */
public interface IIngredientSerializerRegistry
{
    /**
     * Gives access to the current instance of the serializer registry.
     *
     * @return The serializer registry.
     */
    static IIngredientSerializerRegistry getInstance() {
        return IAequivaleoAPI.getInstance().getIngredientSerializerRegistry();
    }

    /**
     * Registers the new serializer.
     *
     * @param serializer The serializer.
     * @param <S> The type of the serializer.
     * @param <I> The type of the recipe ingredient that the serializer can handle.
     * @return The registry.
     */
    <S extends IIngredientSerializer<I>, I extends IRecipeIngredient> IIngredientSerializerRegistry register(S serializer);

}
