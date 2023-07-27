package com.ldtteam.aequivaleo.analysis;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;


import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class EquivalencyRecipeRegistry implements IEquivalencyRecipeRegistry
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceKey<Level>, EquivalencyRecipeRegistry> INSTANCES = Maps.newConcurrentMap();

    public static EquivalencyRecipeRegistry getInstance(@NotNull final ResourceKey<Level> worldKey)
    {
        return INSTANCES.computeIfAbsent(worldKey, (dimType) -> new EquivalencyRecipeRegistry());
    }

    private final Set<IEquivalencyRecipe> recipes = Sets.newConcurrentHashSet();

    private EquivalencyRecipeRegistry()
    {
    }

    /**
     * Adds a new recipe to the registry.
     *
     * @param recipe The recipe to add.
     * @return The registry.
     */
    @NotNull
    @Override
    public IEquivalencyRecipeRegistry register(@NotNull final IEquivalencyRecipe recipe)
    {
        if (recipe.getOutputs().stream().anyMatch(container -> !container.isValid()))
        {
            LOGGER.debug(String.format("Skipping recipe because output is invalid: %s", recipe));
            return this;
        }

        if (recipe.getRequiredKnownOutputs().stream().anyMatch(container -> !container.isValid()))
        {
            LOGGER.debug(String.format("Skipping recipe because required known outputs (residues) is invalid: %s", recipe));
            return this;
        }

        if (recipe.getInputs().stream().anyMatch(input -> input.getCandidates().isEmpty()))
        {
            LOGGER.debug(String.format("Skipping recipe because input is empty: %s", recipe));
            return this;
        }

        if (recipe.getInputs().isEmpty())
        {
            LOGGER.debug(String.format("Skipping recipe, because it has no inputs: %s", recipe));
            return this;
        }

        recipes.add(recipe);
        return this;
    }

    public void reset()
    {
        recipes.clear();
    }

    @NotNull
    public SortedSet<IEquivalencyRecipe> get()
    {
        //We need to sort here, to ensure ordering is properly guaranteed.
        //Makes analysis predictable.
        return new TreeSet<>(recipes);
    }
}
