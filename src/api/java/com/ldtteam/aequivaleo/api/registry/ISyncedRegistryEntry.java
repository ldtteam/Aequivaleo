package com.ldtteam.aequivaleo.api.registry;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents a dynamic synced registry entry.
 *
 * @param <T> The type of the registry entry.
 */
public interface ISyncedRegistryEntry<T extends ISyncedRegistryEntry<T, G>, G extends ISyncedRegistryEntryType<T, G>>
{

    /**
     * The type of the entry, used during synchronization.
     *
     * @return The type of the entry.
     */
    G getType();
}
