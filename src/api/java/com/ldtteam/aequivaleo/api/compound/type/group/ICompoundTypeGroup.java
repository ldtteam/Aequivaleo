package com.ldtteam.aequivaleo.api.compound.type.group;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
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
     * Callback used to determine which value variant should be taken if multiple calculation results are possible.
     *
     * @param candidates The candidate values to decide between.
     * @param complete Indicates if the node is completely analyzed when a decision needs to be made. True when all information is known.
     * @param hasInvalidIngredients Indicates if this node has invalid (aka nodes that did not have an initial value or where calculated using invalid nodes) parents.
     *
     * @implNote This method is the new endpoint for result mediation and should be overridden and implemented instead of
     *           {@link #determineResult(Set, Boolean)}. If the implementer overrides this method, then he should throw an
     *           {@link UnsupportedOperationException} when the other method is called, up until the moment the other method is
     *           removed from the api.
     *
     * @return The set of chosen compound instances. Or an empty set, to indicate that this node should not be processed.
     */
    default Optional<Set<CompoundInstance>> determineResult(Set<Set<CompoundInstance>> candidates, final Boolean complete, final Boolean hasInvalidIngredients) {
        return Optional.ofNullable(this.determineResult(candidates, complete));
    }

    /**
     * Callback used to determine which value variant should be taken if multiple calculation results are possible.
     *
     * @param candidates The candidate values to decide between.
     * @param complete Indicates if the node is completely analyzed when a decision needs to be made. True when all information is known.
     *
     * @deprecated This method has been replaced from a functional standpoint by {@link #determineResult(Set, Boolean, Boolean)}.
     *             If the implementer is newly implementing this interface, he or she should implement the previously mentioned method
     *             and throw an {@link UnsupportedOperationException} in this method. This method will be removed in future.
     *
     * @return The set of chosen compound instances. Or an empty set, to indicate that this node should not be processed.
     */
    @Deprecated
    @Nullable
    Set<CompoundInstance> determineResult(Set<Set<CompoundInstance>> candidates, final Boolean complete);

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
}
