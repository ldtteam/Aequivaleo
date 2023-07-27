package com.ldtteam.aequivaleo.api.util;

import net.minecraft.resources.ResourceLocation;

public final class Constants
{

    private Constants()
    {
        throw new IllegalStateException("Tried to initialize: Constants but this is a Utility class.");
    }

    public static final String MOD_ID = "aequivaleo";
    public static final String MOD_NAME = "Aequivaleo";
    public static final String MOD_VERSION = "%VERSION%";

    public static final ResourceLocation SIMPLE_INGREDIENT = new ResourceLocation(Constants.MOD_ID, "simple");
    public static final ResourceLocation TAG_INGREDIENT = new ResourceLocation(Constants.MOD_ID, "tag");
}
