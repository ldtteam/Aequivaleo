package com.ldtteam.aequivaleo.api.compound.type.group;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.mediation.IMediationEngine;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.results.IResultsInformationCache;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
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
     * The mediation engine.
     *
     * @return The mediation engine.
     */
    @NotNull
    IMediationEngine getMediationEngine();

    /**
     * Allows the group to indicate if an incomplete recipe is allowed to process compounds of this group.
     *
     * @param recipe The recipe in question.
     *
     * @return True to allow, false to disallow.
     */
    boolean shouldIncompleteRecipeBeProcessed(@NotNull final IEquivalencyRecipe recipe);

    /**
     * Indicates if the given instance is allowed to contribute to a given recipe.
     *
     * @param compoundInstance The instance that should be contribute or not.
     * @param recipe The recipe in question.
     *
     * @return True when the compound instance should contribute, false when not.
     */
    boolean canContributeToRecipeAsInput(IEquivalencyRecipe recipe, CompoundInstance compoundInstance);

    /**
     * Indicates if the given instance is allowed to contribute from a given recipe.
     *
     * @param recipe The recipe in question.
     * @param compoundInstance The instance that should contribute or not.
     *
     * @return True when the compound instance should contribute, false when not.
     */
    boolean canContributeToRecipeAsOutput(IEquivalencyRecipe recipe, CompoundInstance compoundInstance);

    /**
     * Indicates if the given compound instance is valid for the compound container.
     *
     * @param wrapper The container.
     * @param compoundInstance The instance.
     *
     * @return True when instance is valid, false when not.
     */
    boolean isValidFor(ICompoundContainer<?> wrapper, CompoundInstance compoundInstance);

    @Override
    default int compareTo(@NotNull ICompoundTypeGroup group) {
        return Objects.requireNonNull(getRegistryName()).compareTo(group.getRegistryName());
    }

    /**
     * Invoked by an instance of {@link IResultsInformationCache} to store an additional cache value.
     * Warning this method is invoked in parallel in almost all cases.
     *
     * @param instances The instances to convert into a cache entry.
     *
     * @return An optional that indicates the cache value to store. Return an empty optional to not store any cache value.
     */
    default Optional<?> convertToCacheEntry(final Set<CompoundInstance> instances) { return Optional.empty(); }
}
