package com.ldtteam.aequivaleo.compound.container.compoundtype;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.bootstrap.ModContainerTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class CompoundTypeContainer implements ICompoundContainer<ICompoundType>
{
    
    public static final Codec<CompoundTypeContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      ModRegistries.COMPOUND_TYPE.getEntryCodec().fieldOf("compound_type").forGetter(CompoundTypeContainer::contents),
      Codec.DOUBLE.fieldOf("count").forGetter(CompoundTypeContainer::contentsCount)
    ).apply(instance, CompoundTypeContainer::new));

    public static final class Type implements ICompoundContainerType<ICompoundType>
    {

        public Type()
        {
        }

        @NotNull
        @Override
        public Class<ICompoundType> getContainedType()
        {
            return ICompoundType.class;
        }

        @NotNull
        @Override
        public ICompoundContainer<ICompoundType> create(@NotNull final ICompoundType instance, final double count)
        {
            return new CompoundTypeContainer(instance, count);
        }
        
        @Override
        public Codec<? extends ICompoundContainer<ICompoundType>> codec() {
            return CODEC;
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
    public ICompoundType contents()
    {
        return type;
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
        return ModRegistries.COMPOUND_TYPE.getKey(contents()).toString().replace(":", "_");
    }
    
    @Override
    public ICompoundContainerType<ICompoundType> type() {
        return ModContainerTypes.COMPOUND_TYPE.get();
    }
    
    @Override
    public int compareTo(@NotNull final ICompoundContainer<?> o)
    {
        return !(o instanceof CompoundTypeContainer) ? -1 : (int) (contentsCount() - o.contentsCount());
    }

    @Override
    public int hashCode()
    {
        return contentsCount().hashCode();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof CompoundTypeContainer))
            return false;

        return ((CompoundTypeContainer) obj).contentsCount().equals(contentsCount());
    }

    @Override
    public String toString()
    {
        return String.format("%s x %s", count, isValid() ? ModRegistries.COMPOUND_TYPE.getKey(contents()) : "<UNKNOWN>");
    }
}
