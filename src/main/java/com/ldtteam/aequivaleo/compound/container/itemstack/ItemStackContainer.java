package com.ldtteam.aequivaleo.compound.container.itemstack;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.dummy.Dummy;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.ldtteam.aequivaleo.api.util.Comparators;
import com.ldtteam.aequivaleo.api.util.ItemStackUtils;
import com.ldtteam.aequivaleo.bootstrap.ModContainerTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ItemStackContainer implements ICompoundContainer<ItemStack>
{

    public static final Codec<ItemStackContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      ItemStack.CODEC.fieldOf("stack").forGetter(ItemStackContainer::contents),
      Codec.DOUBLE.fieldOf("count").forGetter(ItemStackContainer::contentsCount)
    ).apply(instance, ItemStackContainer::new));
    
    public static final class Type implements ICompoundContainerType<ItemStack>
    {

        public Type()
        {
        }

        @NotNull
        @Override
        public Class<ItemStack> getContainedType()
        {
            return ItemStack.class;
        }

        @Override
        public @NotNull ICompoundContainer<ItemStack> create(@NotNull final ItemStack instance, final double count)
        {
            final ItemStack stack = instance.copyWithCount(1);
            return new ItemStackContainer(stack, count);
        }

        @Override
        public double getInnateCount(@NotNull ItemStack inputInstance) {
            return inputInstance.getCount();
        }
        
        @Override
        public Codec<? extends ICompoundContainer<ItemStack>> codec() {
            return CODEC;
        }
    }

    private final ItemStack stack;
    private final double count;

    private final int hashCode;

    public ItemStackContainer(final ItemStack stack, final double count) {
        this.stack = stack.copy();
        this.stack.setCount(1);

        this.count = count;

        if (stack.isEmpty())
        {
            this.hashCode = 0;
            return;
        }

        this.hashCode = stack.save(new CompoundTag()).hashCode();
    }

    @Override
    public boolean isValid()
    {
        return !stack.isEmpty();
    }

    /**
     * The contents of this container.
     * Set to the 1 unit of the content type {@link ItemStack}
     *
     * @return The contents.
     */
    @Override
    public ItemStack contents()
    {
        return stack;
    }

    /**
     * The amount of {@link ItemStack}s contained in this wrapper.
     *
     * @return The amount.
     */
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

        return "itemstack_%s_%s".formatted(
                Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(contents().getItem())).getNamespace(),
                Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(contents().getItem())).getPath()
        );
    }
    
    @Override
    public ICompoundContainerType<ItemStack> type() {
        return ModContainerTypes.ITEMSTACK.get();
    }
    
    @Override
    public int compareTo(@NotNull final ICompoundContainer<?> o)
    {
        //Dummies are after us. :D
        if (o instanceof Dummy)
            return -1;

        final Object contents = Objects.requireNonNull(o.contents());
        if (!(contents instanceof ItemStack otherStack))
        {
            return ItemStack.class.getName().compareTo(contents.getClass().getName());
        }
        
        return Comparators.ITEM_STACK_COMPARATOR.compare(stack, otherStack);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ItemStackContainer that))
        {
            return false;
        }
        
        if (Double.compare(that.count, count) != 0)
        {
            return false;
        }
        return ItemStackUtils.compareItemStacksIgnoreStackSize(stack, that.stack);
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    @Override
    public String toString()
    {
        return String.format("%s x ItemStack: %s", count, stack);
    }
}
