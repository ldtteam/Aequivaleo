package com.ldtteam.aequivaleo.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency.ITagEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import net.minecraft.tags.ITag;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

public class TagEquivalencyRecipe<T> implements ITagEquivalencyRecipe<T>
{
    private final ITag.INamedTag<T>          tag;
    private final SortedSet<IRecipeIngredient> inputs;
    private final SortedSet<ICompoundContainer<?>> outputs;

    public TagEquivalencyRecipe(
      final ITag.INamedTag<T> tag,
      final ICompoundContainer<?> inputs,
      final ICompoundContainer<?> outputs)
    {
        this.tag = tag;
        this.inputs = new TreeSet<>();
        this.outputs = new TreeSet<>();

        this.inputs.add(new SimpleIngredientBuilder().from(Validate.notNull(inputs)).createSimpleIngredient());
        this.outputs.add(Validate.notNull(outputs));
    }

    @Override
    public ITag.INamedTag<T> getTag()
    {
        return tag;
    }

    @Override
    public SortedSet<IRecipeIngredient> getInputs()
    {
        return inputs;
    }

    @Override
    public SortedSet<ICompoundContainer<?>> getRequiredKnownOutputs()
    {
        return Collections.emptySortedSet();
    }

    @Override
    public SortedSet<ICompoundContainer<?>> getOutputs()
    {
        return outputs;
    }

    @Override
    public Double getOffsetFactor()
    {
        return getOutputs().size() / (double) getInputs().size();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof TagEquivalencyRecipe))
        {
            return false;
        }

        final TagEquivalencyRecipe that = (TagEquivalencyRecipe) o;

        if (getTag() != null ? !getTag().equals(that.getTag()) : that.getTag() != null)
        {
            return false;
        }
        if (getInputs() != null ? !getInputs().equals(that.getInputs()) : that.getInputs() != null)
        {
            return false;
        }
        return getOutputs() != null ? getOutputs().equals(that.getOutputs()) : that.getOutputs() == null;
    }

    @Override
    public int hashCode()
    {
        int result = getTag() != null ? getTag().hashCode() : 0;
        result = 31 * result + (getInputs() != null ? getInputs().hashCode() : 0);
        result = 31 * result + (getOutputs() != null ? getOutputs().hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return String.format("Equivalent via Tag: %s", tag.getName());
    }
}
