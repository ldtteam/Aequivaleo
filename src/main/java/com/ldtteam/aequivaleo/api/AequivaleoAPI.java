package com.ldtteam.aequivaleo.api;

import com.ldtteam.aequivaleo.analyzer.EquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.compound.information.ILockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.compound.information.IValidCompoundTypeInformationProviderRegistry;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryRegistry;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerSerializerRegistry;
import com.ldtteam.aequivaleo.api.results.IResultsInformationCache;
import com.ldtteam.aequivaleo.api.gameobject.equivalent.IGameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.tags.ITagEquivalencyRegistry;
import com.ldtteam.aequivaleo.compound.information.LockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryRegistry;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerSerializerRegistry;
import com.ldtteam.aequivaleo.compound.information.ValidCompoundTypeInformationProviderRegistry;
import com.ldtteam.aequivaleo.results.ResultsInformationCache;
import com.ldtteam.aequivaleo.gameobject.equivalent.GameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.api.gameobject.loottable.ILootTableAnalyserRegistry;
import com.ldtteam.aequivaleo.gameobject.loottable.LootTableAnalyserRegistry;
import com.ldtteam.aequivaleo.tags.TagEquivalencyRegistry;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class AequivaleoAPI implements IAequivaleoAPI
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
    public ICompoundContainerFactoryRegistry getCompoundContainerFactoryRegistry()
    {
        return CompoundContainerFactoryRegistry.getInstance();
    }

    @Override
    public ICompoundContainerSerializerRegistry getCompoundContainerSerializerRegistry()
    {
        return CompoundContainerSerializerRegistry.getInstance();
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
    public IResultsInformationCache getEquivalencyInformationCache(@NotNull final World world)
    {
        return ResultsInformationCache.getInstance(world);
    }
}
