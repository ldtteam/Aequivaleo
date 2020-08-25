package com.ldtteam.aequivaleo.api.compound.information;

import com.ldtteam.aequivaleo.api.compound.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * An interface that describes if a given wrapper of type T is allowed to have the given compound type or not.
 * This helps the analysis engine determine if a given compound type of an ingredient should be passed on to the recipe output or not.
 *
 * @param <T> The type of game object for which this provider can provide information.
 */
public interface IValidCompoundTypeInformationProvider<T>
{

    /**
     * Indicates which type of content is contained in the wrapper.
     *
     * @return The class of the contained type.
     */
    Class<T> getWrappedContentType();

    /**
     * Indicates to the generation system if it is even possible for a given wrapper instance to have a given compound type.
     *
     * @param wrapper The wrapper in question.
     * @param type The type to check for.
     *
     * @return An optional with nothing contained, indicating no preference. Or true or false when a preference is locked.
     */
    Optional<Boolean> canWrapperHaveCompound(@NotNull final ICompoundContainer<T> wrapper, @NotNull final ICompoundType type);
}
