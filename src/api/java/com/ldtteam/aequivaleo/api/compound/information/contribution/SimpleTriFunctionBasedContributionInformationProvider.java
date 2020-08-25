package com.ldtteam.aequivaleo.api.compound.information.contribution;

import com.ldtteam.aequivaleo.api.compound.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.util.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SimpleTriFunctionBasedContributionInformationProvider<T> implements IContributionInformationProvider<T>
{
    private final Class<T> clzType;
    private final TriFunction<ICompoundContainer<T>, IEquivalencyRecipe, ICompoundType, Optional<Boolean>> callback;

    public SimpleTriFunctionBasedContributionInformationProvider(
      final Class<T> clzType,
      final TriFunction<ICompoundContainer<T>, IEquivalencyRecipe, ICompoundType, Optional<Boolean>> callback) {
        this.clzType = clzType;
        this.callback = callback;
    }

    @Override
    public Class<T> getWrappedContentType()
    {
        return clzType;
    }

    @Override
    public Optional<Boolean> canWrapperProvideCompoundForRecipe(
      @NotNull final ICompoundContainer<T> wrapper, @NotNull final IEquivalencyRecipe recipe, @NotNull final ICompoundType type)
    {
        return callback.apply(wrapper, recipe, type);
    }


}
