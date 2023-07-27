package com.ldtteam.aequivaleo.api.mediation;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * A engine that is queried by the analysis engine to handle cases
 * where multiple candidate values are present for a given node and a given compound type.
 *
 * The compound types are grouped by the compound type group into sets and then passed
 * to this engine (acquired via the compound type group) in the mediation context.
 */
public interface IMediationEngine
{

    /**
     * Invoked to determine a unique and deterministic result in the case where
     * multiple candidate values are present for a given node and a given compound type.
     *
     * @param context The context.
     * @return The set of compound instances that the target node should have.
     */
    @NotNull
    Optional<Set<CompoundInstance>> determineMediationResult(@NotNull final IMediationContext context);
}
