package com.ldtteam.aequivaleo.api.util;

import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data.IRecipeIngredientType;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistry;
import net.minecraft.core.Registry;

public final class ModRegistries {
    private ModRegistries() {
        throw new IllegalStateException("Tried to initialize: ModRegistries but this is a Utility class.");
    }
    
    public static ISyncedRegistry<ICompoundType, ICompoundTypeGroup> COMPOUND_TYPE;
    public static Registry<ICompoundContainerType<?>> CONTAINER_FACTORY;
    public static Registry<ICompoundTypeGroup> COMPOUND_TYPE_GROUP;
    public static Registry<IRecipeIngredientType> RECIPE_INGREDIENT_TYPE;
}
