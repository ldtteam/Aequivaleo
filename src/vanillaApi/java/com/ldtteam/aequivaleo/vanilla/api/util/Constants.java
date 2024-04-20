package com.ldtteam.aequivaleo.vanilla.api.util;

import net.minecraft.resources.ResourceLocation;

/**
 * Class to hold all the constants used by the vanilla plugin.
 */
public final class Constants
{

    private Constants()
    {
        throw new IllegalStateException("Tried to initialize: Constants but this is a Utility class.");
    }

    /**
     * The recipe type name for simple recipes.
     */
    public static final ResourceLocation SIMPLE_RECIPE_TYPE = new ResourceLocation("simple");

    /**
     * The recipe type name for cooking recipes.
     */
    public static final ResourceLocation COOKING_RECIPE_TYPE       = new ResourceLocation("cooking");

    /**
     * The recipe type name for smelting recipes.
     */
    public static final ResourceLocation STONE_CUTTING_RECIPE_TYPE = new ResourceLocation("stonecutting");

    /**
     * The recipe type name for smithing transform recipes.
     */
    public static final ResourceLocation SMITHING_TRANSFORM_RECIPE_TYPE = new ResourceLocation("smithing_transform");

    /**
     * The recipe type name for smithing trim recipes.
     */
    public static final ResourceLocation SMITHING_TRIM_RECIPE_TYPE = new ResourceLocation("smithing_trim");

    /**
     * The recipe type name for decorated pot recipes.
     */
    public static final ResourceLocation DECORATED_POT_RECIPE_TYPE = new ResourceLocation("decorated_pot");
}
