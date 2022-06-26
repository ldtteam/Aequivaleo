package com.ldtteam.aequivaleo.api.util;

import com.ldtteam.aequivaleo.api.registry.ISyncedRegistryEntry;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistryEntryType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

public final class RegistryUtils
{

    private RegistryUtils()
    {
        throw new IllegalStateException("Tried to initialize: RegistryUtils but this is a Utility class.");
    }

    public static <T> RegistryBuilder<T> makeRegistry(Class<T> type) {
        return new RegistryBuilder<T>()
                 .setIDRange(1, Integer.MAX_VALUE - 1);
    }

    public static <T extends ISyncedRegistryEntry<T>, G extends ISyncedRegistryEntryType<T>> RegistryBuilder<T> makeSyncedRegistry(Class<T> type, Supplier<IForgeRegistry<G>> typeRegistrySupplier) {
        Codec<ISyncedRegistryEntryType<T>> serializerCodec = ResourceLocation.CODEC
                .comapFlatMap(
                        name -> {
                            if (typeRegistrySupplier.get().containsKey(name))
                                return DataResult.success(typeRegistrySupplier.get().getValue(name));
                            return DataResult.error("Object " + name + " not present in the registry");
                        },
                        ISyncedRegistryEntryType::getRegistryName
                );
        final Codec<T> entryCodec  = serializerCodec.dispatch(
                ISyncedRegistryEntry::getType,
                ISyncedRegistryEntryType::getEntryCodec
        );

        return new RegistryBuilder<T>()
                .setIDRange(1, Integer.MAX_VALUE - 1);
    }

    public static <T> ForgeRegistry<T> getFull(final ResourceKey<? extends Registry<T>> key) {
        return RegistryManager.ACTIVE.getRegistry(key);
    }

    public static <T> int getId(final IForgeRegistry<T> registry, final T instance) {
        return ((ForgeRegistry<T>) registry).getID(instance);
    }
}
