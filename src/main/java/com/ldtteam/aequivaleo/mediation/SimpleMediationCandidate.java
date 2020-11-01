package com.ldtteam.aequivaleo.mediation;

import com.google.common.collect.ImmutableSet;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.mediation.IMediationCandidate;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Supplier;

public class SimpleMediationCandidate implements IMediationCandidate
{

    @NotNull
    private final Set<CompoundInstance> values;
    @NotNull
    private final Supplier<Boolean> isSourceIncompleteCallback;

    public SimpleMediationCandidate(@NotNull final Set<CompoundInstance> values, @NotNull final Supplier<Boolean> isSourceIncompleteCallback) {
        this.values = values;
        this.isSourceIncompleteCallback = isSourceIncompleteCallback;
    }

    @Override
    public Set<CompoundInstance> getValues()
    {
        return ImmutableSet.copyOf(values);
    }

    @Override
    public boolean isSourceIncomplete()
    {
        return isSourceIncompleteCallback.get();
    }
}
