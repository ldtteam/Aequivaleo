package com.ldtteam.aequivaleo.api;

import com.ldtteam.aequivaleo.api.compound.information.ILockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.compound.information.IValidCompoundTypeInformationProviderRegistry;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryRegistry;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerSerializerRegistry;
import com.ldtteam.aequivaleo.api.results.IResultsInformationCache;
import com.ldtteam.aequivaleo.api.gameobject.equivalent.IGameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.tags.ITagEquivalencyRegistry;
import com.ldtteam.aequivaleo.api.gameobject.loottable.ILootTableAnalyserRegistry;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * The API for Equivalency.
 * Retrieved via an IMC with a callback.
 */
public interface IAequivaleoAPI
{

    /**
     * Gives access to the registry that handles the callbacks that convert game objects to their wrapped instances.
     * @return The registry that handles the callbacks used to convert game objects into wrapped counterparts.
     */
    ICompoundContainerFactoryRegistry getCompoundContainerFactoryRegistry();

    /**
     * Gives access to the registry that handles the callbacks that serialize and deserialize the game object with compounds
     * from and to disk.
     *
     * @return The registry that handles the callbacks for serialization and deserialization of wrapped game objects.
     */
    ICompoundContainerSerializerRegistry getCompoundContainerSerializerRegistry();

    /**
     * Gives access to a registry which handles the registration of callbacks which can tell the system if two objects are equal to one another.
     *
     * @return The registry which handles callbacks for equivalency checks.
     */
    IGameObjectEquivalencyHandlerRegistry getGameObjectEquivalencyHandlerRegistry();

    /**
     * Gives access to a registry which handles analysers for equivalencies based on loottables.
     *
     * @return The loot table analyser registry.
     */
    ILootTableAnalyserRegistry getLootTableAnalyserRegistry();

    /**
     * Gives access to a registry which handles equivalencies via tags.
     * @return The registry which allows the analysis engine to take tags into account.
     */
    ITagEquivalencyRegistry getTagEquivalencyRegistry();

    /**
     * Gives access to the registry that holds the recipe information for a given world.
     * @param worldKey The world key to get the equivalency recipe information for.
     * @return The recipe registry for a given world.
     */
    IEquivalencyRecipeRegistry getEquivalencyRecipeRegistry(@NotNull final RegistryKey<World> worldKey);

    /**
     * Gives access to the registry that contains the locking information for game objects and wrappers in a given world.
     *
     * @param worldKey The world key that represents the world for which locking information registry is being retrieved.
     * @return The registry for locking type information for a given world.
     */
    ILockedCompoundInformationRegistry getLockedCompoundWrapperToTypeRegistry(@NotNull final RegistryKey<World> worldKey);

    /**
     * Gives access to the registry that contains the information providers that handle the compound validation logic during analysis for a given world.
     *
     * @param worldKey The world key.
     * @return The registry containing information providers that handle the compound validation logic for a given world.
     */
    IValidCompoundTypeInformationProviderRegistry getValidCompoundTypeInformationProviderRegistry(@NotNull final RegistryKey<World> worldKey);

    /**
     * Gives access to the cache that contains the equivalency information after calculation.
     *
     * @param world The world to get the equivalency cache for.
     * @return The equivalency cache for a given dimension.
     */
    IResultsInformationCache getEquivalencyInformationCache(@NotNull final World world);


}
