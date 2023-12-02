package com.ldtteam.aequivaleo.analysis.jgrapht.cache;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
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
          IModInfo::getModId,
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
        if (!(o instanceof final CacheKey cacheKey))
        {
            return false;
        }
        return Objects.equals(modVersions, cacheKey.modVersions) &&
                 Objects.equals(graph, cacheKey.graph);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(modVersions, graph);
    }
}
