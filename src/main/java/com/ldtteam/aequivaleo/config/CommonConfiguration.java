package com.ldtteam.aequivaleo.config;

import com.ldtteam.aequivaleo.api.config.AbstractAequivaleoConfiguration;
import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfiguration extends AbstractAequivaleoConfiguration
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
