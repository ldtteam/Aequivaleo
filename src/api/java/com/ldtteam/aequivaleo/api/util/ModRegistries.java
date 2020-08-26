package com.ldtteam.aequivaleo.api.util;

import com.ldtteam.aequivaleo.api.compound.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModRegistries
{
    private ModRegistries()
    {
        throw new IllegalStateException("Tried to initialize: ModRegistries but this is a Utility class.");
    }

    public static IForgeRegistry<ICompoundType> COMPOUND_TYPE;
    public static IForgeRegistry<ICompoundContainerFactory<?>> CONTAINER_FACTORY;
}
