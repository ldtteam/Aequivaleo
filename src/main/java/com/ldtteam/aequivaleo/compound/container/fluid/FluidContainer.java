package com.ldtteam.aequivaleo.compound.container.fluid;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.dummy.Dummy;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.ldtteam.aequivaleo.bootstrap.ModContainerTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FluidContainer implements ICompoundContainer<Fluid>
{

    public static final Codec<FluidContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidContainer::contents),
            Codec.DOUBLE.fieldOf("count").forGetter(FluidContainer::contentsCount)
    ).apply(instance, FluidContainer::new));
    
    public static final class Type implements ICompoundContainerType<Fluid>
    {

        public Type()
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
        public Codec<? extends ICompoundContainer<Fluid>> codec() {
            return CODEC;
        }
    }

    private final Fluid   fluid;
    private final double count;
    private final int hashCode;

    public FluidContainer(final Fluid fluid, final double count) {
        this.fluid = fluid;
        this.count = count;
        this.hashCode = Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(fluid)).hashCode();
    }

    @Override
    public boolean isValid()
    {
        return !fluid.isSame(Fluids.EMPTY);
    }

    @Override
    public Fluid contents()
    {
        return fluid;
    }

    @Override
    public Double contentsCount()
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
                Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(contents())).getNamespace(),
                Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(contents())).getPath()
        );
    }
    
    @Override
    public ICompoundContainerType<Fluid> type() {
        return ModContainerTypes.FLUID.get();
    }
    
    @Override
    public int compareTo(@NotNull final ICompoundContainer<?> o)
    {
        //Dummies are after us. :D
        if (o instanceof Dummy)
            return -1;

        final Object contents = Objects.requireNonNull(o.contents());
        if (!(contents instanceof final Fluid otherFluid))
        {
            return Fluid.class.getName().compareTo(contents.getClass().getName());
        }

        return Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(otherFluid)).compareTo(Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(fluid)));
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
        return Objects.equals(Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(contents())), Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(that.contents())));
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    @Override
    public String toString()
    {
        return String.format("%s x Fluid: %s", count, Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(contents())));
    }
}
