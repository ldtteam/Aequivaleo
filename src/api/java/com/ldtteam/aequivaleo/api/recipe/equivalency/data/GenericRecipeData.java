package com.ldtteam.aequivaleo.api.recipe.equivalency.data;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import net.minecraftforge.common.crafting.conditions.FalseCondition;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.apache.commons.lang3.Validate;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * Represents the data of a generic recipe on disk.
 */
public class GenericRecipeData
{

    /**
     * A disabled generic recipe data.
     */
    public static final GenericRecipeData DISABLED = new GenericRecipeData(
      Sets.newHashSet(),
      Sets.newHashSet(),
      Sets.newHashSet(),
      Sets.newHashSet(FalseCondition.INSTANCE)
    );

    private final SortedSet<IRecipeIngredient>     inputs;
    private final SortedSet<IRecipeIngredient> requiredKnownOutputs;
    private final SortedSet<ICompoundContainer<?>> outputs;
    private final SortedSet<ICondition> conditions;

    /**
     * Creates a new generic recipe data.
     *
     * @param inputs The inputs.
     * @param requiredKnownOutputs The required known outputs.
     * @param outputs The outputs.
     */
    GenericRecipeData(
      final Set<IRecipeIngredient> inputs,
      final Set<IRecipeIngredient> requiredKnownOutputs,
      final Set<ICompoundContainer<?>> outputs,
      final Set<ICondition> conditions
    )
    {
        this.inputs = new TreeSet<>(Validate.noNullElements(Validate.notNull(inputs)));
        this.requiredKnownOutputs = new TreeSet<>(Validate.noNullElements(Validate.notNull(requiredKnownOutputs)));
        this.outputs = new TreeSet<>(Validate.noNullElements(Validate.notNull(outputs)));
        this.conditions = new TreeSet<>(Comparator.comparing(iCondition -> iCondition.getID().toString()));
        this.conditions.addAll(Validate.noNullElements(Validate.notNull(conditions)));
    }

    /**
     * Gets the inputs.
     *
     * @return The inputs.
     */
    public SortedSet<IRecipeIngredient> getInputs()
    {
        return inputs;
    }

    /**
     * Gets the required known outputs.
     *
     * @return The required known outputs.
     */
    public SortedSet<IRecipeIngredient> getRequiredKnownOutputs()
    {
        return requiredKnownOutputs;
    }

    /**
     * Gets the outputs.
     *
     * @return The outputs.
     */
    public SortedSet<ICompoundContainer<?>> getOutputs()
    {
        return outputs;
    }

    /**
     * Gets the conditions.
     *
     * @return The conditions.
     */
    public Set<ICondition> getConditions()
    {
        return conditions;
    }
}
