package com.ldtteam.aequivaleo.testing.compound.container.testing;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

public class StringCompoundContainer implements ICompoundContainer<String>
{
    String content;
    double count;

    public StringCompoundContainer(String content, double count)
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
    public ICompoundContainerType<String> type() {
        return new Type();
    }
    
    @Override
    public int compareTo(@NotNull ICompoundContainer<?> o)
    {
        if (o == this)
        {
            return 0;
        }
        if (o instanceof StringCompoundContainer)
        {
            return content.compareTo(((StringCompoundContainer) o).content);
        }
        return -1;
    }

    public String toString()
    {
        return String.format("[s=\"%s\",c=%f]", content, count);
    }

    @Override
    public int hashCode()
    {
        return content.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof StringCompoundContainer)
        {
            return compareTo((StringCompoundContainer) obj) == 0;
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
            return new StringCompoundContainer(inputInstance, count);
        }
        
        @Override
        public Codec<? extends ICompoundContainer<String>> codec() {
            return Codec.unit(() -> {
                throw new IllegalArgumentException("Cannot deserialize a StringCompoundContainer");
            });
        }
    }
}
