package com.ldtteam.aequivaleo.analyzer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;


import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class EquivalencyRecipeRegistry implements IEquivalencyRecipeRegistry
{
    private static final Map<RegistryKey<World>, EquivalencyRecipeRegistry> INSTANCES = Maps.newConcurrentMap();

    public static EquivalencyRecipeRegistry getInstance(@NotNull final RegistryKey<World> worldKey)
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
