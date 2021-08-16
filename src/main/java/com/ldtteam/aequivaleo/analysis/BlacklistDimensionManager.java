package com.ldtteam.aequivaleo.analysis;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.analysis.IBlacklistDimensionManager;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class BlacklistDimensionManager implements IBlacklistDimensionManager
{
    private static final BlacklistDimensionManager INSTANCE = new BlacklistDimensionManager();

    public static BlacklistDimensionManager getInstance()
    {
        return INSTANCE;
    }

    private final Set<ResourceLocation> programmaticallyBlacklistedDimensions = Sets.newConcurrentHashSet();

    private BlacklistDimensionManager()
    {
    }

    @Override
    public IBlacklistDimensionManager blacklist(final ResourceLocation worldKey)
    {
        programmaticallyBlacklistedDimensions.add(worldKey);
        return this;
    }

    @Override
    public boolean isBlacklisted(final ResourceLocation worldKey)
    {
        return programmaticallyBlacklistedDimensions.contains(worldKey) || Aequivaleo.getInstance().getConfiguration().getCommon().blackListedDimensions.get().contains(worldKey.toString());
    }
}
