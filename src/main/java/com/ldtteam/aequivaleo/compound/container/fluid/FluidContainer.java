package com.ldtteam.aequivaleo.compound.container.fluid;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.dummy.Dummy;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.RegistryUtils;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Objects;

public class FluidContainer implements ICompoundContainer<Fluid>
{

    public static final class Factory extends ForgeRegistryEntry<ICompoundContainerFactory<?>> implements ICompoundContainerFactory<Fluid>
    {

        public Factory()
        {
            setRegistryName(Constants.MOD_ID, "fluid");
        }

        @NotNull
        @Override
        public Class<Fluid> getContainedType()
        {
            return Fluid.class;
        }

        @Override
        public ICompoundContainer<Fluid> create(@NotNull final Fluid instance, final double count)
        {
            return new FluidContainer(instance, count);
        }

        @Override
        public ICompoundContainer<Fluid> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
        {
            return new FluidContainer(Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(json.getAsJsonObject().get("fluid").getAsString()))), json.getAsJsonObject().get("count").getAsDouble());
        }

        @Override
        public JsonElement serialize(final ICompoundContainer<Fluid> src, final Type typeOfSrc, final JsonSerializationContext context)
        {
            final JsonObject object = new JsonObject();
            object.addProperty("count", src.getContentsCount());
            object.addProperty("fluid", Objects.requireNonNull(src.getContents().getRegistryName()).toString());
            return object;
        }

        @Override
        public void write(final ICompoundContainer<Fluid> object, final PacketBuffer buffer)
        {
            buffer.writeVarInt(RegistryUtils.getFull(Fluid.class).getID(object.getContents()));
            buffer.writeDouble(object.getContentsCount());
        }

        @Override
        public ICompoundContainer<Fluid> read(final PacketBuffer buffer)
        {
            return new FluidContainer(
              RegistryUtils.getFull(Fluid.class).getValue(buffer.readVarInt()),
              buffer.readDouble()
            );
        }
    }

    private final Fluid   fluid;
    private final double count;
    private final int hashCode;

    public FluidContainer(final Fluid fluid, final double count) {
        this.fluid = fluid;
        this.count = count;
        this.hashCode = Objects.requireNonNull(fluid.getRegistryName()).hashCode();
    }

    @Override
    public boolean isValid()
    {
        return !fluid.isEquivalentTo(Fluids.EMPTY);
    }

    @Override
    public Fluid getContents()
    {
        return fluid;
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
        return "fluid_" + Objects.requireNonNull(getContents().getRegistryName())
          .getNamespace() + "_" + getContents().getRegistryName().getPath();
    }

    @Override
    public int compareTo(@NotNull final ICompoundContainer<?> o)
    {
        //Dummies are after us. :D
        if (o instanceof Dummy)
            return -1;

        final Object contents = Validate.notNull(o.getContents());
        if (!(contents instanceof Fluid))
        {
            return Fluid.class.getName().compareTo(contents.getClass().getName());
        }

        final Fluid otherFluid = (Fluid) contents;
        return Objects.requireNonNull(otherFluid.getRegistryName()).compareTo(fluid.getRegistryName());
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof FluidContainer))
        {
            return false;
        }

        final FluidContainer that = (FluidContainer) o;

        if (Double.compare(that.count, count) != 0)
        {
            return false;
        }
        return Objects.equals(fluid.getRegistryName(), that.getContents().getRegistryName());
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    @Override
    public String toString()
    {
        return String.format("%s x Fluid: %s", count, fluid.getRegistryName());
    }
}
