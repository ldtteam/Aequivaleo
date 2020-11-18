package com.ldtteam.aequivaleo.api.recipe.equivalency.data;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class GenericRecipeData
{
    private final SortedSet<IRecipeIngredient>     inputs;
    private final SortedSet<ICompoundContainer<?>> requiredKnownOutputs;
    private final SortedSet<ICompoundContainer<?>> outputs;

    public GenericRecipeData(
      final Set<IRecipeIngredient> inputs, final Set<ICompoundContainer<?>> requiredKnownOutputs, final Set<ICompoundContainer<?>> outputs)
    {
        this.inputs = new TreeSet<>(Validate.noNullElements(Validate.notNull(inputs)));
        this.requiredKnownOutputs = new TreeSet<>(Validate.noNullElements(Validate.notNull(requiredKnownOutputs)));
        this.outputs = new TreeSet<>(Validate.noNullElements(Validate.notNull(outputs)));
    }

    public SortedSet<IRecipeIngredient> getInputs()
    {
        return inputs;
    }

    public SortedSet<ICompoundContainer<?>> getRequiredKnownOutputs()
    {
        return requiredKnownOutputs;
    }

    public SortedSet<ICompoundContainer<?>> getOutputs()
    {
        return outputs;
    }
}
