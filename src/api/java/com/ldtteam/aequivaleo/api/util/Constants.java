package com.ldtteam.aequivaleo.api.util;

import net.minecraft.resources.ResourceLocation;

/**
 * Utility class that defines constants used by Aequivaleo.
 */
public final class Constants
{

    private Constants()
    {
        throw new IllegalStateException("Tried to initialize: Constants but this is a Utility class.");
    }

    /**
     * The mod id of Aequivaleo.
     */
    public static final String MOD_ID = "aequivaleo";

    /**
     * The id of a simple ingredient
     */
    public static final ResourceLocation SIMPLE_INGREDIENT = new ResourceLocation(Constants.MOD_ID, "simple");

    /**
     * The id of a tag ingredient
     */
    public static final ResourceLocation TAG_INGREDIENT = new ResourceLocation(Constants.MOD_ID, "tag");
}
