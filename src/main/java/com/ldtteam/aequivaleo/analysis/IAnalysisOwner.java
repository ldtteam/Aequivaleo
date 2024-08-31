package com.ldtteam.aequivaleo.analysis;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.io.File;

/**
 * Interface for objects that own an analysis.
 */
public interface IAnalysisOwner
{

    /**
     * The identifier of the analysis owner.
     *
     * @return The identifier of the analysis owner.
     */
    ResourceKey<Level> getIdentifier();

    /**
     * The cache directory for the analysis owner.
     *
     * @return The cache directory for the analysis owner.
     */
    File getCacheDirectory();
}
