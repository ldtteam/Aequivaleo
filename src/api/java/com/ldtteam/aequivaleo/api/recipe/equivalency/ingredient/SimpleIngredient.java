package com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a simple ingredient for a recipe.
 */
public class SimpleIngredient implements IRecipeIngredient
{
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
}
