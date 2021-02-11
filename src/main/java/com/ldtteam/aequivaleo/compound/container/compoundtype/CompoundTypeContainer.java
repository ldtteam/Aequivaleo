package com.ldtteam.aequivaleo.compound.container.compoundtype;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Objects;

public class CompoundTypeContainer implements ICompoundContainer<ICompoundType>
{

    public static final class Factory extends ForgeRegistryEntry<ICompoundContainerFactory<?>> implements ICompoundContainerFactory<ICompoundType>
    {

        public Factory()
        {
            setRegistryName(Constants.MOD_ID, "compound_type");
        }

        @NotNull
        @Override
        public Class<ICompoundType> getContainedType()
        {
            return ICompoundType.class;
        }

        @NotNull
        @Override
        public ICompoundContainer<ICompoundType> create(@NotNull final ICompoundType instance, @NotNull final double count)
        {
            return new CompoundTypeContainer(instance, count);
        }

        @Override
        public ICompoundContainer<ICompoundType> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
        {
            if (!json.isJsonObject())
                throw new JsonParseException("JSON for Container Type container needs to be an object.");

            final ResourceLocation typeName = new ResourceLocation(json.getAsJsonObject().get("compound_type").getAsString());
            final double amount = json.getAsJsonObject().get("count").getAsDouble();

            return new CompoundTypeContainer(ModRegistries.COMPOUND_TYPE.getValue(typeName), amount);
        }

        @Override
        public JsonElement serialize(final ICompoundContainer<ICompoundType> src, final Type typeOfSrc, final JsonSerializationContext context)
        {
            if (!src.isValid())
                throw new IllegalArgumentException("Can not serialize a container which is invalid.");

            final JsonObject result = new JsonObject();
            result.addProperty("type", Objects.requireNonNull(src.getContents().getRegistryName()).toString());
            result.addProperty("count", src.getContentsCount());

            return result;
        }

        @Override
        public void write(final ICompoundContainer<ICompoundType> object, final PacketBuffer buffer)
        {
            buffer.writeString(Objects.requireNonNull(object.getContents().getRegistryName()).toString());
            buffer.writeDouble(object.getContentsCount());
        }

        @Override
        public ICompoundContainer<ICompoundType> read(final PacketBuffer buffer)
        {
            return new CompoundTypeContainer(ModRegistries.COMPOUND_TYPE.getValue(new ResourceLocation(buffer.readString())), buffer.readDouble());
        }
    }

    private final ICompoundType type;
    private final Double        count;

    public CompoundTypeContainer(final ICompoundType type, final Double count)
    {
        this.type = type;
        this.count = count;
    }

    @Override
    public boolean isValid()
    {
        return type != null;
    }

    @Override
    public ICompoundType getContents()
    {
        return type;
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
        return "heat";
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
        return String.format("%s x %s", count, isValid() ? getContents().getRegistryName() : "<UNKNOWN>");
    }
}
