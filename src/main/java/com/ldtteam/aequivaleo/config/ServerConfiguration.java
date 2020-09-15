package com.ldtteam.aequivaleo.config;

import com.ldtteam.aequivaleo.api.config.AbstractAequivaleoConfiguration;
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

    protected ServerConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "debugging");
        exportGraph = defineBoolean(builder, "debugging.export.graph", false);
        writeResultsToLog = defineBoolean(builder, "debugging.write.graph", false);
        finishCategory(builder);
        createCategory(builder, "recipes");
        allowNoneSimpleIngredients = defineBoolean(builder, "recipes.ingredients.none-simple", true);
        finishCategory(builder);
    }
}