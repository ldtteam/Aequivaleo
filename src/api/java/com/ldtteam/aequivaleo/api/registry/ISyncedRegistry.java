package com.ldtteam.aequivaleo.api.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Represents a registry that is synced with the client.
 *
 * @param <T> The type of the entry in the registry.
 */
public interface ISyncedRegistry<T extends ISyncedRegistryEntry<T>> extends IRegistryView<T>
{

    /**
     * Gets the codec for the registry.
     *
     * @return The code which is used synchronize and read and write the registry.
     */
    Codec<List<T>> getCodec();

    /**
     * Gets the given entries' registry name.
     *
     * @param entry The entry to get the registry name of.
     * @return The registry name of the entry.
     */
    @Override
    default ResourceLocation getRegistryNameOf(final T entry) {
        return entry.getRegistryName();
    }

    /**
     * Gets the synchronization id of the entry in this registry.
     * This id is unique to the entry in the registry, in the current running instance.
     * This means that the id can not be used to save the entry to disk, but can only be used during network synchronization.
     *
     * @param entry The entry to get the id from.
     * @return The id of the entry.
     */
    int getSynchronizationIdOf(final T entry);

    /**
     * Gets the entry with the given synchronization id.
     *
     * @param synchronizationId The synchronization id of the entry to get.
     * @return The entry with the given synchronization id.
     */
    T get(int synchronizationId);

    /**
     * Adds a new entry to the registry.
     *
     * @param entry The entry to add.
     * @return The registry invoked after the entry was added.
     */
    ISyncedRegistry<T> add(final T entry);

    /**
     * Gives access to all known registry names of entries in this registry.
     *
     * @return A set of all known registry names of entries in this registry.
     */
    Set<ResourceLocation> getAllKnownRegistryNames();

    /**
     * Producer which returns an entry type for a given registry name of the type.
     *
     * @return The type name to type producer. Useful for serialization handling.
     */
    Function<ResourceLocation, ISyncedRegistryEntryType<T>> getTypeProducer();

    /**
     * Returns all types which are known to this synced registry.
     * The types have to be statically registered for the registry to work.
     *
     * @return A set of all known types.
     */
    Set<ISyncedRegistryEntryType<T>> getTypes();

    /**
     * Returns a list of all syncable entries in this registry.
     *
     * @return The list of all syncable entries in this registry.
     */
    List<T> getSyncableEntries();

    /**
     * Clears the synced registry of entries.
     */
    void clear();

    /**
     * Synchronizes the data to all currently connected clients.
     */
    void synchronizeAll();

    /**
     * Synchronizes the data to the given player.
     *
     * @param player The player to send the data to.
     */
    void synchronizePlayer(ServerPlayer player);

    /**
     * Force loads the registry from the data entries given.
     *
     * @param entries The entries that should make up the registry.
     */
    void forceLoad(List<T> entries);

    /**
     * The registry key of the backing static registry.
     *
     * @return The registry key of the backing static registry.
     */
    @NotNull
    ResourceKey<? extends Registry<T>> getBackingRegistryKey();
}
