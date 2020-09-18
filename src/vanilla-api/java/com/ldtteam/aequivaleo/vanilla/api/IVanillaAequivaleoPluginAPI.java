package com.ldtteam.aequivaleo.vanilla.api;

import com.ldtteam.aequivaleo.vanilla.api.tags.ITagEquivalencyRegistry;

/**
 * The API for the vanilla aequivaleo api.
 * Retrieved via an IMC with a callback or via its {@link #getInstance()} method.
 */
public interface IVanillaAequivaleoPluginAPI
{

    /**
     * Returns the instance of the api, once it has been initialized.
     * (Initialization happens during mod construction)
     *
     * @return The api.
     */
    static IVanillaAequivaleoPluginAPI getInstance() {
        return IVanillaAequivaleoPluginAPI.Holder.getInstance();
    }

    /**
     * Gives access to a registry which handles equivalencies via tags.
     * @return The registry which allows the analysis engine to take tags into account.
     */
    ITagEquivalencyRegistry getTagEquivalencyRegistry();

    class Holder {
        private static IVanillaAequivaleoPluginAPI apiInstance;

        public static IVanillaAequivaleoPluginAPI getInstance()
        {
            return apiInstance;
        }

        public static void setInstance(final IVanillaAequivaleoPluginAPI instance)
        {
            if (apiInstance != null)
                throw new IllegalStateException("Can not setup API twice!");

            apiInstance = instance;
        }
    }
}
