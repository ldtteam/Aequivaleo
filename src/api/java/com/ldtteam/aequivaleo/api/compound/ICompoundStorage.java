package com.ldtteam.aequivaleo.api.compound;

import javax.annotation.Nullable;

/**
 * A storage for compounds.
 * Should be attached as a capability.
 */
public interface ICompoundStorage
{

    /**
     * Fills up this storage with the give compound.
     *
     * @param compoundInstance The instance to fill this storage with.
     * @param simulate True to simulate the filling.
     *
     * @return The amount that would have been / is filled into this storage.
     */
    Double fill(CompoundInstance compoundInstance, boolean simulate);

    /**
     * Drains the storage with the given amount.
     * The type returned depends on the implementation.
     *
     * @param amount The amount to drain.
     * @param simulate True to simulate the draining.
     *
     * @return The instance that was drained.
     */
    @Nullable
    CompoundInstance drain(Double amount, boolean simulate);
}
