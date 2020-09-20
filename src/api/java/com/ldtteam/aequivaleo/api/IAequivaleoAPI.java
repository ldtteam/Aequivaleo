package com.ldtteam.aequivaleo.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.compound.information.locked.ILockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.gameobject.equivalent.IGameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPluginManager;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IRecipeCalculator;
import com.ldtteam.aequivaleo.api.results.IResultsInformationCache;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * The API for aequivaleo.
 * Retrieved via an IMC with a callback or via its {@link #getInstance()} method.
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

    /**
     * Gives access to the aequivaleo plugin manager.
     *
     * @return The plugin manager.
     */
    IAequivaleoPluginManager getPluginManager();

    /**
     * Gives access to the recipe calculator.
     *
     * @return The recipe calculator.
     */
    IRecipeCalculator getRecipeCalculator();

    /**
     * Sets up a new Gson instance and create the serialization handler.
     *
     * @return The Gson serialization handler.
     */
    default Gson getGson() {
        return setupGson().create();
    }

    /**
     * Setups a new {@link GsonBuilder} to use with Aequivaleos serializers.
     *
     * @return The new {@link GsonBuilder} setup to be used with Aequivaleo.
     */
    default GsonBuilder setupGson() {
        return setupGson(new GsonBuilder());
    }

    /**
     * Allows for Aequivaleo to inject its serialization handlers into the given {@link GsonBuilder}.
     *
     * @param builder The builder to inject serializers into.
     * @return The builder with the serializers setup as type adapters.
     */
    GsonBuilder setupGson(GsonBuilder builder);

    /**
     * Returns the aequivaleo mod container.
     * Allows for the creation of configurations in the name of aequivaleo by its plugins.
     *
     * @return The aequivaleo mod container.
     */
    default ModContainer getAequivaleoContainer() {
        return ModList.get().getModContainerById(Constants.MOD_ID).orElseThrow(()->new RuntimeException("Where is Aequivaleo???!"));
    }

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
