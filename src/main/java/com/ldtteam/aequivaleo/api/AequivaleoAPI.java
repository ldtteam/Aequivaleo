package com.ldtteam.aequivaleo.api;

import com.ldtteam.aequivaleo.analyzer.EquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.compound.information.contribution.IContributionInformationProviderRegistry;
import com.ldtteam.aequivaleo.api.compound.information.locked.ILockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.compound.information.validity.IValidCompoundTypeInformationProviderRegistry;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.results.IResultsInformationCache;
import com.ldtteam.aequivaleo.api.gameobject.equivalent.IGameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.tags.ITagEquivalencyRegistry;
import com.ldtteam.aequivaleo.compound.information.contribution.ContributionInformationProviderRegistry;
import com.ldtteam.aequivaleo.compound.information.locked.LockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.information.validity.ValidCompoundTypeInformationProviderRegistry;
import com.ldtteam.aequivaleo.results.ResultsInformationCache;
import com.ldtteam.aequivaleo.gameobject.equivalent.GameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.api.gameobject.loottable.ILootTableAnalyserRegistry;
import com.ldtteam.aequivaleo.gameobject.loottable.LootTableAnalyserRegistry;
import com.ldtteam.aequivaleo.tags.TagEquivalencyRegistry;
import net.minecraft.util.RegistryKey;
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
    public ILootTableAnalyserRegistry getLootTableAnalyserRegistry()
    {
        return LootTableAnalyserRegistry.getInstance();
    }

    @Override
    public ITagEquivalencyRegistry getTagEquivalencyRegistry()
    {
        return TagEquivalencyRegistry.getInstance();
    }

    @Override
    public IEquivalencyRecipeRegistry getEquivalencyRecipeRegistry(@NotNull final RegistryKey<World> worldKey)
    {
        return EquivalencyRecipeRegistry.getInstance(worldKey);
    }

    @Override
    public ILockedCompoundInformationRegistry getLockedCompoundWrapperToTypeRegistry(@NotNull final RegistryKey<World> worldKey)
    {
        return LockedCompoundInformationRegistry.getInstance(worldKey);
    }

    @Override
    public IValidCompoundTypeInformationProviderRegistry getValidCompoundTypeInformationProviderRegistry(@NotNull final RegistryKey<World> worldKey)
    {
        return ValidCompoundTypeInformationProviderRegistry.getInstance(worldKey);
    }

    @Override
    public IResultsInformationCache getResultsInformationCache(@NotNull final RegistryKey<World> worldKey)
    {
        return ResultsInformationCache.getInstance(worldKey);
    }

    @Override
    public IContributionInformationProviderRegistry getContributionInformationProviderRegistry(@NotNull final RegistryKey<World> worldKey)
    {
        return ContributionInformationProviderRegistry.getInstance(worldKey);
    }
}
