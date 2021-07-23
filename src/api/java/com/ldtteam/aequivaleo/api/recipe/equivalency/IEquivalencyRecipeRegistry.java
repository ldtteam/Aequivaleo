package com.ldtteam.aequivaleo.api.recipe.equivalency;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * A registry containing recipes which the analysis engine uses to determine how compounds are passed from inputs to outputs.
 */
public interface IEquivalencyRecipeRegistry
{

    /**
     * Gives access to the current instance of the recipe registry.
     *
     * @param worldKey The key for the world for which the instance is retrieved.
     *
     * @return The recipe registry.
     */
    static IEquivalencyRecipeRegistry getInstance(@NotNull final ResourceKey<Level> worldKey) {
        return IAequivaleoAPI.getInstance().getEquivalencyRecipeRegistry(worldKey);
    }

    /**
     * Adds a new recipe to the registry.
     *
     * @param recipe The recipe to add.
     * @return The registry with the recipe added.
     */
    @NotNull
    IEquivalencyRecipeRegistry register(@NotNull final IEquivalencyRecipe recipe);
}
