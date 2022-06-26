package com.ldtteam.aequivaleo.vanilla.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Mod root configuration.
 */
public class Configuration
{
    /**
     * Loaded serverside, synced on connection
     */
    private final ModConfig common;
    private final CommonConfiguration commonConfiguration;

    /**
     * Builds configuration tree.
     *
     * @param modContainer from event
     */
    public Configuration(final ModContainer modContainer)
    {
        final Pair<CommonConfiguration, ForgeConfigSpec> ser = new ForgeConfigSpec.Builder().configure(CommonConfiguration::new);
        common = new ModConfig(ModConfig.Type.COMMON, ser.getRight(), modContainer, "vanilla-aequivaleo.toml");
        commonConfiguration = ser.getLeft();
        modContainer.addConfig(common);
    }

    public CommonConfiguration getCommon()
    {
        return commonConfiguration;
    }

}