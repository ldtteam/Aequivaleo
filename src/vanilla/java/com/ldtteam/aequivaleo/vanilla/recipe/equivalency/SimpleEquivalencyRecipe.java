package com.ldtteam.aequivaleo.vanilla.recipe.equivalency;

import com.ldtteam.aequivaleo.api.recipe.equivalency.BaseEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.calculator.RecipeVariant;
import com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency.ISimpleEquivalencyRecipe;
import net.minecraft.resources.ResourceLocation;

public class SimpleEquivalencyRecipe extends BaseEquivalencyRecipe implements ISimpleEquivalencyRecipe
{

    private final ResourceLocation name;

    public SimpleEquivalencyRecipe(final RecipeVariant variant, ResourceLocation name) {
        super(variant);
        this.name = name;
    }

    @Override
    public String toString() {
        return "SimpleEquivalencyRecipe{" +
                "name=" + name +
                ", inputs=" + inputs +
                ", requiredKnownOutputs=" + requiredKnownOutputs +
                ", outputs=" + outputs +
                '}';
    }
}
