package com.ldtteam.aequivaleo.api.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;

import java.util.Set;

/**
 * Represents a "recipe", this class does not only represent crafting in the normal sense.
 * But also for example the conversion of furnace fuel into heat.
 *
 * EG: 2 Wooden planks for 4 Sticks as output, as well a 1 coal for 16 heat.
 */
public interface IEquivalencyRecipe
{

    /**
     * The compound containers that are the input for this recipe.
     *
     * @return The inputs.
     */
    Set<ICompoundContainer<?>> getInputs();

    /**
     * The compound containers that are the output for this recipe.
     *
     * @return The output.
     */
    Set<ICompoundContainer<?>> getOutputs();

    /**
     * Returns the offset factor between inputs and outputs.
     *
     * @return
     */
    Double getOffsetFactor();
}
