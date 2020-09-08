package com.ldtteam.aequivaleo.api.compound.type;

import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import net.minecraftforge.registries.IForgeRegistryEntry;

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
public interface ICompoundType extends IForgeRegistryEntry<ICompoundType>, Comparable<ICompoundType>
{
    /**
     * The group this compound type belongs to.
     * Defines the common behaviour of several compound types, and allows for them to be grouped together.
     *
     * @return The group.
     */
    ICompoundTypeGroup getGroup();
}
