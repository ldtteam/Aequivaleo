package com.ldtteam.aequivaleo.api.registry;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

/**
 * A type of synced registry entries.
 * This is used during sync and allows for the registry entries custom data of this typed to be send over.
 * For each entry a type needs to be added, but types can be reused.
 *
 * @param <T> The type of the registry entry.
 */
public interface ISyncedRegistryEntryType<T extends ISyncedRegistryEntry<T>>
{

    /**
     * The registry name of the type.
     *
     * @return The registry name of the type.
     */
    ResourceLocation getRegistryName();

    /**
     * The codec used during synchronization of the entries with this type.
     *
     * @return The codec used during synchronization of the entries with this type. Return null if not supported!
     */
    default Codec<? extends T> getEntryCodec() {
        return null;
    }

    /**
     * Returns the directory name of the registry entries of this type.
     * By default, this is a combination of the registry namespace and the registry path combined with a slash.
     *
     * Override this method if your registry name is not compatible with this pattern!
     *
     * @return The name of the directory.
     */
    default String getDirectoryName() {
        return getRegistryName().getNamespace() + "/" + getRegistryName().getPath();
    }
}
