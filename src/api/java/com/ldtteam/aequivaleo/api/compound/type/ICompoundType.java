package com.ldtteam.aequivaleo.api.compound.type;

import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
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
public interface ICompoundType extends IForgeRegistryEntry<ICompoundType>, Comparable<ICompoundType>
{

    Comparator<ICompoundType> COMPARATOR = Comparator.nullsLast(Comparator.comparing(IForgeRegistryEntry::getRegistryName));

    /**
     * The group this compound type belongs to.
     * Defines the common behaviour of several compound types, and allows for them to be grouped together.
     *
     * @return The group.
     */
    ICompoundTypeGroup getGroup();

    @Override
    default int compareTo(@NotNull ICompoundType o) {
        return COMPARATOR.compare(this, o);
    }
}
