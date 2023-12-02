package com.ldtteam.aequivaleo.apiimpl;

import com.ldtteam.aequivaleo.analysis.AnalysisStateManager;
import com.ldtteam.aequivaleo.analysis.BlacklistDimensionManager;
import com.ldtteam.aequivaleo.analysis.EquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.analysis.AnalysisState;
import com.ldtteam.aequivaleo.api.analysis.IBlacklistDimensionManager;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.compound.information.ICompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.gameobject.equivalent.IGameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.api.instanced.IInstancedEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPluginManager;
import com.ldtteam.aequivaleo.api.recipe.IRecipeTypeProcessingRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.calculator.IRecipeCalculator;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IDefaultRecipeIngredients;
import com.ldtteam.aequivaleo.api.registry.IRegistryView;
import com.ldtteam.aequivaleo.api.results.IEquivalencyResults;
import com.ldtteam.aequivaleo.api.results.IResultsAdapterHandlerRegistry;
import com.ldtteam.aequivaleo.api.results.IResultsInformationCache;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.information.CompoundInformationRegistry;
import com.ldtteam.aequivaleo.gameobject.equivalent.GameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.instanced.InstancedEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.ldtteam.aequivaleo.recipe.RecipeTypeProcessingRegistry;
import com.ldtteam.aequivaleo.recipe.equivalency.RecipeCalculator;
import com.ldtteam.aequivaleo.recipe.equivalency.ingredient.DefaultRecipeIngredients;
import com.ldtteam.aequivaleo.registry.ShadowRegistry;
import com.ldtteam.aequivaleo.results.EquivalencyResults;
import com.ldtteam.aequivaleo.results.ResultsAdapterHandlerRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public final class AequivaleoAPI implements IAequivaleoAPI
{
    private static final AequivaleoAPI ourInstance = new AequivaleoAPI();

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
    public IEquivalencyRecipeRegistry getEquivalencyRecipeRegistry(@NotNull final ResourceKey<Level> worldKey)
    {
        return EquivalencyRecipeRegistry.getInstance(worldKey);
    }

    @Override
    public ICompoundInformationRegistry getLockedCompoundWrapperToTypeRegistry(@NotNull final ResourceKey<Level> worldKey)
    {
        return CompoundInformationRegistry.getInstance(worldKey);
    }

    @Deprecated
    @Override
    public IResultsInformationCache getResultsInformationCache(@NotNull final ResourceKey<Level> worldKey)
    {
        return EquivalencyResults.getInstance(worldKey);
    }

    @Override
    public IEquivalencyResults getEquivalencyResults(final ResourceKey<Level> worldKey)
    {
        return EquivalencyResults.getInstance(worldKey);
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
    public AnalysisState getState(final ResourceKey<Level> key)
    {
        return AnalysisStateManager.getState(key);
    }

    @Override
    public IBlacklistDimensionManager getBlacklistDimensionManager()
    {
        return BlacklistDimensionManager.getInstance();
    }

    @Override
    public <T, E> IRegistryView<E> createView(final Registry<T> registry, final Function<T, Optional<E>> viewFilter) {
        return new ShadowRegistry<>(registry, viewFilter);
    }
    
    @Override
    public @NotNull IDefaultRecipeIngredients getDefaultRecipeIngredients() {
        return DefaultRecipeIngredients.getInstance();
    }
}
