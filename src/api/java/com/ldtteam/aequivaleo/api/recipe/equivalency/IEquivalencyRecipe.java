package com.ldtteam.aequivaleo.api.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.util.SortedSetComparator;
import org.jetbrains.annotations.NotNull;

import java.util.SortedSet;

/**
 * Represents a "recipe", this class does not only represent crafting in the normal sense.
 * But also for example the conversion of furnace fuel into heat.
 *
 * EG: 2 Wooden planks for 4 Sticks as output, as well a 1 coal for 16 heat.
 */
public interface IEquivalencyRecipe extends Comparable<IEquivalencyRecipe>
{

    /**
     * The compound containers that are the input for this recipe.
     *
     * @return The inputs.
     */
    SortedSet<IRecipeIngredient> getInputs();

    /**
     * These are compound containers which have be processed first before calculation of the output
     * types can commence via this recipe.
     *
     * Examples of items returned here are: The empty bucket from the cake recipe.
     * This ensures that the compound types on those outputs are known, and are treated as catalysts.
     *
     * @return The compound containers who act as catalysts in the recipe.
     */
    SortedSet<ICompoundContainer<?>> getRequiredKnownOutputs();

    /**
     * The compound containers that are the output for this recipe.
     *
     * @return The output.
     */
    SortedSet<ICompoundContainer<?>> getOutputs();

    /**
     * Returns the offset factor between inputs and outputs.
     *
     * @return The offset factor.
     */
    default Double getOffsetFactor() {
        return 1d;
    }

    /**
     * Indicates if this recipe is valid.
     *
     * @return {@code True} when valid.
     */
    default boolean isValid() {
        return !getInputs().isEmpty() &&
                 !getOutputs().isEmpty() &&
                 getInputs().stream().allMatch(IRecipeIngredient::isValid) &&
                 getRequiredKnownOutputs().stream().allMatch(ICompoundContainer::isValid) &&
                 getOutputs().stream().allMatch(ICompoundContainer::isValid);
    }

    @Override
    default int compareTo(@NotNull IEquivalencyRecipe recipe) {
        final int inputComparison = SortedSetComparator.<IRecipeIngredient>getInstance().compare(getInputs(), recipe.getInputs());
        if (inputComparison != 0)
            return inputComparison;

        final int requiredOutputsComparison = SortedSetComparator.<ICompoundContainer<?>>getInstance().compare(getRequiredKnownOutputs(), recipe.getRequiredKnownOutputs());
        if (requiredOutputsComparison != 0)
            return requiredOutputsComparison;

        final int outputComparison = SortedSetComparator.<ICompoundContainer<?>>getInstance().compare(getOutputs(), recipe.getOutputs());
        if (outputComparison != 0)
            return outputComparison;

        return (int) (getOffsetFactor() - recipe.getOffsetFactor());
    }
}
