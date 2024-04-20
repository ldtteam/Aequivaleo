package com.ldtteam.aequivaleo.api.recipe.equivalency.data;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Set;

/**
 * A builder for generic recipe data.
 * <p>
 *     This builder is implemented as a mutable builder.
 * </p>
 */
public class GenericRecipeDataBuilder {
    private Set<IRecipeIngredient>     inputs;
    private Set<ICompoundContainer<?>> requiredKnownOutputs = Sets.newHashSet();
    private Set<ICompoundContainer<?>> outputs;
    private List<ICondition> conditions = Lists.newArrayList();

    /**
     * Sets the inputs of the recipe.
     *
     * @param inputs The inputs.
     * @return This builder.
     */
    public GenericRecipeDataBuilder setInputs(final Set<IRecipeIngredient> inputs)
    {
        this.inputs = inputs;
        return this;
    }

    /**
     * Sets the required known outputs of the recipe.
     *
     * @param requiredKnownOutputs The required known outputs.
     * @return This builder.
     */
    public GenericRecipeDataBuilder setRequiredKnownOutputs(final Set<ICompoundContainer<?>> requiredKnownOutputs)
    {
        this.requiredKnownOutputs = requiredKnownOutputs;
        return this;
    }

    /**
     * Sets the outputs of the recipe.
     *
     * @param outputs The outputs.
     * @return This builder.
     */
    public GenericRecipeDataBuilder setOutputs(final Set<ICompoundContainer<?>> outputs)
    {
        this.outputs = outputs;
        return this;
    }

    /**
     * Adds a condition to the recipe.
     *
     * @param conditions The conditions .
     * @return This builder.
     */
    public GenericRecipeDataBuilder setConditions(final Set<ICondition> conditions)
    {
        this.conditions.addAll(conditions);
        return this;
    }

    /**
     * Builds a conditional generic recipe data.
     *
     * @return The conditional generic recipe data.
     */
    public WithConditions<GenericRecipeData> createGenericRecipeData()
    {
        Validate.notNull(inputs, "The inputs of a recipe are required.");
        Validate.notNull(outputs, "The outputs of a recipe are required.");

        Validate.notEmpty(inputs, "At least one input is required.");
        Validate.notEmpty(outputs, "At least one output is required.");

        return new WithConditions<>(conditions, new GenericRecipeData(inputs, requiredKnownOutputs, outputs));
    }
}