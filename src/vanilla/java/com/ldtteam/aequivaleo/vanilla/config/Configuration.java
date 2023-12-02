package com.ldtteam.aequivaleo.vanilla.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
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
        final Pair<CommonConfiguration, ModConfigSpec> ser = new ModConfigSpec.Builder().configure(CommonConfiguration::new);
        common = new ModConfig(ModConfig.Type.COMMON, ser.getRight(), modContainer, "vanilla-aequivaleo.toml");
        commonConfiguration = ser.getLeft();
        modContainer.addConfig(common);
    }

    public CommonConfiguration getCommon()
    {
        return commonConfiguration;
    }

}