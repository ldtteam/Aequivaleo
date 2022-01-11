package com.ldtteam.aequivaleo.api.plugin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Represents a plugin for Aequivaleo.
 *
 * Plugins have callbacks that can be invoked by aequivaleo.
 * See their documentation for more information.
 *
 * All methods are potentially invoked in parallel with other plugins, or even aequivaleo itself.
 */
public interface IAequivaleoPlugin
{
    /**
     * The id of the plugin.
     * Has to be unique over all plugins.
     *
     * @return The id.
     */
    String getId();

    /**
     * Invoked when the plugin is constructed.
     */
    default void onConstruction() {};

    /**
     * Called after Aequivaleos common setup completes.
     * Allows for the registration of static (none world specific) data.
     */
    default void onCommonSetup() {};

    /**
     * Called when the data for a world is being reloaded.
     * Allows for the registration of recipes.
     * @param world The world in question for which the data has been reloaded.
     */
    default void onReloadStartedFor(final ServerLevel world) {};

    /**
     * Called when the data has been recalculated on the server side.
     * @param world The world in question for which the data has been reloaded.
     */
    default void onReloadFinishedFor(final ServerLevel world) {};

    /**
     * Called on the client side to indicate that the data for all worlds
     * has been synced over from the client.
     *
     * @param worldRegistryKey The registry
     */
    @OnlyIn(Dist.CLIENT)
    default void onDataSynced(final ResourceKey<Level> worldRegistryKey) {};

    /**
     * Invoked when the compound type synced registry has been synced to the client.
     */
    default void onCompoundTypeRegistrySync() {};
}
