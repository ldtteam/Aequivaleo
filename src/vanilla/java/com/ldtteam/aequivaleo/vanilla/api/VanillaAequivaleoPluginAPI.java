package com.ldtteam.aequivaleo.vanilla.api;

import com.ldtteam.aequivaleo.vanilla.api.tags.ITagEquivalencyRegistry;
import com.ldtteam.aequivaleo.vanilla.tags.TagEquivalencyRegistry;

public class VanillaAequivaleoPluginAPI implements IVanillaAequivaleoPluginAPI
{

    private static final VanillaAequivaleoPluginAPI INSTANCE = new VanillaAequivaleoPluginAPI();

    public static VanillaAequivaleoPluginAPI getInstance()
    {
        return INSTANCE;
    }

    @Override
    public ITagEquivalencyRegistry getTagEquivalencyRegistry()
    {
        return TagEquivalencyRegistry.getInstance();
    }

    private VanillaAequivaleoPluginAPI()
    {
    }
}
