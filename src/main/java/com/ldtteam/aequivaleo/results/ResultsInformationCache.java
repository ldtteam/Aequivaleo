package com.ldtteam.aequivaleo.results;

import com.google.common.collect.Maps;
import com.ldtteam.aequivaleo.api.compound.ICompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.results.IResultsInformationCache;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class ResultsInformationCache implements IResultsInformationCache
{

    private final        Map<ICompoundContainer<?>, Set<ICompoundInstance>> cacheData       = Maps.newConcurrentMap();
    private static final Map<World, ResultsInformationCache>                WORLD_INSTANCES = Maps.newConcurrentMap();

    private ResultsInformationCache()
    {

    }

    public static ResultsInformationCache getInstance(@NotNull final World world)
    {
        return WORLD_INSTANCES.computeIfAbsent(world, (dimType) -> new ResultsInformationCache());
    }

    @Override
    public Map<ICompoundContainer<?>, Set<ICompoundInstance>> get()
    {
        return cacheData;
    }

    public void set(@NotNull final Map<ICompoundContainer<?>, Set<ICompoundInstance>> data)
    {
        cacheData.clear();
        cacheData.putAll(data);
    }
}
