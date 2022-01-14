package com.ldtteam.aequivaleo.api.registry;

import net.minecraft.resources.ResourceLocation;

/**
 * An entry in a registry with a given name.
 */
public interface IRegistryEntry
{
    /**
     * The name of the entry in the registry.
     *
     * @return The name of the entry.
     */
    ResourceLocation getRegistryName();
}
