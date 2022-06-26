package com.ldtteam.aequivaleo.testing.compound.container.testing;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

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
    public String getContents()
    {
        return content;
    }

    @Override
    public Double getContentsCount()
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

    public static class Factory implements ICompoundContainerFactory<String>
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
        public ICompoundContainer<String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return null;
        }

        @Override
        public JsonElement serialize(ICompoundContainer<String> src, Type typeOfSrc, JsonSerializationContext context) {
            return null;
        }

        @Override
        public void write(ICompoundContainer<String> object, FriendlyByteBuf buffer) {

        }

        @Override
        public ICompoundContainer<String> read(FriendlyByteBuf buffer) {
            return null;
        }
    }
}
