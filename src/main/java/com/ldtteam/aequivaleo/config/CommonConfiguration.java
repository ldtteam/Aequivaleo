package com.ldtteam.aequivaleo.config;

import com.ldtteam.aequivaleo.api.config.AbstractAequivaleoConfiguration;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class CommonConfiguration extends AbstractAequivaleoConfiguration
{

    public ModConfigSpec.BooleanValue jsonPrettyPrint;
    public ModConfigSpec.IntValue networkBatchingSize;
    public ModConfigSpec.BooleanValue debugAnalysisLog;
    public ModConfigSpec.ConfigValue<List<? extends String>> blackListedDimensions;

    public CommonConfiguration(ModConfigSpec.Builder builder)
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
        finishCategory(builder);
        createCategory(builder, "export");
        jsonPrettyPrint = defineBoolean(builder, "json", false);
        finishCategory(builder);
        finishCategory(builder);
    }

}
