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
    public static final ResourceLocation SMITHING_RECIPE_TYPE = new ResourceLocation("smithing");
}
