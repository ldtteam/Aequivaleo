package com.ldtteam.aequivaleo.api.util;

import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistry;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Supplier;

/**
 * Defines the registries that are used by the mod.
 */
public final class ModRegistries
{;

    private ModRegistries()
    {
        throw new IllegalStateException("Tried to initialize: ModRegistries but this is a Utility class.");
    }

    /**
     * The registry for compound types.
     * <p>
     *     This is a custom kind of registry, as it is synced between client and server.
     *     The servers datapack drives the content of this registry.
     * </p>
     *
     */
    public static Supplier<ISyncedRegistry<ICompoundType>>                         COMPOUND_TYPE;

    /**
     * The registry for compound container types.
     */
    public static Supplier<IForgeRegistry<ICompoundContainerFactory<?>>> CONTAINER_FACTORY;

    /**
     * The registry for compound type groups.
     */
    public static Supplier<IForgeRegistry<ICompoundTypeGroup>>           COMPOUND_TYPE_GROUP;
}
