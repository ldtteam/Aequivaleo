package com.ldtteam.aequivaleo.testing.compound.container.testing;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class LoadableStringCompoundContainer implements ICompoundContainer<String>
{

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
            return new LoadableStringCompoundContainer(inputInstance, count);
        }

        @Override
        public ICompoundContainer<String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            return new LoadableStringCompoundContainer(json.getAsString(), 1);
        }

        @Override
        public JsonElement serialize(ICompoundContainer<String> src, Type typeOfSrc, JsonSerializationContext context) {
            final LoadableStringCompoundContainer container = (LoadableStringCompoundContainer) src;
            return new JsonPrimitive(container.content);
        }

        @Override
        public void write(ICompoundContainer<String> object, FriendlyByteBuf buffer) {
            final LoadableStringCompoundContainer container = (LoadableStringCompoundContainer) object;
            buffer.writeUtf(container.content);
        }

        @Override
        public ICompoundContainer<String> read(FriendlyByteBuf buffer) {
            return new LoadableStringCompoundContainer(buffer.readUtf(32767), 1d);
        }
    }
}
