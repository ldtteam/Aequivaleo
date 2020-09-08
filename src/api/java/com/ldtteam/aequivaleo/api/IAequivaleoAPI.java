package com.ldtteam.aequivaleo.api;

import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.compound.information.locked.ILockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.gameobject.equivalent.IGameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.results.IResultsInformationCache;
import com.ldtteam.aequivaleo.api.tags.ITagEquivalencyRegistry;
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
     * Returns the instance of the api, once it has been initialized.
     * (Initialization happens during mod construction)
     *
     * @return The api.
     */
    static IAequivaleoAPI getInstance() {
        return IAequivaleoAPI.Holder.getInstance();
    }

    /**
     * Gives access to the registry that handles the callbacks that convert game objects to their wrapped instances.
     * @return The registry that handles the callbacks used to convert game objects into wrapped counterparts.
     */
    ICompoundContainerFactoryManager getCompoundContainerFactoryManager();

    /**
     * Gives access to a registry which handles the registration of callbacks which can tell the system if two objects are equal to one another.
     *
     * @return The registry which handles callbacks for equivalency checks.
     */
    IGameObjectEquivalencyHandlerRegistry getGameObjectEquivalencyHandlerRegistry();

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
     * Gives access to the cache that contains the equivalency information after calculation.
     *
     * @param worldKey The world key to get the equivalency cache for.
     * @return The equivalency cache for a given dimension.
     */
    IResultsInformationCache getResultsInformationCache(@NotNull final RegistryKey<World> worldKey);

    class Holder {
        private static IAequivaleoAPI apiInstance;

        public static IAequivaleoAPI getInstance()
        {
            return apiInstance;
        }

        public static void setInstance(final IAequivaleoAPI instance)
        {
            if (apiInstance != null)
                throw new IllegalStateException("Can not setup API twice!");

            apiInstance = instance;
        }
    }
}
