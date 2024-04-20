package com.ldtteam.aequivaleo.api.util;

import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data.IRecipeIngredientType;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistry;
import net.minecraft.core.Registry;

/**
 * Defines the registries that are used by the mod.
 */
public final class ModRegistries {
    private ModRegistries() {
        throw new IllegalStateException("Tried to initialize: ModRegistries but this is a Utility class.");
    }

    /**
     * The registry for compound types.
     * <p>
     *     This is a custom kind of registry, as it is synced between client and server.
     *     The servers datapack drives the content of this registry.
     * </p>
     *
     */
    public static ISyncedRegistry<ICompoundType, ICompoundTypeGroup> COMPOUND_TYPE;

    /**
     * The registry for compound container types.
     */
    public static Registry<ICompoundContainerType<?>> CONTAINER_FACTORY;

    /**
     * The registry for compound type groups.
     */
    public static Registry<ICompoundTypeGroup> COMPOUND_TYPE_GROUP;

    /**
     * The registry for recipe ingredient types.
     */
    public static Registry<IRecipeIngredientType> RECIPE_INGREDIENT_TYPE;
}
