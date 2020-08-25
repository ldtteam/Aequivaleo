package com.ldtteam.aequivaleo.api.compound.information.contribution;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.util.TriFunction;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface IContributionInformationProviderRegistry
{
    /**
     * Gives access to the current instance of the contribution information provider registry.
     *
     * @param worldKey The key for the world for which the instance is retrieved.
     *
     * @return The contribution information provider registry.
     */
    static IContributionInformationProviderRegistry getInstance(@NotNull final RegistryKey<World> worldKey) {
        return IAequivaleoAPI.getInstance().getContributionInformationProviderRegistry(worldKey);
    }

    /**
     * Registers an information provider used during analysis directly, when analyzing recipe inputs.
     *
     * @param provider The provider that is being registered.
     * @return The registry with the provider added.
     */
    IContributionInformationProviderRegistry registerNewInputProvider(@NotNull final IContributionInformationProvider<?> provider);

    /**
     * Registers an information provider used during analysis, when analyzing recipe inputs
     * This information provider is build from the given class and callback.
     *
     * @param clazz The class of the game object for which the callback serves as a {@link IContributionInformationProvider}
     * @param decider The callback that determines if a given compound type is valid for a given game object.
     * @param <T> The type of the game object.
     * @return The registry with the provider, constructed from the class and callback, added.
     */
    default <T> IContributionInformationProviderRegistry registerNewInputProvider(@NotNull final Class<T> clazz, @NotNull final TriFunction<ICompoundContainer<T>, IEquivalencyRecipe, ICompoundType, Optional<Boolean>> decider)
    {
        return this.registerNewInputProvider(new SimpleTriFunctionBasedContributionInformationProvider<>(clazz, decider));
    }


    /**
     * Registers an information provider used during analysis directly, when analyzing recipe outputs.
     *
     * @param provider The provider that is being registered.
     * @return The registry with the provider added.
     */
    IContributionInformationProviderRegistry registerNewOutputProvider(@NotNull final IContributionInformationProvider<?> provider);

    /**
     * Registers an information provider used during analysis, when analyzing recipe outputs
     * This information provider is build from the given class and callback.
     *
     * @param clazz The class of the game object for which the callback serves as a {@link IContributionInformationProvider}
     * @param decider The callback that determines if a given compound type is valid for a given game object.
     * @param <T> The type of the game object.
     * @return The registry with the provider, constructed from the class and callback, added.
     */
    default <T> IContributionInformationProviderRegistry registerNewOutputProvider(@NotNull final Class<T> clazz, @NotNull final TriFunction<ICompoundContainer<T>, IEquivalencyRecipe, ICompoundType, Optional<Boolean>> decider)
    {
        return this.registerNewOutputProvider(new SimpleTriFunctionBasedContributionInformationProvider<>(clazz, decider));
    }
}
