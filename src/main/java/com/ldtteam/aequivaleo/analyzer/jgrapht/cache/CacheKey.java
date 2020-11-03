package com.ldtteam.aequivaleo.analyzer.jgrapht.cache;

import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.IGraph;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CacheKey
{

    private final Map<String, String> modVersions;
    private final IGraph              graph;

    public CacheKey(final ModList modList, final IGraph graph)
    {
        Validate.notNull(modList);
        Validate.notNull(graph);

        this.modVersions = modList.getMods().stream().collect(Collectors.toMap(
          ModInfo::getModId,
          i -> i.getVersion().toString()
        ));
        this.graph = graph;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof CacheKey))
        {
            return false;
        }
        final CacheKey cacheKey = (CacheKey) o;
        return Objects.equals(modVersions, cacheKey.modVersions) &&
                 Objects.equals(graph, cacheKey.graph);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(modVersions, graph);
    }
}
