package com.ldtteam.aequivaleo.compound.container.tag;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

@SuppressWarnings("rawtypes")
public class TagContainer implements ICompoundContainer<TagKey<?>>
{

    @SuppressWarnings("unchecked")
    public static final class Factory extends ForgeRegistryEntry<ICompoundContainerFactory<?>> implements ICompoundContainerFactory<TagKey<?>>
    {

        public Factory()
        {
            setRegistryName(Constants.MOD_ID, "tag");
        }

        @NotNull
        @Override
        public Class<TagKey<?>> getContainedType()
        {
            return (Class<TagKey<?>>) (Class) TagKey.class;
        }

        @NotNull
        @Override
        public ICompoundContainer<TagKey<?>> create(@NotNull final TagKey<?> instance, @NotNull final double count)
        {
            return new TagContainer(instance, count);
        }

        @Override
        public ICompoundContainer<TagKey<?>> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
        {
            if (!json.isJsonObject())
                throw new JsonParseException("JSON for tag container needs to be an object.");

            final ResourceLocation tagType = new ResourceLocation(json.getAsJsonObject().get("tagType").getAsString());
            final ResourceLocation tagName = new ResourceLocation(json.getAsJsonObject().get("tagName").getAsString());
            final double amount = json.getAsJsonObject().get("count").getAsDouble();

            TagKey<?> key = TagKey.create(ResourceKey.createRegistryKey(tagType), tagName);
            return new TagContainer(key, amount);
        }

        @Override
        public JsonElement serialize(final ICompoundContainer<TagKey<?>> src, final Type typeOfSrc, final JsonSerializationContext context)
        {
            if (!src.isValid())
                throw new IllegalArgumentException("Can not serialize a container which is invalid.");

            final JsonObject result = new JsonObject();
            result.addProperty("tagType", src.getContents().registry().location().toString());
            result.addProperty("tagName", src.getContents().location().toString());
            result.addProperty("count", src.getContentsCount());

            return result;
        }

        @Override
        public void write(final ICompoundContainer<TagKey<?>> object, final FriendlyByteBuf buffer)
        {
            buffer.writeUtf(object.getContents().registry().location().toString());
            buffer.writeUtf(object.getContents().location().toString());
            buffer.writeDouble(object.getContentsCount());
        }

        @Override
        public ICompoundContainer<TagKey<?>> read(final FriendlyByteBuf buffer)
        {
            final ResourceLocation tagType = new ResourceLocation(buffer.readUtf(32767));
            final ResourceLocation tagName = new ResourceLocation(buffer.readUtf(32767));
            final double amount = buffer.readDouble();

            TagKey<?> key = TagKey.create(ResourceKey.createRegistryKey(tagType), tagName);
            return new TagContainer(key, amount);
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
    public TagKey<?> getContents()
    {
        return tag;
    }

    @Override
    public Double getContentsCount()
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
    public int compareTo(@NotNull final ICompoundContainer<?> o)
    {
        if (!(o instanceof final TagContainer other))
            return -1;

        final int tagTypeResult = tag.registry().compareTo(other.tag.registry());
        if (tagTypeResult != 0)
            return tagTypeResult;

        final int tagNameResult = getContents().location().compareTo(other.getContents().location());
        if (tagNameResult != 0)
            return tagNameResult;

        return (int) (getContentsCount() - other.getContentsCount());
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

        if (!other.getContentsCount().equals(this.getContentsCount()))
            return false;

        if (!otherCollection.equals(tag.registry().location()))
            return false;

        return tag.location().equals(other.tag.location());
    }

    @Override
    public String toString()
    {
        return String.format("%s x %s", count, isValid() ? "[" + getContents().registry().location().toString() + "]" + getContents().location().toString() : "<UNKNOWN>");
    }
}