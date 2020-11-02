package com.ldtteam.aequivaleo.config;

import com.ldtteam.aequivaleo.api.config.AbstractAequivaleoConfiguration;
import com.ldtteam.aequivaleo.utils.IngredientLogLevel;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Mod server configuration.
 * Loaded serverside, synced on connection.
 */
public class ServerConfiguration extends AbstractAequivaleoConfiguration
{
    public ForgeConfigSpec.BooleanValue exportGraph;
    public ForgeConfigSpec.BooleanValue writeResultsToLog;
    public ForgeConfigSpec.BooleanValue allowNoneSimpleIngredients;
    public ForgeConfigSpec.EnumValue<IngredientLogLevel> ingredientLogLevelEnumValue;
    public ForgeConfigSpec.IntValue maxCacheFilesToKeep;

    protected ServerConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "debugging");
        exportGraph = defineBoolean(builder, "debugging.export.graph", false);
        writeResultsToLog = defineBoolean(builder, "debugging.write.graph", false);
        finishCategory(builder);
        createCategory(builder, "recipes");
        ingredientLogLevelEnumValue = defineEnum(builder, "recipes.ingredients.error.logging", IngredientLogLevel.FULL);
        allowNoneSimpleIngredients = defineBoolean(builder, "recipes.ingredients.none-simple", true);
        finishCategory(builder);
        createCategory(builder, "cache");
        maxCacheFilesToKeep = defineInteger(builder, "cache.max", 5, 1, Integer.MAX_VALUE);
        finishCategory(builder);
    }
}