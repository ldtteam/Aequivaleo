package com.ldtteam.aequivaleo.api;

import com.google.gson.GsonBuilder;
import com.ldtteam.aequivaleo.analyzer.EquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.compound.information.ICompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.gameobject.equivalent.IGameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.api.instanced.IInstancedEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPluginManager;
import com.ldtteam.aequivaleo.api.recipe.IRecipeTypeProcessingRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.calculator.IRecipeCalculator;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data.IIngredientSerializerRegistry;
import com.ldtteam.aequivaleo.api.results.IResultsAdapterHandlerRegistry;
import com.ldtteam.aequivaleo.api.results.IResultsInformationCache;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.data.serializers.*;
import com.ldtteam.aequivaleo.compound.information.CompoundInformationRegistry;
import com.ldtteam.aequivaleo.gameobject.equivalent.GameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.instanced.InstancedEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.ldtteam.aequivaleo.recipe.RecipeTypeProcessingRegistry;
import com.ldtteam.aequivaleo.recipe.equivalency.RecipeCalculator;
import com.ldtteam.aequivaleo.recipe.equivalency.data.GenericRecipeDataSerializer;
import com.ldtteam.aequivaleo.recipe.equivalency.ingredient.data.IngredientSerializerRegistry;
import com.ldtteam.aequivaleo.recipe.equivalency.ingredient.data.IngredientSetSerializer;
import com.ldtteam.aequivaleo.recipe.equivalency.ingredient.data.SimpleIngredientSerializer;
import com.ldtteam.aequivaleo.results.ResultsAdapterHandlerRegistry;
import com.ldtteam.aequivaleo.results.ResultsInformationCache;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public final class AequivaleoAPI implements IAequivaleoAPI
{
    private static AequivaleoAPI ourInstance = new AequivaleoAPI();

    public static AequivaleoAPI getInstance()
    {
        return ourInstance;
    }

    private AequivaleoAPI()
    {
    }

    @Override
    public ICompoundContainerFactoryManager getCompoundContainerFactoryManager()
    {
        return CompoundContainerFactoryManager.getInstance();
    }

    @Override
    public IGameObjectEquivalencyHandlerRegistry getGameObjectEquivalencyHandlerRegistry()
    {
        return GameObjectEquivalencyHandlerRegistry.getInstance();
    }

    @Override
    public IEquivalencyRecipeRegistry getEquivalencyRecipeRegistry(@NotNull final RegistryKey<World> worldKey)
    {
        return EquivalencyRecipeRegistry.getInstance(worldKey);
    }

    @Override
    public ICompoundInformationRegistry getLockedCompoundWrapperToTypeRegistry(@NotNull final RegistryKey<World> worldKey)
    {
        return CompoundInformationRegistry.getInstance(worldKey);
    }

    @Override
    public IResultsInformationCache getResultsInformationCache(@NotNull final RegistryKey<World> worldKey)
    {
        return ResultsInformationCache.getInstance(worldKey);
    }

    @Override
    public IAequivaleoPluginManager getPluginManager()
    {
        return PluginManger.getInstance();
    }

    @Override
    public IRecipeCalculator getRecipeCalculator()
    {
        return RecipeCalculator.getInstance();
    }

    @Override
    public GsonBuilder setupGson(final GsonBuilder builder)
    {
        return builder
                 .setLenient()
                 .registerTypeAdapter(CompoundInstanceDataModeSerializer.HANDLED_TYPE, new CompoundInstanceDataModeSerializer())
                 .registerTypeAdapter(CompoundInstanceDataSerializer.HANDLED_TYPE, new CompoundInstanceDataSerializer())
                 .registerTypeAdapter(CompoundInstanceRefSerializer.HANDLED_TYPE, new CompoundInstanceRefSerializer())
                 .registerTypeAdapter(CompoundInstanceRefSetSerializer.HANDLED_TYPE, new CompoundInstanceRefSetSerializer())
                 .registerTypeAdapter(CompoundContainerSetSerializer.HANDLED_TYPE, new CompoundContainerSetSerializer())
                 .registerTypeAdapter(CompoundContainerFactoryManager.HANDLED_TYPE, CompoundContainerFactoryManager.getInstance())
                 .registerTypeAdapter(IngredientSerializerRegistry.HANDLED_TYPE, IngredientSerializerRegistry.getInstance())
                 .registerTypeAdapter(IngredientSetSerializer.HANDLED_TYPE, new IngredientSetSerializer())
                 .registerTypeAdapter(GenericRecipeDataSerializer.HANDLED_TYPE, new GenericRecipeDataSerializer())
                 .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer());
    }

    @Override
    public IRecipeTypeProcessingRegistry getRecipeTypeProcessingRegistry()
    {
        return RecipeTypeProcessingRegistry.getInstance();
    }

    @Override
    public IInstancedEquivalencyHandlerRegistry getInstancedEquivalencyHandlerRegistry()
    {
        return InstancedEquivalencyHandlerRegistry.getInstance();
    }

    @Override
    public IResultsAdapterHandlerRegistry getResultsAdapterHandlerRegistry()
    {
        return ResultsAdapterHandlerRegistry.getInstance();
    }

    @Override
    public IIngredientSerializerRegistry getIngredientSerializerRegistry()
    {
        return IngredientSerializerRegistry.getInstance();
    }
}
