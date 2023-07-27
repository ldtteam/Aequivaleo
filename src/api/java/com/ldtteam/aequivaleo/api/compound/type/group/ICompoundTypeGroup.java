package com.ldtteam.aequivaleo.api.compound.type.group;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.mediation.IMediationEngine;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistryEntryType;
import com.ldtteam.aequivaleo.api.results.IEquivalencyResults;
import com.ldtteam.aequivaleo.api.results.IResultsInformationCache;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
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
public interface ICompoundTypeGroup extends Comparable<ICompoundTypeGroup>, ISyncedRegistryEntryType<ICompoundType>
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

    /**
     * Allows for the adaptation of the candidate data that is produced by the given recipe.
     * This allows for the implementation of a lossy crafting mechanic.
     *
     * @param recipe The recipe in question.
     * @param candidateData The data calculated by the analyzer that is about to be added to an output.
     * @return The data that should be added to the output.
     */
    default Collection<CompoundInstance> adaptRecipeResult(final IEquivalencyRecipe recipe, final Collection<CompoundInstance> candidateData) {
        return candidateData;
    }

    @Override
    default int compareTo(@NotNull ICompoundTypeGroup group) {
        return Objects.requireNonNull(getRegistryName()).compareTo(Objects.requireNonNull(group.getRegistryName()));
    }

    /**
     * Invoked by an instance of {@link IResultsInformationCache} to store an additional cache value.
     * Warning this method is invoked in parallel in almost all cases.
     *
     * @param instances The instances to convert into a cache entry.
     *
     * @return An optional that indicates the cache value to store. Return an empty optional to not store any cache value.
     */
    @Deprecated
    default Optional<?> convertToCacheEntry(final Set<CompoundInstance> instances) { return Optional.empty(); }

    /**
     * Invoked by an instance of {@link IResultsInformationCache} to store an additional cache value.
     * Warning this method is invoked in parallel in almost all cases.
     *
     * @param container The container for which the instances are converted.
     * @param instances The instances to convert into a cache entry.
     *
     * @return An optional that indicates the cache value to store. Return an empty optional to not store any cache value.
     */
    @Deprecated
    default Optional<?> convertToCacheEntry(final ICompoundContainer<?> container, final Set<CompoundInstance> instances) { return convertToCacheEntry(instances); }

    /**
     * Invoked by an instance of {@link IEquivalencyResults} to store an additional mapped value.
     * Warning this method is invoked in parallel in almost all cases.
     *
     * @param instances The instances to convert into a mapped entry.
     *
     * @return An optional that indicates the mapped value to store. Return an empty optional to not store any mapped value.
     */
    default Optional<?> mapEntry(final Set<CompoundInstance> instances) { return convertToCacheEntry(instances); }

    /**
     * Invoked by an instance of {@link IEquivalencyResults} to store an additional mapped value.
     * Warning this method is invoked in parallel in almost all cases.
     *
     * @param container The container for which the instances are converted.
     * @param instances The instances to convert into a mapped entry.
     *
     * @return An optional that indicates the mapped value to store. Return an empty optional to not store any mapped value.
     */
    default Optional<?> mapEntry(final ICompoundContainer<?> container, final Set<CompoundInstance> instances) { return convertToCacheEntry(instances); }
}
