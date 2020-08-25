package com.ldtteam.aequivaleo.api.compound;

import org.jetbrains.annotations.NotNull;

public interface ICompoundInstance extends Comparable<ICompoundInstance>
{

    /**
     * Returns the type of the instance.
     *
     * @return The type.
     */
    @NotNull
    ICompoundType getType();

    /**
     * Returns the amount stored in this instance.
     *
     * @return The amount.
     */
    @NotNull
    Double getAmount();

}
