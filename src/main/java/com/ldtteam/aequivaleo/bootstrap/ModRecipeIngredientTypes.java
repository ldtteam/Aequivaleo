package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data.IRecipeIngredientType;
import com.ldtteam.aequivaleo.recipe.equivalency.ingredient.SimpleIngredient;
import com.ldtteam.aequivaleo.recipe.equivalency.ingredient.TagIngredient;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModRecipeIngredientTypes {
    
    
    private ModRecipeIngredientTypes() {
        throw new IllegalStateException("Tried to create utility class!");
    }
    
    public static DeferredHolder<IRecipeIngredientType, SimpleIngredient.Type> SIMPLE;
    
    public static DeferredHolder<IRecipeIngredientType, TagIngredient.Type> TAG;
}
