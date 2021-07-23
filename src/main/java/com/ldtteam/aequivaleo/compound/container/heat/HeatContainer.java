package com.ldtteam.aequivaleo.compound.container.heat;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.heat.Heat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.HashMap;

public class HeatContainer implements ICompoundContainer<Heat>
{

    public static final class Factory extends ForgeRegistryEntry<ICompoundContainerFactory<?>> implements ICompoundContainerFactory<Heat>
    {

        public Factory()
        {
            setRegistryName(Constants.MOD_ID, "heat");
        }

        @NotNull
        @Override
        public Class<Heat> getContainedType()
        {
            return Heat.class;
        }

        @NotNull
        @Override
        public ICompoundContainer<Heat> create(@NotNull final Heat instance, @NotNull final double count)
        {
            return new HeatContainer(instance, count);
        }

        @Override
        public ICompoundContainer<Heat> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
        {
            return new HeatContainer(new Heat(), json.getAsDouble());
        }

        @Override
        public JsonElement serialize(final ICompoundContainer<Heat> src, final Type typeOfSrc, final JsonSerializationContext context)
        {
            return new JsonPrimitive(src.getContentsCount());
        }

        @Override
        public void write(final ICompoundContainer<Heat> object, final FriendlyByteBuf buffer)
        {
            buffer.writeDouble(object.getContentsCount());
        }

        @Override
        public ICompoundContainer<Heat> read(final FriendlyByteBuf buffer)
        {
            return new HeatContainer(new Heat(), buffer.readDouble());
        }
    }

    private final Heat heat;
    private final Double count;

    public HeatContainer(final Heat heat, final Double count)
    {
        this.heat = heat;
        this.count = count;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }

    @Override
    public Heat getContents()
    {
        return heat;
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
        return !(o instanceof HeatContainer) ? -1 : (int) (getContentsCount() - o.getContentsCount());
    }

    @Override
    public int hashCode()
    {
        return getContentsCount().hashCode();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof HeatContainer))
            return false;

        return ((HeatContainer) obj).getContentsCount().equals(getContentsCount());
    }

    @Override
    public String toString()
    {
        return String.format("%s x Heat", count);
    }
}
