package com.ldtteam.aequivaleo.api.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.calculator.RecipeVariant;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A base implementation of an equivalency recipe.
 */
public class BaseEquivalencyRecipe implements IEquivalencyRecipe {

    /**
     * The inputs.
     */
    protected final SortedSet<IRecipeIngredient> inputs;
    /**
     * The required known outputs.
     */
    protected final SortedSet<IRecipeIngredient> requiredKnownOutputs;
    /**
     * The outputs.
     */
    protected final SortedSet<ICompoundContainer<?>> outputs;

    /**
     * Creates a new generic recipe equivalency recipe.
     *
     * @param inputs               The inputs.
     * @param requiredKnownOutputs The required known outputs.
     * @param outputs              The outputs.
     */
    public BaseEquivalencyRecipe(
            final Set<IRecipeIngredient> inputs, final Set<IRecipeIngredient> requiredKnownOutputs, final Set<ICompoundContainer<?>> outputs) {
        this.inputs = new TreeSet<>(Validate.noNullElements(Validate.notNull(inputs)));
        this.requiredKnownOutputs = new TreeSet<>(Validate.noNullElements(Validate.notNull(requiredKnownOutputs)));
        this.outputs = new TreeSet<>(Validate.noNullElements(Validate.notNull(outputs)));
    }

    /**
     * Creates a new generic recipe equivalency recipe.
     *
     * @param variant The recipe variant.
     */
    public BaseEquivalencyRecipe(final RecipeVariant variant) {
        this.inputs = new TreeSet<>(variant.ingredients());
        this.requiredKnownOutputs = new TreeSet<>(variant.remainders());
        this.outputs = new TreeSet<>(Set.of(variant.output()));
    }

    public SortedSet<IRecipeIngredient> getInputs() {
        return inputs;
    }

    public SortedSet<IRecipeIngredient> getRequiredKnownOutputs() {
        return requiredKnownOutputs;
    }

    public SortedSet<ICompoundContainer<?>> getOutputs() {
        return outputs;
    }

    @Override
    public String toString() {
        return "%s{inputs=%s, requiredKnownOutputs=%s, outputs=%s}".formatted(getClass().getSimpleName(), inputs, requiredKnownOutputs, outputs);
    }
}
