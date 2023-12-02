package com.ldtteam.aequivaleo.api.recipe.equivalency.data;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.util.AequivaleoExtraCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.apache.commons.lang3.Validate;

import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class GenericRecipeData
{
    public static final Codec<GenericRecipeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      AequivaleoExtraCodecs.sortedSetOf(IRecipeIngredient.CODEC).fieldOf("inputs").forGetter(GenericRecipeData::getInputs),
      AequivaleoExtraCodecs.sortedSetOf(ICompoundContainer.CODEC).fieldOf("requiredKnownOutputs").forGetter(GenericRecipeData::getRequiredKnownOutputs),
      AequivaleoExtraCodecs.sortedSetOf(ICompoundContainer.CODEC).fieldOf("outputs").forGetter(GenericRecipeData::getOutputs)
    ).apply(instance, GenericRecipeData::new));

    public static final GenericRecipeData DISABLED = new GenericRecipeData(
      Sets.newHashSet(),
      Sets.newHashSet(),
      Sets.newHashSet()
    );

    private final SortedSet<IRecipeIngredient>     inputs;
    private final SortedSet<ICompoundContainer<?>> requiredKnownOutputs;
    private final SortedSet<ICompoundContainer<?>> outputs;

    GenericRecipeData(
      final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> requiredKnownOutputs,
      final Set<ICompoundContainer<?>> outputs
    )
    {
        this.inputs = new TreeSet<>(Validate.noNullElements(Objects.requireNonNull(inputs)));
        this.requiredKnownOutputs = new TreeSet<>(Validate.noNullElements(Objects.requireNonNull(requiredKnownOutputs)));
        this.outputs = new TreeSet<>(Validate.noNullElements(Objects.requireNonNull(outputs)));
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
