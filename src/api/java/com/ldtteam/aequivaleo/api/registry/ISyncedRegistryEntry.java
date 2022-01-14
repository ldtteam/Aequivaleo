package com.ldtteam.aequivaleo.api.registry;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents a dynamic synced registry entry.
 *
 * @param <T> The type of the registry entry.
 */
public interface ISyncedRegistryEntry<T extends ISyncedRegistryEntry<T>> extends IRegistryEntry
{

    /**
     * The type of the entry, used during synchronization.
     *
     * @return The type of the entry.
     */
    ISyncedRegistryEntryType<T> getType();
}
