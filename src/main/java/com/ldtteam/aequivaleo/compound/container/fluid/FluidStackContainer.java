package com.ldtteam.aequivaleo.compound.container.fluid;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.dummy.Dummy;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.ldtteam.aequivaleo.api.util.Comparators;
import com.ldtteam.aequivaleo.api.util.FluidStackUtils;
import com.ldtteam.aequivaleo.bootstrap.ModContainerTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FluidStackContainer implements ICompoundContainer<FluidStack>
{
    
    public static final Codec<FluidStackContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      FluidStack.CODEC.fieldOf("stack").forGetter(FluidStackContainer::contents),
      Codec.DOUBLE.fieldOf("count").forGetter(FluidStackContainer::contentsCount)
    ).apply(instance, FluidStackContainer::new));

    public static final class Type implements ICompoundContainerType<FluidStack>
    {

        public Type()
        {
        }

        @NotNull
        @Override
        public Class<FluidStack> getContainedType()
        {
            return FluidStack.class;
        }

        @Override
        public @NotNull ICompoundContainer<FluidStack> create(@NotNull final FluidStack instance, final double count)
        {
            final FluidStack stack = instance.copy();
            stack.setAmount(1);
            return new FluidStackContainer(stack, count);
        }
        
        @Override
        public Codec<? extends ICompoundContainer<FluidStack>> codec() {
            return CODEC;
        }
        
        @Override
        public double getInnateCount(@NotNull FluidStack inputInstance) {
            return inputInstance.getAmount();
        }
    }

    private final FluidStack stack;
    private final double     count;

    private final int hashCode;

    public FluidStackContainer(final FluidStack stack, final double count)
    {
        this.stack = stack.copy();
        this.stack.setAmount(1);

        this.count = count;

        if (stack.isEmpty())
        {
            this.hashCode = 0;
            return;
        }

        this.hashCode = stack.writeToNBT(new CompoundTag()).hashCode();
    }

    @Override
    public boolean isValid()
    {
        return !stack.isEmpty();
    }

    @Override
    public FluidStack contents()
    {
        return stack;
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
        return "fluidstack_%s_%s".formatted(
                Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(contents().getFluid())).getNamespace(),
                Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(contents().getFluid())).getPath()
        );
    }
    
    @Override
    public ICompoundContainerType<FluidStack> type() {
        return ModContainerTypes.FLUIDSTACK.get();
    }
    
    @Override
    public int compareTo(@NotNull final ICompoundContainer<?> o)
    {
        //Dummies are after us. :D
        if (o instanceof Dummy)
            return -1;

        final Object contents = Objects.requireNonNull(o.contents());
        if (!(contents instanceof FluidStack otherStack))
        {
            return FluidStack.class.getName().compareTo(contents.getClass().getName());
        }
        
        return Comparators.FLUID_STACK_COMPARATOR.compare(stack, otherStack);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof FluidStackContainer that))
        {
            return false;
        }
        
        if (Double.compare(that.count, count) != 0)
        {
            return false;
        }
        return FluidStackUtils.compareFluidStacksIgnoreStackSize(stack, that.stack);
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    @Override
    public String toString()
    {
        return String.format("%s x FluidStack: %s", count, stack);
    }
}
