package com.ldtteam.aequivaleo.vanilla.api.util;

import net.minecraft.util.ResourceLocation;

public final class Constants
{

    private Constants()
    {
        throw new IllegalStateException("Tried to initialize: Constants but this is a Utility class.");
    }

    public static final ResourceLocation SIMPLE_RECIPE_TYPE = new ResourceLocation("simple");
    public static final ResourceLocation COOKING_RECIPE_TYPE = new ResourceLocation("cooking");
}
