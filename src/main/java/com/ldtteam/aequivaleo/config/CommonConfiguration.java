package com.ldtteam.aequivaleo.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfiguration extends AbstractConfiguration
{

    public ForgeConfigSpec.BooleanValue jsonPrettyPrint;
    public ForgeConfigSpec.IntValue networkBatchingSize;

    public CommonConfiguration(ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "export.json");
        jsonPrettyPrint = defineBoolean(builder, "help.enabled", true);
        finishCategory(builder);
        createCategory(builder, "networking");
        networkBatchingSize = defineInteger(builder, "batch.size", 100);
        finishCategory(builder);
    }

}
