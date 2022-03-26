package com.ldtteam.aequivaleo.registry;

import com.google.common.collect.*;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.registry.*;
import com.ldtteam.aequivaleo.network.messages.CompoundTypeSyncedRegistryNetworkPacket;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ForgeRegistryBackedSyncedRegistry<T extends IForgeRegistryEntry<T> & ISyncedRegistryEntry<T>, G extends ISyncedRegistryEntryType<T> & IForgeRegistryEntry<G>> implements ISyncedRegistry<T>
{
    private final Supplier<IForgeRegistry<T>> backingInternalRegistry;
    private final Supplier<IForgeRegistry<G>> backingTypeRegistry;

    private final Codec<T> entryCodec;

    private final BiMap<ResourceLocation, T> syncedEntriesMap = Maps.synchronizedBiMap(HashBiMap.create());
    private final List<T> syncedEntriesList = Collections.synchronizedList(Lists.newArrayList());

    public ForgeRegistryBackedSyncedRegistry(
      final Supplier<IForgeRegistry<T>> backingInternalRegistry,
      final Supplier<IForgeRegistry<G>> backingTypeRegistry
    ) {
        this.backingInternalRegistry = backingInternalRegistry;
        this.backingTypeRegistry = backingTypeRegistry;

        Codec<ISyncedRegistryEntryType<T>> serializerCodec = ResourceLocation.CODEC
          .comapFlatMap(
            name -> {
                if (backingTypeRegistry.get().containsKey(name))
                    return DataResult.success(backingTypeRegistry.get().getValue(name));
                return DataResult.error("Object " + name + " not present in the registry");
            },
            ISyncedRegistryEntryType::getRegistryName
          );
        this.entryCodec  = serializerCodec.dispatch(
          ISyncedRegistryEntry::getType,
          ISyncedRegistryEntryType::getEntryCodec
        );
    }

    @Override
    public Codec<List<T>> getCodec()
    {
        return this.entryCodec.listOf();
    }

    @Override
    public int getSynchronizationIdOf(final T entry)
    {
        if (backingInternalRegistry.get().containsKey(entry.getRegistryName())) {
            return ((ForgeRegistry<T>) backingInternalRegistry.get()).getID(entry);
        }

        return this.syncedEntriesList.indexOf(entry);
    }

    @Override
    public Optional<T> get(final ResourceLocation key)
    {
        if (backingInternalRegistry.get().containsKey(key))
        {
            return Optional.ofNullable(backingInternalRegistry.get().getValue(key));
        }

        if (syncedEntriesMap.containsKey(key))
        {
            return Optional.ofNullable(syncedEntriesMap.get(key));
        }

        return Optional.empty();
    }

    @Override
    public ISyncedRegistry<T> add(final T entry)
    {
        Validate.notNull(entry);

        final ResourceLocation name = entry.getRegistryName();
        Validate.notNull(name);

        if (get(name).isPresent())
            throw new IllegalArgumentException("The given entry with the key: " + entry.getRegistryName() + " already exists in the registry.");

        syncedEntriesMap.put(name, entry);
        syncedEntriesList.add(entry);

        return this;
    }

    @Override
    public Set<ResourceLocation> getAllKnownRegistryNames()
    {
        return ImmutableSet.<ResourceLocation>builder().addAll(backingInternalRegistry.get().getKeys()).addAll(syncedEntriesMap.keySet()).build();
    }

    @Override
    public Function<ResourceLocation, ISyncedRegistryEntryType<T>> getTypeProducer()
    {
        return backingTypeRegistry.get()::getValue;
    }

    @Override
    public Stream<T> stream()
    {
        return Stream.concat(
          backingInternalRegistry.get().getValues().stream(),
          syncedEntriesList.stream()
        );
    }

    @Override
    public void forEach(final BiConsumer<ResourceLocation, T> consumer)
    {
        backingInternalRegistry.get().forEach(
          t -> consumer.accept(t.getRegistryName(), t)
        );
        syncedEntriesMap.forEach(consumer);
    }

    @Override
    public <E extends IRegistryEntry> IRegistryView<E> createView(Function<T, Optional<E>> viewFilter)
    {
        return new ShadowRegistry<>(this, viewFilter);
    }

    @Override
    public Set<ISyncedRegistryEntryType<T>> getTypes()
    {
        return Sets.newHashSet(backingTypeRegistry.get().getValues());
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
    public void forceLoad(final List<T> entries)
    {
        clear();
        this.syncedEntriesList.addAll(entries);
        this.syncedEntriesList.forEach(entry -> this.syncedEntriesMap.put(entry.getRegistryName(), entry));
    }

    @NotNull
    @Override
    public Iterator<T> iterator()
    {
        final List<T> values = ImmutableList.<T>builder()
                                 .addAll(backingInternalRegistry.get().getValues())
                                 .addAll(syncedEntriesList)
                                 .build();
        return values.iterator();
    }
}
