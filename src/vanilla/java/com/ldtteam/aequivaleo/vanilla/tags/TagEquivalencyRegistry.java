package com.ldtteam.aequivaleo.vanilla.tags;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.vanilla.api.tags.ITagEquivalencyRegistry;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class TagEquivalencyRegistry implements ITagEquivalencyRegistry
{
    private static final TagEquivalencyRegistry INSTANCE = new TagEquivalencyRegistry();

    public static TagEquivalencyRegistry getInstance()
    {
        return INSTANCE;
    }

    private final Set<TagKey<?>> tags = Sets.newConcurrentHashSet();

    private TagEquivalencyRegistry()
    {
    }

    @Override
    public ITagEquivalencyRegistry addTag(@NotNull final TagKey<?> tag)
    {
        this.tags.add(tag);
        return this;
    }

    public Set<TagKey<?>> getTags()
    {
        return tags;
    }
}
