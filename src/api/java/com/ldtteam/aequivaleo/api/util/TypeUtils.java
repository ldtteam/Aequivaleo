package com.ldtteam.aequivaleo.api.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class TypeUtils
{

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Cache<Class<?>, Set<Class<?>>> SUPER_TYPE_CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .maximumSize(150)
        .build();


    private TypeUtils()
    {
        throw new IllegalStateException("Tried to initialize: TypeUtils but this is a Utility class.");
    }

    public static Set<Class<?>> getAllSuperTypesExcludingObject(@NotNull final Class<?> clz)
    {
        try
        {
            return SUPER_TYPE_CACHE.get(clz, () -> getAllSuperTypesExcludingObjectInternal(clz));
        }
        catch (ExecutionException e)
        {
            LOGGER.fatal("Failed to query the super type cache. Execution got aborted. Attempting normal recovery.", e);
            return getAllSuperTypesExcludingObjectInternal(clz);
        }
    }

    private static Set<Class<?>> getAllSuperTypesExcludingObjectInternal(final Class<?> clz) {
        final Set<Class<?>> result = Sets.newHashSet();
        result.add(clz);

        final List<Class<?>> directSuperTypes = Lists.newArrayList();
        if (clz.getSuperclass() != null)
            directSuperTypes.addAll(Collections.singleton(clz.getSuperclass()));

        if (clz.getInterfaces().length > 0)
            directSuperTypes.addAll(Arrays.asList(clz.getInterfaces()));

        directSuperTypes.remove(Object.class);
        result.addAll(directSuperTypes.stream().map(TypeUtils::getAllSuperTypesExcludingObject).flatMap(Set::stream).collect(Collectors.toSet()));

        return result;
    }
}
