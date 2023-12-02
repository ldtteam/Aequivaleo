package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data.IRecipeIngredientType;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.recipe.equivalency.ingredient.SimpleIngredient;
import com.ldtteam.aequivaleo.recipe.equivalency.ingredient.TagIngredient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class RecipeIngredientTypeRegistrar {
    
    private static final ResourceLocation TYPE_REGISTRY_NAME = new ResourceLocation(Constants.MOD_ID, "recipe_ingredient_type");
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static final DeferredRegister<IRecipeIngredientType> RECIPE_INGREDIENT_TYPE_REGISTRY = DeferredRegister.create(
            ResourceKey.createRegistryKey(TYPE_REGISTRY_NAME),
            Constants.MOD_ID
    );
    
    static {
        ModRegistries.RECIPE_INGREDIENT_TYPE = RECIPE_INGREDIENT_TYPE_REGISTRY.makeRegistry(builder -> builder.maxId(Integer.MAX_VALUE));
        
        ModRecipeIngredientTypes.SIMPLE = RECIPE_INGREDIENT_TYPE_REGISTRY.register("simple", SimpleIngredient.Type::new);
        ModRecipeIngredientTypes.TAG = RECIPE_INGREDIENT_TYPE_REGISTRY.register("tag", TagIngredient.Type::new);
    }
}
