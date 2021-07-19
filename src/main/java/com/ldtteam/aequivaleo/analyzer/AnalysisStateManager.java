package com.ldtteam.aequivaleo.analyzer;

import com.google.common.collect.Maps;
import com.ldtteam.aequivaleo.api.analysis.AnalysisState;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import java.util.Map;

public class AnalysisStateManager
{
    private static final Map<RegistryKey<World>, AnalysisState> STATE_MAP = Maps.newConcurrentMap();

    public static AnalysisState getState(final RegistryKey<World> key)
    {
        return STATE_MAP.getOrDefault(key, AnalysisState.UNINITIALIZED);
    }

    private AnalysisStateManager()
    {
    }

    public static void setState(final RegistryKey<World> key, final AnalysisState state) {
        STATE_MAP.put(key, state);
    }

    public static void setStateIfNotError(final RegistryKey<World> key, final AnalysisState state) {
        if (getState(key).isErrored())
            return;

        setState(key, state);
    }
}
