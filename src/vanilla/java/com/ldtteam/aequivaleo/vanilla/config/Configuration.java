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
    private final ModConfig server;
    private final ServerConfiguration serverConfig;

    /**
     * Builds configuration tree.
     *
     * @param modContainer from event
     */
    public Configuration(final ModContainer modContainer)
    {
        final Pair<ServerConfiguration, ForgeConfigSpec> ser = new ForgeConfigSpec.Builder().configure(ServerConfiguration::new);
        server = new ModConfig(ModConfig.Type.SERVER, ser.getRight(), modContainer, "server-vanilla-aequivaleo.toml");
        serverConfig = ser.getLeft();
        modContainer.addConfig(server);
    }

    public ServerConfiguration getServer()
    {
        return serverConfig;
    }

}