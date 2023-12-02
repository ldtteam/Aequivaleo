package com.ldtteam.aequivaleo.config;


import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForgeConfig;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Mod root configuration.
 */
public class Configuration
{
    /**
     * Loaded clientside, not synced
     */
    private final ModConfig client;
    private final ClientConfiguration clientConfig;
    /**
     * Loaded serverside, synced on connection
     */
    private final ModConfig server;
    private final ServerConfiguration serverConfig;

    /**
     * Loaded on both sides, not synced. Values might differ.
     */
    private final ModConfig common;
    private final CommonConfiguration commonConfig;

    /**
     * Builds configuration tree.
     *
     * @param modContainer from event
     */
    public Configuration(final ModContainer modContainer)
    {
        final Pair<ClientConfiguration, ModConfigSpec> cli = new ModConfigSpec.Builder().configure(ClientConfiguration::new);
        final Pair<ServerConfiguration, ModConfigSpec> ser = new ModConfigSpec.Builder().configure(ServerConfiguration::new);
        final Pair<CommonConfiguration, ModConfigSpec> com = new ModConfigSpec.Builder().configure(CommonConfiguration::new);
        client = new ModConfig(ModConfig.Type.CLIENT, cli.getRight(), modContainer);
        server = new ModConfig(ModConfig.Type.SERVER, ser.getRight(), modContainer);
        common = new ModConfig(ModConfig.Type.COMMON, com.getRight(), modContainer);
        clientConfig = cli.getLeft();
        serverConfig = ser.getLeft();
        commonConfig = com.getLeft();
        modContainer.addConfig(client);
        modContainer.addConfig(server);
        modContainer.addConfig(common);
    }

    public ClientConfiguration getClient()
    {
        return clientConfig;
    }

    public ServerConfiguration getServer()
    {
        return serverConfig;
    }

    public CommonConfiguration getCommon()
    {
        return commonConfig;
    }


}