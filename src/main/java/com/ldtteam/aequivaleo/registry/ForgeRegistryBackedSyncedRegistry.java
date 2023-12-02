package com.ldtteam.aequivaleo.registry;

import com.google.common.collect.*;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.registry.IRegistryView;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistry;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistryEntry;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistryEntryType;
import com.ldtteam.aequivaleo.api.util.AequivaleoExtraCodecs;
import com.ldtteam.aequivaleo.network.messages.CompoundTypeSyncedRegistryNetworkPacket;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class ForgeRegistryBackedSyncedRegistry<T extends ISyncedRegistryEntry<T, G>, G extends ISyncedRegistryEntryType<T, G>> implements ISyncedRegistry<T, G>
{
    private final ResourceKey<? extends Registry<T>> backingInternalRegistryKey;
    private final Registry<T> backingInternalRegistry;
    private final Registry<G> backingTypeRegistry;

    private final Codec<T> entryCodec;

    private final BiMap<ResourceLocation, T> syncedEntriesMap = Maps.synchronizedBiMap(HashBiMap.create());
    private final List<T> syncedEntriesList = Collections.synchronizedList(Lists.newArrayList());

    public ForgeRegistryBackedSyncedRegistry(
            final ResourceKey<? extends Registry<T>> backingInternalRegistryKey,
            final Registry<T> backingInternalRegistry,
            final Registry<G> backingTypeRegistry
    ) {
        this.backingInternalRegistryKey = backingInternalRegistryKey;
        this.backingInternalRegistry = backingInternalRegistry;
        this.backingTypeRegistry = backingTypeRegistry;
        this.entryCodec  = this.backingTypeRegistry.byNameCodec().dispatch(
                ISyncedRegistryEntry::getType,
                ISyncedRegistryEntryType::getEntryCodec
        );
    }
    
    @Override
    public Codec<T> getEntryCodec() {
        return this.entryCodec;
    }
    
    @Override
    public Codec<BiMap<ResourceLocation, T>> getCodec()
    {
        return AequivaleoExtraCodecs.bimapOf(
            ResourceLocation.CODEC,
            getEntryCodec()
            );
    }

    @Override
    public T get(final int synchronizationId) {
        if (backingInternalRegistry.size() <= synchronizationId) {
            return syncedEntriesList.get(synchronizationId - backingInternalRegistry.size());
        }

        return backingInternalRegistry.getHolder(synchronizationId).orElseThrow(() -> new IllegalArgumentException("No entry with the synchronization id: " + synchronizationId + " exists in the registry."))
                       .value();
    }

    @Override
    public Optional<T> get(final ResourceLocation key)
    {
        if (backingInternalRegistry.containsKey(key))
        {
            return Optional.ofNullable(backingInternalRegistry.get(key));
        }

        if (syncedEntriesMap.containsKey(key))
        {
            return Optional.ofNullable(syncedEntriesMap.get(key));
        }

        return Optional.empty();
    }
    
    @Override
    public ResourceLocation getKey(T entry) {
        final ResourceLocation registryName = backingInternalRegistry.getKey(entry);
        if (registryName != null) {
            return registryName;
        }
        
        return syncedEntriesMap.inverse().get(entry);
    }
    
    @Override
    public ForgeRegistryBackedSyncedRegistry<T, G> add(final ResourceLocation key, final T entry)
    {
        Objects.requireNonNull(entry);

        final ResourceLocation name = getKey(entry);
        if (name != null)
            throw new IllegalArgumentException("The given entry with the key: %s already exists in the registry.".formatted(name));

        syncedEntriesMap.put(key, entry);
        syncedEntriesList.add(entry);

        return this;
    }

    @Override
    public Set<ResourceLocation> getAllKnownRegistryNames()
    {
        return ImmutableSet.<ResourceLocation>builder().addAll(backingInternalRegistry.keySet()).addAll(syncedEntriesMap.keySet()).build();
    }

    @Override
    public Function<ResourceLocation, ISyncedRegistryEntryType<T, G>> getTypeProducer()
    {
        return backingTypeRegistry::get;
    }

    @Override
    public Stream<T> stream()
    {
        return Stream.concat(
          backingInternalRegistry.stream(),
          syncedEntriesList.stream()
        );
    }
    
    @Override
    public <E> IRegistryView<E> createView(Function<T, Optional<E>> viewFilter) {
        return new ShadowRegistry<>(this, viewFilter);
    }
    
    @Override
    public Set<ISyncedRegistryEntryType<T, G>> getTypes()
    {
        return Sets.newHashSet(backingTypeRegistry);
    }

    @Override
    public List<T> getSyncableEntries()
    {
        return ImmutableList.copyOf(syncedEntriesList);
    }

    @Override
    public void clear()
    {
        syncedEntriesMap.clear();
        syncedEntriesList.clear();
    }

    @Override
    public void synchronizeAll()
    {
        Aequivaleo.getInstance().getNetworkChannel().sendToEveryone(
            new CompoundTypeSyncedRegistryNetworkPacket()
        );
    }

    @Override
    public void synchronizePlayer(final ServerPlayer player)
    {
        Aequivaleo.getInstance().getNetworkChannel().sendToPlayer(
            new CompoundTypeSyncedRegistryNetworkPacket(),
            player
        );
    }

    @Override
    public void forceLoad(final BiMap<ResourceLocation, T> entries)
    {
        clear();
        this.syncedEntriesList.addAll(entries.values());
        this.syncedEntriesMap.putAll(entries);
    }

    @Override
    public @NotNull ResourceKey<? extends Registry<T>> getBackingRegistryKey() {
        return backingInternalRegistryKey;
    }

    @NotNull
    @Override
    public Iterator<T> iterator()
    {
        final List<T> values = ImmutableList.<T>builder()
                                 .addAll(backingInternalRegistry)
                                 .addAll(syncedEntriesList)
                                 .build();
        return values.iterator();
    }
    
    @Override
    public int getId(@NotNull T entry) {
        final int id = this.backingInternalRegistry.getId(entry);
        if (id != -1) {
            return id;
        }
        
        if (!this.syncedEntriesList.contains(entry)) {
            return -1;
        }
        
        return this.backingInternalRegistry.size() + this.syncedEntriesList.indexOf(entry);
    }
    
    @Nullable
    @Override
    public T byId(int p_122651_) {
        if (p_122651_ < this.backingInternalRegistry.size()) {
            return this.backingInternalRegistry.byId(p_122651_);
        }
        
        return this.syncedEntriesList.get(p_122651_ - this.backingInternalRegistry.size());
    }
    
    @Override
    public int size() {
        return this.backingInternalRegistry.size() + this.syncedEntriesList.size();
    }
}
