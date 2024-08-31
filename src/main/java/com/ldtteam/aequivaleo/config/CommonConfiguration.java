package com.ldtteam.aequivaleo.config;

import com.ldtteam.aequivaleo.api.config.AbstractAequivaleoConfiguration;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.List;

public class CommonConfiguration extends AbstractAequivaleoConfiguration
{

    public ForgeConfigSpec.BooleanValue jsonPrettyPrint;
    public ForgeConfigSpec.IntValue networkBatchingSize;
    public ForgeConfigSpec.BooleanValue debugAnalysisLog;
    public ForgeConfigSpec.ConfigValue<List<? extends String>> blackListedDimensions;
    public ForgeConfigSpec.BooleanValue traceCycleLog;
    public ForgeConfigSpec.BooleanValue debugCycleLog;
    public ForgeConfigSpec.BooleanValue outputCycleCount;

    public CommonConfiguration(ForgeConfigSpec.Builder builder)
    {

        createCategory(builder, "networking");
        networkBatchingSize = defineInteger(builder, "batch.size", 1000);
        finishCategory(builder);
        createCategory(builder, "analysis");
        createCategory(builder, "dimensions");
        blackListedDimensions = defineList(builder, "blacklist", Collections.emptyList(), s -> s instanceof String);
        finishCategory(builder);
        createCategory(builder, "log");
        debugAnalysisLog = defineBoolean(builder,"debug", false);
        traceCycleLog = defineBoolean(builder, "cycle.trace", false);
        debugCycleLog = defineBoolean(builder, "cycle.full", false);
        outputCycleCount = defineBoolean(builder, "cycle.count", true);
        finishCategory(builder);
        createCategory(builder, "export");
        jsonPrettyPrint = defineBoolean(builder, "json", false);
        finishCategory(builder);
        finishCategory(builder);
    }

}
