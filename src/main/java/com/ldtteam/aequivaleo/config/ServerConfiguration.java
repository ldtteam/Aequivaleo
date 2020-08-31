package com.ldtteam.aequivaleo.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Mod server configuration.
 * Loaded serverside, synced on connection.
 */
public class ServerConfiguration extends AbstractConfiguration
{
    public ForgeConfigSpec.BooleanValue exportGraph;

    protected ServerConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "debugging");
        exportGraph = defineBoolean(builder, "debugging.export.graph", false);
        finishCategory(builder);
    }
}