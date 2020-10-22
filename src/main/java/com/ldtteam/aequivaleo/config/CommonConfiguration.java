package com.ldtteam.aequivaleo.config;

import com.ldtteam.aequivaleo.api.config.AbstractAequivaleoConfiguration;
import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfiguration extends AbstractAequivaleoConfiguration
{

    public ForgeConfigSpec.BooleanValue jsonPrettyPrint;
    public ForgeConfigSpec.IntValue networkBatchingSize;
    public ForgeConfigSpec.BooleanValue debugAnalysisLog;

    public CommonConfiguration(ForgeConfigSpec.Builder builder)
    {

        createCategory(builder, "networking");
        networkBatchingSize = defineInteger(builder, "batch.size", 1000);
        finishCategory(builder);
        createCategory(builder, "analysis");
        createCategory(builder, "log");
        debugAnalysisLog = defineBoolean(builder,"debug", false);
        finishCategory(builder);
        createCategory(builder, "export");
        jsonPrettyPrint = defineBoolean(builder, "json", false);
        finishCategory(builder);
        finishCategory(builder);
    }

}
