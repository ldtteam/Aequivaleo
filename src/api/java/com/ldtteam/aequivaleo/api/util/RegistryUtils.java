package com.ldtteam.aequivaleo.api.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.*;

public final class RegistryUtils
{

    private RegistryUtils()
    {
        throw new IllegalStateException("Tried to initialize: RegistryUtils but this is a Utility class.");
    }

    public static <T extends IForgeRegistryEntry<T>> RegistryBuilder<T> makeRegistry(String name, Class<T> type) {
        return new RegistryBuilder<T>()
                 .setName(new ResourceLocation(Constants.MOD_ID, name))
                 .setIDRange(1, Integer.MAX_VALUE - 1)
                 .disableSaving()
                 .setType(type);
    }

    public static <T extends IForgeRegistryEntry<T>> ForgeRegistry<T> getFull(final Class<T> clz) {
        return (ForgeRegistry<T>) RegistryManager.ACTIVE.getRegistry(clz);
    }

    public static <T extends IForgeRegistryEntry<T>> int getId(final IForgeRegistry<T> registry, final T instance) {
        return ((ForgeRegistry<T>) registry).getID(instance);
    }
}
