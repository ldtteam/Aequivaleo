package com.ldtteam.aequivaleo.api.plugin;

import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
     * Called after Aequivaleos common setup completes.
     * Allows for the registration of static (none world specific) data.
     */
    void onCommonSetup();

    /**
     * Called when the data for a world is being reloaded.
     * Allows for the registration of recipes.
     * @param world The world in question for which the data has been reloaded.
     */
    void onReloadStartedFor(final ServerWorld world);

    /**
     * Called when the data has been recalculated on the server side.
     * @param world The world in question for which the data has been reloaded.
     */
    void onReloadFinishedFor(final ServerWorld world);

    /**
     * Called on the client side to indicate that the data for all worlds
     * has been synced over from the client.
     */
    @OnlyIn(Dist.CLIENT)
    void onDataSynced();
}
