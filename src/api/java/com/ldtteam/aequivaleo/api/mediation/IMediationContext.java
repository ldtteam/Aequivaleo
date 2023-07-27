package com.ldtteam.aequivaleo.api.mediation;

import java.util.Set;

/**
 * Represents a single mediation procedure and its environment.
 */
public interface IMediationContext
{

    /**
     * The {@link IMediationCandidate candidates} between which an {@link IMediationEngine engine} needs make a decision.
     *
     * @return The {@link Set set} of {@link IMediationCandidate candidates}.
     */
    Set<IMediationCandidate> getCandidates();

    /**
     * Can be used to determine if the target nodes parents are all analyzed already.
     *
     * Even though the analysis engine does its best to always analyze all parents of a node
     * before evaluating a given node, and then potentially requiring mediation. This is not
     * always possible if every recipe configuration. This method returns an indicator if the
     * current nodes parents where analyzed or not.
     * This does not take graph inheritance into account, so if any parent nodes parent where not
     * completely analyzed but the target nodes parents all where this method will still return
     * {@code True}.
     *
     * @return {@code True} when all parents where analyzed, {@code False} when not.
     */
    boolean areTargetParentsAnalyzed();
}
