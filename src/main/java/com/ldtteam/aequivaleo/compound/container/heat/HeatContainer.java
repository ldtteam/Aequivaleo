package com.ldtteam.aequivaleo.compound.container.heat;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.ldtteam.aequivaleo.bootstrap.ModContainerTypes;
import com.ldtteam.aequivaleo.heat.Heat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class HeatContainer implements ICompoundContainer<Heat>
{
    
    public static final Codec<HeatContainer> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.unit(new Heat()).fieldOf("heat").forGetter(HeatContainer::contents),
            Codec.DOUBLE.fieldOf("count").forGetter(HeatContainer::contentsCount)
    ).apply(instance, HeatContainer::new));

    public static final class Type implements ICompoundContainerType<Heat>
    {

        public Type()
        {
        }

        @NotNull
        @Override
        public Class<Heat> getContainedType()
        {
            return Heat.class;
        }

        @NotNull
        @Override
        public ICompoundContainer<Heat> create(@NotNull final Heat instance, final double count)
        {
            return new HeatContainer(instance, count);
        }
        
        @Override
        public Codec<? extends ICompoundContainer<Heat>> codec() {
            return CODEC;
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
    public Heat contents()
    {
        return heat;
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
        return "heat";
    }
    
    @Override
    public ICompoundContainerType<Heat> type() {
        return ModContainerTypes.HEAT.get();
    }
    
    @Override
    public int compareTo(@NotNull final ICompoundContainer<?> o)
    {
        return !(o instanceof HeatContainer) ? -1 : (int) (contentsCount() - o.contentsCount());
    }

    @Override
    public int hashCode()
    {
        return contentsCount().hashCode();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof HeatContainer))
            return false;

        return ((HeatContainer) obj).contentsCount().equals(contentsCount());
    }

    @Override
    public String toString()
    {
        return String.format("%s x Heat", count);
    }
}
