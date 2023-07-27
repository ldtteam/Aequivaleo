package com.ldtteam.aequivaleo.api.mediation;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;

import java.util.Set;

/**
 * Represents a single candidate that the mediation API endpoint can choose between.
 */
public interface IMediationCandidate
{

    /**
     * The values of compounds that this candidate holds.
     *
     * @return A set of compound instances that represent this candidate.
     */
    Set<CompoundInstance> getValues();

    /**
     * Indicates if the source of the {@link #getValues() values} is incomplete or not.
     * This indicator is transient, and gets passed along the entire graph.
     *
     * A source node is considered incomplete if any of the following three predicates are true:
     *  - A node has no result set (Not analyzed node, or root node with out value set)
     *  - A node has an empty result set (Node with failed mediation)
     *  - A nodes parents is incomplete (transient)
     *
     * @return {@code True} when the source that provided these values was incomplete {@code False} when not.
     */
    boolean isSourceIncomplete();
}
