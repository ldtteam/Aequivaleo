package com.ldtteam.aequivaleo.api.compound;

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
}
