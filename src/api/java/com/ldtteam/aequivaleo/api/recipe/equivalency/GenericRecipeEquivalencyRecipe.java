package com.ldtteam.aequivaleo.api.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.calculator.RecipeVariant;
import com.ldtteam.aequivaleo.api.recipe.equivalency.calculator.RecipeVariants;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Represents a generic recipe equivalency recipe.
 * <p>
 * This is the in-graph and game representation of a generic recipe.
 * </p>
 */
public class GenericRecipeEquivalencyRecipe extends BaseEquivalencyRecipe implements IGenericRecipeEquivalencyRecipe {

    /**
     * The name of the recipe.
     */
    private final ResourceLocation name;

    /**
     * Creates a new generic recipe equivalency recipe.
     *
     * @param inputs               The inputs.
     * @param requiredKnownOutputs The required known outputs.
     * @param outputs              The outputs.
     * @param name                 The name of the recipe.
     */
    public GenericRecipeEquivalencyRecipe(Set<IRecipeIngredient> inputs, Set<IRecipeIngredient> requiredKnownOutputs, Set<ICompoundContainer<?>> outputs, ResourceLocation name) {
        super(inputs, requiredKnownOutputs, outputs);
        this.name = name;
    }

    /**
     * Creates a new generic recipe equivalency recipe.
     *
     * @param variant The recipe variant.
     * @param name    The name of the recipe.
     */
    public GenericRecipeEquivalencyRecipe(RecipeVariant variant, ResourceLocation name) {
        super(variant);
        this.name = name;
    }

    @Override
    public ResourceLocation getRecipeName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GenericRecipeEquivalencyRecipe that = (GenericRecipeEquivalencyRecipe) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, super.hashCode());
    }
}
