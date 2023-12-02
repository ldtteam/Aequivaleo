package com.ldtteam.aequivaleo.recipe.equivalency.ingredient;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data.IRecipeIngredientType;
import com.ldtteam.aequivaleo.api.util.AequivaleoExtraCodecs;
import com.ldtteam.aequivaleo.bootstrap.ModRecipeIngredientTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a simple ingredient for a recipe.
 */
public class SimpleIngredient implements IRecipeIngredient
{
    public static final Codec<SimpleIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        AequivaleoExtraCodecs.setOf(ICompoundContainer.CODEC).fieldOf("candidates").forGetter(SimpleIngredient::getCandidates),
        Codec.DOUBLE.fieldOf("count").forGetter(SimpleIngredient::getRequiredCount)
    ).apply(instance, SimpleIngredient::new));
    
    private static final Logger LOGGER = LogManager.getLogger();

    private final SortedSet<ICompoundContainer<?>> candidates;
    private final double                           count;

    SimpleIngredient(final Set<ICompoundContainer<?>> candidates, final double count) {
        this.candidates = candidates.stream().filter(ICompoundContainer::isValid).collect(Collectors.toCollection(TreeSet::new));
        this.count = count;

        candidates.stream().filter(container -> !container.isValid())
          .forEach(inValidContainer -> LOGGER.debug(String.format("Tried to add invalid container to ingredient: %s", inValidContainer)));
    }

    @Override
    public SortedSet<ICompoundContainer<?>> getCandidates()
    {
        return candidates;
    }

    @Override
    public Double getRequiredCount()
    {
        return count;
    }
    
    @Override
    public IRecipeIngredientType getType() {
        return ModRecipeIngredientTypes.SIMPLE.get();
    }
    
    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final SimpleIngredient that = (SimpleIngredient) o;
        return Double.compare(that.count, count) == 0 &&
                 getCandidates().equals(that.getCandidates());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getCandidates(), count);
    }

    @Override
    public String toString()
    {
        return candidates.stream().map(Object::toString).collect(Collectors.joining(", "));
    }
    
    public static final class Type implements IRecipeIngredientType {
        
        @Override
        public Codec<? extends IRecipeIngredient> codec() {
            return CODEC;
        }
    }
}
