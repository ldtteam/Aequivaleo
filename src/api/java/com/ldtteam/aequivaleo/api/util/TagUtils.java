package com.ldtteam.aequivaleo.api.util;

import com.google.common.collect.Lists;
import net.minecraft.tags.*;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public final class TagUtils
{

    private TagUtils()
    {
        throw new IllegalStateException("Tried to initialize: TagUtils but this is a Utility class.");
    }

    public static List<ITag.INamedTag<?>> get(@NotNull final ResourceLocation name)
    {
        final List<ITag.INamedTag<?>> result = Lists.newArrayList();

        getTag(BlockTags.getCollection(), name).ifPresent(result::add);
        getTag(ItemTags.getCollection(), name).ifPresent(result::add);
        getTag(EntityTypeTags.getCollection(), name).ifPresent(result::add);
        getTag(FluidTags.getCollection(), name).ifPresent(result::add);

        return result;
    }

    public static <T> Optional<ITag.INamedTag<T>> getTag(final TagCollection<T> tTagCollection, final ResourceLocation location) {
        return Optional.ofNullable(tTagCollection.getTagMap().get(location))
                 .filter(ITag.INamedTag.class::isInstance)
                 .map(tag -> (ITag.INamedTag<T>) tag);
    }
}
