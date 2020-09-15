package com.ldtteam.aequivaleo.api.plugin;

import com.google.common.collect.ImmutableSet;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;

/**
 * The manager for plugins.
 */
public interface IAequivaleoPluginManager
{

    /**
     * The instance of the plugin manager.
     *
     * @return The plugin manager.
     */
    static IAequivaleoPluginManager getInstance() {
        return IAequivaleoAPI.getInstance().getPluginManager();
    }

    /**
     * Gets the plugins.
     *
     * @return An immutable set with the plugins.
     */
    ImmutableSet<IAequivaleoPlugin> getPlugins();
}
