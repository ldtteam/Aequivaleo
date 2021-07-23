package com.ldtteam.aequivaleo.recipe;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.recipe.IRecipeTypeProcessingRegistry;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class RecipeTypeProcessingRegistry implements IRecipeTypeProcessingRegistry
{

    private static final RecipeTypeProcessingRegistry INSTANCE = new RecipeTypeProcessingRegistry();

    public static RecipeTypeProcessingRegistry getInstance()
    {
        return INSTANCE;
    }

    private final Map<ResourceLocation, Set<RecipeType<?>>> types = Maps.newConcurrentMap();

    private RecipeTypeProcessingRegistry()
    {
    }

    @Override
    public IRecipeTypeProcessingRegistry registerAs(final ResourceLocation type, final RecipeType<?>... vanillaTypes)
    {
        types.computeIfAbsent(type, (t) -> Sets.newConcurrentHashSet()).addAll(Arrays.asList(vanillaTypes.clone()));
        return this;
    }

    @Override
    public Set<RecipeType<?>> getRecipeTypesToBeProcessedAs(final ResourceLocation type)
    {
        return types.getOrDefault(type, Sets.newHashSet());
    }

    @Override
    public Set<RecipeType<?>> getAllKnownTypes()
    {
        return types.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }
}
