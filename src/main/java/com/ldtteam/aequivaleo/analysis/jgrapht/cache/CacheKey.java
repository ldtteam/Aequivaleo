package com.ldtteam.aequivaleo.analysis.jgrapht.cache;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.commons.lang3.Validate;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CacheKey
{

    private final Map<String, String> modVersions;

    public CacheKey(final ModList modList)
    {
        Validate.notNull(modList);

        this.modVersions = modList.getMods().stream().collect(Collectors.toMap(
          IModInfo::getModId,
          i -> i.getVersion().toString()
        ));
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
        return Objects.equals(modVersions, cacheKey.modVersions);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(modVersions);
    }

    @SuppressWarnings("UnstableApiUsage")
    public String hash()
    {
        final Hasher hasher = Hashing.sha256().newHasher();

        modVersions.forEach((k, v) -> hasher.putString(k, Charset.defaultCharset()).putString(v, Charset.defaultCharset()));

        return hasher.hash().toString();
    }
}
