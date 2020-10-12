package com.ldtteam.aequivaleo.api.compound.type.group;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a group of compound types which behave all the same during analysis.
 * Examples:
 *   * Goo
 *   * Equivalencies aspects
 *   * EMC.
 */
public interface ICompoundTypeGroup extends IForgeRegistryEntry<ICompoundTypeGroup>, Comparable<ICompoundTypeGroup>
{

    /**
     * Callback used to determine which value variant should be taken if multiple calculation results are possible.
     *
     * @param candidates The candidate values to decide between.
     *
     * @return The set of chosen compound instances. Or an empty set, to indicate that this node should not be processed.
     */
    Set<CompoundInstance> determineResult(Set<Set<CompoundInstance>> candidates);

    /**
     * Indicates if the given instance is allowed to contribute to a given recipe.
     *
     * @param compoundInstance The instance that should be contribute or not.
     * @param recipe The recipe in question.
     *
     * @return True when the compound instance should contribute, false when not.
     */
    boolean canContributeToRecipeAsInput(CompoundInstance compoundInstance, IEquivalencyRecipe recipe);

    /**
     * Indicates if the given instance is allowed to contribute from a given recipe.
     *
     * @param wrapper The target object.
     * @param recipe The recipe in question.
     * @param simpleCompoundInstance The instance that should contribute or not.
     *
     * @return True when the compound instance should contribute, false when not.
     */
    boolean canContributeToRecipeAsOutput(ICompoundContainer<?> wrapper, IEquivalencyRecipe recipe, CompoundInstance simpleCompoundInstance);

    /**
     * Indicates if the given compound instance is valid for the compound container.
     *
     * @param wrapper The container.
     * @param simpleCompoundInstance The instance.
     *
     * @return True when instance is valid, false when not.
     */
    boolean isValidFor(ICompoundContainer<?> wrapper, CompoundInstance simpleCompoundInstance);

    @Override
    default int compareTo(@NotNull ICompoundTypeGroup iCompoundTypeGroup) {
        return Objects.requireNonNull(getRegistryName()).compareTo(iCompoundTypeGroup.getRegistryName());
    }
}
