package com.ldtteam.aequivaleo.testing.compound.container.testing;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class LoadableStringCompoundContainer implements ICompoundContainer<String>
{
    
    public static final Codec<LoadableStringCompoundContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("content").forGetter(LoadableStringCompoundContainer::contents),
            Codec.DOUBLE.fieldOf("count").forGetter(LoadableStringCompoundContainer::contentsCount)
    ).apply(instance, LoadableStringCompoundContainer::new));
    
    String content;
    double count;

    public LoadableStringCompoundContainer(String content, double count)
    {
        this.content = content;
        this.count = count;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }

    @Override
    public String contents()
    {
        return content;
    }

    @Override
    public Double contentsCount()
    {
        return count;
    }

    @Override
    public int compareTo(@NotNull ICompoundContainer<?> o)
    {
        if (o == this)
        {
            return 0;
        }
        if (o instanceof LoadableStringCompoundContainer)
        {
            return content.compareTo(((LoadableStringCompoundContainer) o).content);
        }
        return -1;
    }

    public String toString()
    {
        return String.format("[s=\"%s\",c=%f]", content, count);
    }

    @Override
    public boolean canBeLoadedFromDisk()
    {
        return true;
    }

    @Override
    public String getContentAsFileName()
    {
        return content;
    }
    
    @Override
    public ICompoundContainerType<String> type() {
        return new Type();
    }
    
    @Override
    public int hashCode()
    {
        return content.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof LoadableStringCompoundContainer)
        {
            return compareTo((LoadableStringCompoundContainer) obj) == 0;
        }
        return false;
    }

    public static class Type implements ICompoundContainerType<String>
    {
        @NotNull
        @Override
        public Class<String> getContainedType() {
            return String.class;
        }

        @NotNull
        @Override
        public ICompoundContainer<String> create(@NotNull String inputInstance, double count) {
            return new LoadableStringCompoundContainer(inputInstance, count);
        }
        
        @Override
        public Codec<? extends ICompoundContainer<String>> codec() {
            return CODEC;
        }
    }
}
