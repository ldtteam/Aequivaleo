package com.ldtteam.aequivaleo.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency.ITagEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TagEquivalencyRecipe<T> implements ITagEquivalencyRecipe<T>
{
    private final TagKey<T>                    tag;
    private final SortedSet<IRecipeIngredient> inputs;
    private final SortedSet<ICompoundContainer<?>> elements;

    public TagEquivalencyRecipe(
      final TagKey<T> tag,
      final Collection<ICompoundContainer<?>> elements)
    {
        this.tag = Validate.notNull(tag, "Tag cannot be null.");
        this.elements = new TreeSet<>();

        Validate.notNull(elements, "Elements cannot be null.");
        this.elements.addAll(elements);

        this.inputs = new TreeSet<>();
        this.inputs.addAll(elements.stream().map(SimpleIngredientBuilder::simple).collect(Collectors.toSet()));

        Validate.notEmpty(this.inputs, "Inputs cannot be empty.");
    }

    @Override
    public TagKey<T> getTag()
    {
        return tag;
    }

    @Override
    public SortedSet<IRecipeIngredient> getInputs()
    {
        return inputs;
    }

    @Override
    public SortedSet<IRecipeIngredient> getRequiredKnownOutputs()
    {
        return Collections.emptySortedSet();
    }

    @Override
    public SortedSet<ICompoundContainer<?>> getOutputs()
    {
        return elements;
    }

    @Override
    public Double getOffsetFactor()
    {
        return getOutputs().size() / (double) getInputs().size();
    }

    @Override
    public boolean isDistributor() {
        return true;
    }

    @Override
    public boolean isValid() {
        return !this.elements.isEmpty();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof final TagEquivalencyRecipe<?> that))
        {
            return false;
        }

        return getTag().equals(that.getTag());
    }

    @Override
    public int hashCode()
    {
        return getTag() != null ? getTag().hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return String.format("Equivalent via Tag: %s", tag.location());
    }
}
