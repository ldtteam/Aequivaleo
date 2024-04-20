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

/**
 * Represents the data of a generic recipe on disk.
 */
public class GenericRecipeData
{
    /**
     * The codec for the generic recipe data.
     */
    public static final Codec<GenericRecipeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      AequivaleoExtraCodecs.sortedSetOf(IRecipeIngredient.CODEC).fieldOf("inputs").forGetter(GenericRecipeData::getInputs),
      AequivaleoExtraCodecs.sortedSetOf(ICompoundContainer.CODEC).fieldOf("requiredKnownOutputs").forGetter(GenericRecipeData::getRequiredKnownOutputs),
      AequivaleoExtraCodecs.sortedSetOf(ICompoundContainer.CODEC).fieldOf("outputs").forGetter(GenericRecipeData::getOutputs)
    ).apply(instance, GenericRecipeData::new));

    /**
     * A disabled generic recipe data.
     */
    public static final GenericRecipeData DISABLED = new GenericRecipeData(
      Sets.newHashSet(),
      Sets.newHashSet(),
      Sets.newHashSet()
    );

    private final SortedSet<IRecipeIngredient>     inputs;
    private final SortedSet<ICompoundContainer<?>> requiredKnownOutputs;
    private final SortedSet<ICompoundContainer<?>> outputs;

    /**
     * Creates a new generic recipe data.
     *
     * @param inputs The inputs.
     * @param requiredKnownOutputs The required known outputs.
     * @param outputs The outputs.
     */
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
    public SortedSet<ICompoundContainer<?>> getRequiredKnownOutputs()
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
}
