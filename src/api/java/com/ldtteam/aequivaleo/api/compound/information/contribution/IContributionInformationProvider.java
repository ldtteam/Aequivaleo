package com.ldtteam.aequivaleo.api.compound.information.contribution;

import com.ldtteam.aequivaleo.api.compound.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface IContributionInformationProvider<T>
{

    /**
     * Indicates which type of content is contained in the wrapper.
     *
     * @return The class of the contained type.
     */
    Class<T> getWrappedContentType();

    /**
     * Indicates to the generation system if it is even possible for a given wrapper instance to provide a given compound as an input to or output of a given recipe.
     *
     * @param wrapper The wrapper in question.
     * @param recipe The recipe in question.
     * @param type The type to check for.
     *
     * @return An optional with nothing contained, indicating no preference. Or true or false when a preference is locked.
     */
    Optional<Boolean> canWrapperProvideCompoundForRecipe(@NotNull final ICompoundContainer<T> wrapper, @NotNull IEquivalencyRecipe recipe, @NotNull final ICompoundType type);
}
