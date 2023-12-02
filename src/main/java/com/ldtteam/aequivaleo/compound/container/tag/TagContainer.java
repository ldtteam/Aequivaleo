package com.ldtteam.aequivaleo.compound.container.tag;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.ldtteam.aequivaleo.bootstrap.ModContainerTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("rawtypes")
public class TagContainer implements ICompoundContainer<TagKey<?>>
{
    public static final Codec<TagContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("tagType").forGetter(container -> container.contents().registry().location()),
            ResourceLocation.CODEC.fieldOf("tagName").forGetter(container -> container.contents().location()),
            Codec.DOUBLE.fieldOf("count").forGetter(TagContainer::contentsCount)
    ).apply(instance, (type, name, count) -> {
        TagKey<?> key = TagKey.create(ResourceKey.createRegistryKey(type), name);
        return new TagContainer(key, count);
    }));
    

    @SuppressWarnings("unchecked")
    public static final class Type implements ICompoundContainerType<TagKey<?>>
    {

        public Type()
        {
        }

        @NotNull
        @Override
        public Class<TagKey<?>> getContainedType()
        {
            return (Class<TagKey<?>>) (Class) TagKey.class;
        }

        @NotNull
        @Override
        public ICompoundContainer<TagKey<?>> create(@NotNull final TagKey<?> instance, final double count)
        {
            return new TagContainer(instance, count);
        }
        
        @Override
        public Codec<? extends ICompoundContainer<TagKey<?>>> codec() {
            return CODEC;
        }
    }

    private final TagKey<?> tag;
    private final Double count;

    public TagContainer(final TagKey<?> tag, final Double count)
    {
        this.tag = tag;
        this.count = count;
    }

    @Override
    public boolean isValid()
    {
        return tag != null;
    }

    @Override
    public TagKey<?> contents()
    {
        return tag;
    }

    @Override
    public Double contentsCount()
    {
        return count;
    }

    @Override
    public boolean canBeLoadedFromDisk()
    {
        return true;
    }

    @Override
    public String getContentAsFileName()
    {
        return tag.registry().location().toString().replace(":", "_") + "_" + tag.location().toString().replace(":", "_");
    }
    
    @Override
    public ICompoundContainerType<TagKey<?>> type() {
        return ModContainerTypes.TAG.get();
    }
    
    @Override
    public int compareTo(@NotNull final ICompoundContainer<?> o)
    {
        if (!(o instanceof final TagContainer other))
            return -1;

        final int tagTypeResult = tag.registry().compareTo(other.tag.registry());
        if (tagTypeResult != 0)
            return tagTypeResult;

        final int tagNameResult = contents().location().compareTo(other.contents().location());
        if (tagNameResult != 0)
            return tagNameResult;

        return (int) (contentsCount() - other.contentsCount());
    }

    @Override
    public int hashCode()
    {
        int result = tag.hashCode();
        result = 31 * result + count.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof final TagContainer other))
            return false;

        final ResourceLocation otherCollection = other.tag.registry().location();

        if (!other.contentsCount().equals(this.contentsCount()))
            return false;

        if (!otherCollection.equals(tag.registry().location()))
            return false;

        return tag.location().equals(other.tag.location());
    }

    @Override
    public String toString()
    {
        return String.format("%s x %s", count, isValid() ? "[" + contents().registry().location() + "]" + contents().location() : "<UNKNOWN>");
    }
}