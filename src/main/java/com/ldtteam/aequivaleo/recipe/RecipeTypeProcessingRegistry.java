package com.ldtteam.aequivaleo.recipe;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.recipe.IRecipeTypeProcessingRegistry;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public final class RecipeTypeProcessingRegistry implements IRecipeTypeProcessingRegistry
{

    private static final RecipeTypeProcessingRegistry INSTANCE = new RecipeTypeProcessingRegistry();

    public static RecipeTypeProcessingRegistry getInstance()
    {
        return INSTANCE;
    }

    private final Map<ResourceLocation, Set<IRecipeType<?>>> types = Maps.newConcurrentMap();

    private RecipeTypeProcessingRegistry()
    {
    }

    @Override
    public IRecipeTypeProcessingRegistry registerAs(final ResourceLocation type, final IRecipeType<?>... vanillaTypes)
    {
        types.computeIfAbsent(type, (t) -> Sets.newConcurrentHashSet()).addAll(Arrays.asList(vanillaTypes.clone()));
        return this;
    }

    @Override
    public Set<IRecipeType<?>> getRecipeTypesToBeProcessedAs(final ResourceLocation type)
    {
        return types.getOrDefault(type, Sets.newHashSet());
    }
}
