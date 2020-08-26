package com.ldtteam.aequivaleo.compound.information.contribution;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.information.contribution.IContributionInformationProvider;
import com.ldtteam.aequivaleo.api.compound.information.contribution.IContributionInformationProviderRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.util.Suppression;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ContributionInformationProviderRegistry implements IContributionInformationProviderRegistry
{

    private static final Map<RegistryKey<World>, ContributionInformationProviderRegistry> INSTANCES = Maps.newConcurrentMap();

    public static ContributionInformationProviderRegistry getInstance(@NotNull final RegistryKey<World> worldKey)
    {
        return INSTANCES.computeIfAbsent(worldKey, ContributionInformationProviderRegistry::new);
    }

    private final Map<Class<?>, Set<IContributionInformationProvider<?>>> inputs = Maps.newConcurrentMap();
    private final Map<Class<?>, Set<IContributionInformationProvider<?>>> outputs = Maps.newConcurrentMap();
    private final RegistryKey<World>                                      worldKey;

    private ContributionInformationProviderRegistry(final RegistryKey<World> worldKey)
    {
        this.worldKey = worldKey;
    }


    @Override
    public IContributionInformationProviderRegistry registerNewInputProvider(@NotNull final IContributionInformationProvider<?> provider)
    {
        inputs.computeIfAbsent(provider.getWrappedContentType(), (type) -> Sets.newConcurrentHashSet()).add(provider);
        return this;
    }

    @Override
    public IContributionInformationProviderRegistry registerNewOutputProvider(@NotNull final IContributionInformationProvider<?> provider)
    {
        outputs.computeIfAbsent(provider.getWrappedContentType(), (type) -> Sets.newConcurrentHashSet()).add(provider);
        return this;
    }

    public RegistryKey<World> getWorldKey()
    {
        return worldKey;
    }

    public void reset()
    {
        inputs.clear();
        outputs.clear();
    }

    @SuppressWarnings(Suppression.UNCHECKED)
    public <T> boolean canCompoundTypeContributeAsInput(
      @NotNull final ICompoundContainer<T> wrapper,
      @NotNull final IEquivalencyRecipe recipe,
      @NotNull final ICompoundType type
    )
    {
        return checkDataMap(wrapper, recipe, type, inputs);
    }

    @SuppressWarnings(Suppression.UNCHECKED)
    public <T> boolean canCompoundTypeContributeAsOutput(
      @NotNull final ICompoundContainer<T> wrapper,
      @NotNull final IEquivalencyRecipe recipe,
      @NotNull final ICompoundType type
    )
    {
        return checkDataMap(wrapper, recipe, type, outputs);
    }

    @SuppressWarnings(Suppression.UNCHECKED)
    private static <T> boolean checkDataMap(
      final @NotNull ICompoundContainer<T> wrapper,
      final @NotNull IEquivalencyRecipe recipe,
      final @NotNull ICompoundType type,
      final Map<Class<?>, Set<IContributionInformationProvider<?>>> dataMap)
    {
        if (!dataMap.containsKey(wrapper.getContents().getClass()) && !dataMap.containsKey(Object.class))
        {
            return true;
        }

        return dataMap
                 .get(wrapper.getContents().getClass())
                 .stream()
                 .map(provider -> (IContributionInformationProvider<T>) provider)
                 .map(provider -> provider.canWrapperProvideCompoundForRecipe(wrapper, recipe, type))
                 .filter(Optional::isPresent)
                 .findFirst()
                 .orElseGet(() -> dataMap
                                    .get(Object.class)
                                    .stream()
                                    .map(provider -> (IContributionInformationProvider<T>) provider)
                                    .map(provider -> provider.canWrapperProvideCompoundForRecipe(wrapper, recipe, type))
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .findFirst())
                 .orElse(true);
    }
}
