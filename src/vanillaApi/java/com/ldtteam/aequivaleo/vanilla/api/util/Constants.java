package com.ldtteam.aequivaleo.vanilla.api.util;

import net.minecraft.resources.ResourceLocation;

public final class Constants
{

    private Constants()
    {
        throw new IllegalStateException("Tried to initialize: Constants but this is a Utility class.");
    }

    public static final ResourceLocation SIMPLE_RECIPE_TYPE = new ResourceLocation("simple");
    public static final ResourceLocation COOKING_RECIPE_TYPE       = new ResourceLocation("cooking");
    public static final ResourceLocation STONE_CUTTING_RECIPE_TYPE = new ResourceLocation("stonecutting");
    public static final ResourceLocation SMITHING_TRANSFORM_RECIPE_TYPE = new ResourceLocation("smithing_transform");
    public static final ResourceLocation SMITHING_TRIM_RECIPE_TYPE = new ResourceLocation("smithing_trim");
    public static final ResourceLocation DECORATED_POT_RECIPE_TYPE = new ResourceLocation("decorated_pot");
}
