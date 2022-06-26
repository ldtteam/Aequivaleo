package com.ldtteam.aequivaleo.compound.container.fluid;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.dummy.Dummy;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.RegistryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Objects;

public class FluidContainer implements ICompoundContainer<Fluid>
{

    public static final class Factory implements ICompoundContainerFactory<Fluid>
    {

        public Factory()
        {
        }

        @NotNull
        @Override
        public Class<Fluid> getContainedType()
        {
            return Fluid.class;
        }

        @Override
        public @NotNull ICompoundContainer<Fluid> create(@NotNull final Fluid instance, final double count)
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
            object.addProperty("fluid", Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(src.getContents())).toString());
            return object;
        }

        @Override
        public void write(final ICompoundContainer<Fluid> object, final FriendlyByteBuf buffer)
        {
            buffer.writeVarInt(RegistryUtils.getFull(ForgeRegistries.FLUIDS.getRegistryKey()).getID(object.getContents()));
            buffer.writeDouble(object.getContentsCount());
        }

        @Override
        public ICompoundContainer<Fluid> read(final FriendlyByteBuf buffer)
        {
            return new FluidContainer(
              RegistryUtils.getFull(ForgeRegistries.FLUIDS.getRegistryKey()).getValue(buffer.readVarInt()),
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
        this.hashCode = Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluid)).hashCode();
    }

    @Override
    public boolean isValid()
    {
        return !fluid.isSame(Fluids.EMPTY);
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
        return "fluid_%s_%s".formatted(
                Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(getContents())).getNamespace(),
                Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(getContents())).getPath()
        );
    }

    @Override
    public int compareTo(@NotNull final ICompoundContainer<?> o)
    {
        //Dummies are after us. :D
        if (o instanceof Dummy)
            return -1;

        final Object contents = Validate.notNull(o.getContents());
        if (!(contents instanceof final Fluid otherFluid))
        {
            return Fluid.class.getName().compareTo(contents.getClass().getName());
        }

        return Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(otherFluid)).compareTo(Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluid)));
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof final FluidContainer that))
        {
            return false;
        }

        if (Double.compare(that.count, count) != 0)
        {
            return false;
        }
        return Objects.equals(Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(getContents())), Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(that.getContents())));
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    @Override
    public String toString()
    {
        return String.format("%s x Fluid: %s", count, Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(getContents())));
    }
}
