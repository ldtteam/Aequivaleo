package com.ldtteam.aequivaleo.api.compound.type;

import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistryEntry;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistryEntryType;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * Represents a single compound that makes up part of the world.
 * Examples:
 *   * Air
 *   * Fire
 *   * Earth
 *   * Magic
 *   * Energy
 *   * etc.
 */
public interface ICompoundType extends Comparable<ICompoundType>, ISyncedRegistryEntry<ICompoundType, ICompoundTypeGroup>
{
    
    /**
     * Default comparator for compound types.
     * Sorts based on name, allows for a reproducible order.
     */
    Comparator<ICompoundType> COMPARATOR = Comparator.nullsLast(Comparator.comparing((type) -> ModRegistries.COMPOUND_TYPE.getKey(type)));

    /**
     * The group this compound type belongs to.
     * Defines the common behaviour of several compound types, and allows for them to be grouped together.
     *
     * @return The group.
     */
    ICompoundTypeGroup getGroup();

    @Override
    default ICompoundTypeGroup getType() {
        return getGroup();
    }

    @Override
    default int compareTo(@NotNull ICompoundType o) {
        return COMPARATOR.compare(this, o);
    }
}
