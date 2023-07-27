package com.ldtteam.aequivaleo.mediation;

import com.google.common.collect.ImmutableSet;
import com.ldtteam.aequivaleo.api.mediation.IMediationCandidate;
import com.ldtteam.aequivaleo.api.mediation.IMediationContext;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Supplier;

public class SimpleMediationContext implements IMediationContext
{
    @NotNull
    private final Set<IMediationCandidate> candidates;
    @NotNull
    private final Supplier<Boolean> areTargetParentsAnalyzedCallback;

    public SimpleMediationContext(@NotNull final Set<IMediationCandidate> candidates, @NotNull final Supplier<Boolean> areTargetParentsAnalyzedCallback) {
        this.candidates = candidates;
        this.areTargetParentsAnalyzedCallback = areTargetParentsAnalyzedCallback;
    }

    @Override
    public Set<IMediationCandidate> getCandidates()
    {
        return ImmutableSet.copyOf(candidates);
    }

    @Override
    public boolean areTargetParentsAnalyzed()
    {
        return areTargetParentsAnalyzedCallback.get();
    }
}
