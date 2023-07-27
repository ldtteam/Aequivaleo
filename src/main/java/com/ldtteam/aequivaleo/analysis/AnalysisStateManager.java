package com.ldtteam.aequivaleo.analysis;

import com.google.common.collect.Maps;
import com.ldtteam.aequivaleo.api.analysis.AnalysisState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

public class AnalysisStateManager
{
    private static final Map<ResourceKey<Level>, AnalysisState> STATE_MAP = Maps.newConcurrentMap();

    public static AnalysisState getState(final ResourceKey<Level> key)
    {
        return STATE_MAP.getOrDefault(key, AnalysisState.UNINITIALIZED);
    }

    private AnalysisStateManager()
    {
    }

    public static void setState(final ResourceKey<Level> key, final AnalysisState state) {
        STATE_MAP.put(key, state);
    }

    public static void setState(final List<? extends Level> worlds, final AnalysisState state) {
        worlds.forEach(world -> setState(world.dimension(), state));
    }

    public static void setStateIfNotError(final ResourceKey<Level> key, final AnalysisState state) {
        if (getState(key).isErrored())
            return;

        setState(key, state);
    }

    public static void setStateIfNotError(final List<? extends Level> worlds, final AnalysisState state) {
        worlds.forEach(world -> setStateIfNotError(world.dimension(), state));
    }
}
