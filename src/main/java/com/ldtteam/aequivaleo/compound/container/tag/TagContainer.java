package com.ldtteam.aequivaleo.compound.container.tag;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.TagUtils;
import com.ldtteam.aequivaleo.compound.container.compoundtype.CompoundTypeContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("rawtypes")
public class TagContainer implements ICompoundContainer<Tag.Named>
{

    public static final class Factory extends ForgeRegistryEntry<ICompoundContainerFactory<?>> implements ICompoundContainerFactory<Tag.Named>
    {

        public Factory()
        {
            setRegistryName(Constants.MOD_ID, "tag");
        }

        @NotNull
        @Override
        public Class<Tag.Named> getContainedType()
        {
            return Tag.Named.class;
        }

        @NotNull
        @Override
        public ICompoundContainer<Tag.Named> create(@NotNull final Tag.Named instance, @NotNull final double count)
        {
            return new TagContainer(instance, count);
        }

        @Override
        public ICompoundContainer<Tag.Named> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
        {
            if (!json.isJsonObject())
                throw new JsonParseException("JSON for tag container needs to be an object.");

            final ResourceLocation tagType = new ResourceLocation(json.getAsJsonObject().get("tagType").getAsString());
            final ResourceLocation tagName = new ResourceLocation(json.getAsJsonObject().get("tagName").getAsString());
            final double amount = json.getAsJsonObject().get("count").getAsDouble();

            Optional<Tag.Named<?>> tag = TagUtils.getTag(tagType, tagName);
            return new TagContainer(tag.orElseGet(() -> ForgeTagHandler.createOptionalTag(tagType, tagName)), amount);
        }

        @Override
        public JsonElement serialize(final ICompoundContainer<Tag.Named> src, final Type typeOfSrc, final JsonSerializationContext context)
        {
            if (!src.isValid())
                throw new IllegalArgumentException("Can not serialize a container which is invalid.");


            final JsonObject result = new JsonObject();
            result.addProperty("tagType", TagUtils.getTagCollectionName(src.getContents()).toString());
            result.addProperty("tagName", Objects.requireNonNull(src.getContents().getName()).toString());
            result.addProperty("count", src.getContentsCount());

            return result;
        }

        @Override
        public void write(final ICompoundContainer<Tag.Named> object, final FriendlyByteBuf buffer)
        {
            buffer.writeUtf(TagUtils.getTagCollectionName(object.getContents()).toString());
            buffer.writeUtf(object.getContents().toString());
            buffer.writeDouble(object.getContentsCount());
        }

        @Override
        public ICompoundContainer<Tag.Named> read(final FriendlyByteBuf buffer)
        {
            final ResourceLocation tagType = new ResourceLocation(buffer.readUtf(32767));
            final ResourceLocation tagName = new ResourceLocation(buffer.readUtf(32767));
            final double amount = buffer.readDouble();

            Optional<Tag.Named<?>> tag = TagUtils.getTag(tagType, tagName);
            return new TagContainer(tag.orElseGet(() -> ForgeTagHandler.createOptionalTag(tagType, tagName)), amount);
        }
    }

    private final Tag.Named   tag;
    private final Double count;

    public TagContainer(final Tag.Named tag, final Double count)
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
    public Tag.Named getContents()
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
        return TagUtils.getTagCollectionName(getContents()).toString().replace(":", "_") + "_" + getContents().getName().toString().replace(":", "_");
    }

    @Override
    public int compareTo(@NotNull final ICompoundContainer<?> o)
    {
        return !(o instanceof CompoundTypeContainer) ? -1 : (int) (getContentsCount() - o.getContentsCount());
    }

    @Override
    public int hashCode()
    {
        return getContentsCount().hashCode();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof CompoundTypeContainer))
            return false;

        return ((CompoundTypeContainer) obj).getContentsCount().equals(getContentsCount());
    }

    @Override
    public String toString()
    {
        return String.format("%s x %s", count, isValid() ? "[" + TagUtils.getTagCollectionName(getContents()).toString() + "]" + getContents().getName().toString() : "<UNKNOWN>");
    }
}