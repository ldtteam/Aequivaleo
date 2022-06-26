package com.ldtteam.aequivaleo.api.util;

import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistry;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Supplier;

public final class ModRegistries
{;

    private ModRegistries()
    {
        throw new IllegalStateException("Tried to initialize: ModRegistries but this is a Utility class.");
    }

    public static Supplier<ISyncedRegistry<ICompoundType>>                         COMPOUND_TYPE;
    public static Supplier<IForgeRegistry<ICompoundContainerFactory<?>>> CONTAINER_FACTORY;
    public static Supplier<IForgeRegistry<ICompoundTypeGroup>>           COMPOUND_TYPE_GROUP;
}
