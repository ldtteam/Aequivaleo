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

public class GenericRecipeDataBuilder {
    private Set<IRecipeIngredient>     inputs;
    private Set<ICompoundContainer<?>> requiredKnownOutputs = Sets.newHashSet();
    private Set<ICompoundContainer<?>> outputs;
    private List<ICondition> conditions = Lists.newArrayList();
    
    public GenericRecipeDataBuilder setInputs(final Set<IRecipeIngredient> inputs)
    {
        this.inputs = inputs;
        return this;
    }

    public GenericRecipeDataBuilder setRequiredKnownOutputs(final Set<ICompoundContainer<?>> requiredKnownOutputs)
    {
        this.requiredKnownOutputs = requiredKnownOutputs;
        return this;
    }

    public GenericRecipeDataBuilder setOutputs(final Set<ICompoundContainer<?>> outputs)
    {
        this.outputs = outputs;
        return this;
    }
    
    public GenericRecipeDataBuilder setConditions(final Set<ICondition> conditions)
    {
        this.conditions.addAll(conditions);
        return this;
    }

    public WithConditions<GenericRecipeData> createGenericRecipeData()
    {
        Validate.notNull(inputs, "The inputs of a recipe are required.");
        Validate.notNull(outputs, "The outputs of a recipe are required.");

        Validate.notEmpty(inputs, "At least one input is required.");
        Validate.notEmpty(outputs, "At least one output is required.");

        return new WithConditions<>(conditions, new GenericRecipeData(inputs, requiredKnownOutputs, outputs));
    }
}