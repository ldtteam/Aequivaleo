package com.ldtteam.aequivaleo.api.registry;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents a dynamic synced registry entry.
 *
 * @param <T> The type of the registry entry.
 */
public interface ISyncedRegistryEntry<T extends ISyncedRegistryEntry<T>>
{

    /**
     * The type of the entry, used during synchronization.
     *
     * @return The type of the entry.
     */
    ISyncedRegistryEntryType<T> getType();

    /**
     * The name of the entry in the registry.
     *
     * @return The name of the entry.
     */
    ResourceLocation getRegistryName();
}
