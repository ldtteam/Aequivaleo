package com.ldtteam.aequivaleo.api.compound.information.validity;

import com.ldtteam.aequivaleo.api.compound.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiFunction;

public class SimpleBiFunctionBasedValidCompoundTypeInformationProvider<T> implements IValidCompoundTypeInformationProvider<T>
{
    private final Class<T>                                                            clazz;
    private final BiFunction<ICompoundContainer<T>, ICompoundType, Optional<Boolean>> biFunction;

    public SimpleBiFunctionBasedValidCompoundTypeInformationProvider(
      final Class<T> clazz,
      final BiFunction<ICompoundContainer<T>, ICompoundType, Optional<Boolean>> biFunction) {
        this.clazz = clazz;
        this.biFunction = biFunction;
    }

    /**
     * Indicates which type of content is contained in the wrapper.
     *
     * @return The class of the contained type.
     */
    @Override
    public Class<T> getWrappedContentType()
    {
        return clazz;
    }

    /**
     * Indicates to the generation system if it is even possible for a given wrapper instance to have a given compound type.
     *
     * @param wrapper The wrapper in question.
     * @param type    The type to check for.
     * @return An optional with nothing contained, indicating no preference. Or true or false when a preference is locked.
     */
    @Override
    public Optional<Boolean> canWrapperHaveCompound(
      @NotNull final ICompoundContainer<T> wrapper, @NotNull final ICompoundType type)
    {
        return biFunction.apply(wrapper, type);
    }
}
