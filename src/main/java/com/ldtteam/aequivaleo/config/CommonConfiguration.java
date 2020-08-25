package com.ldtteam.aequivaleo.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfiguration extends AbstractConfiguration
{

    public ForgeConfigSpec.BooleanValue jsonPrettyPrint;

    public ForgeConfigSpec.BooleanValue writeExceptionOnBlockDropFailure;

    public CommonConfiguration(ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "export.json");
        jsonPrettyPrint = defineBoolean(builder, "help.enabled", true);
        finishCategory(builder);
        createCategory(builder, "errorhandling");
        writeExceptionOnBlockDropFailure = defineBoolean(builder, "errorhandling.blocks.print", false);
        finishCategory(builder);
    }

}
