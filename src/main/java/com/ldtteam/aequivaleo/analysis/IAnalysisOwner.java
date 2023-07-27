package com.ldtteam.aequivaleo.analysis;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.io.File;

public interface IAnalysisOwner
{

    ResourceKey<Level> getIdentifier();

    File getCacheDirectory();
}
