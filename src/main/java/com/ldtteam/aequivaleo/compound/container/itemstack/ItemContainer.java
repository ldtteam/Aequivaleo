package com.ldtteam.aequivaleo.compound.container.itemstack;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.dummy.Dummy;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.ldtteam.aequivaleo.bootstrap.ModContainerTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ItemContainer implements ICompoundContainer<Item> {
    
    public static final Codec<ItemContainer> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ItemContainer::contents),
            Codec.DOUBLE.fieldOf("count").forGetter(ItemContainer::contentsCount)
    ).apply(instance, ItemContainer::new));
    
    public static final class Type implements ICompoundContainerType<Item> {
        
        public Type() {}
        
        @NotNull
        @Override
        public Class<Item> getContainedType() {
            return Item.class;
        }
        
        @Override
        public @NotNull ICompoundContainer<Item> create(@NotNull final Item instance, final double count) {
            return new ItemContainer(instance, count);
        }
        
        @Override
        public Codec<? extends ICompoundContainer<Item>> codec() {
            return CODEC;
        }
    }
    
    private final Item item;
    private final double count;
    private final int hashCode;
    
    public ItemContainer(final Item item, final double count) {
        this.item = item;
        this.count = count;
        this.hashCode = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)).hashCode();
    }
    
    @Override
    public boolean isValid() {
        return item != Items.AIR;
    }
    
    /**
     * The contents of this container.
     * Set to the 1 unit of the content type {@link Item}
     *
     * @return The contents.
     */
    @Override
    public Item contents() {
        return item;
    }
    
    /**
     * The amount of {@link Item}s contained in this wrapper.
     *
     * @return The amount.
     */
    @Override
    public Double contentsCount() {
        return count;
    }
    
    @Override
    public boolean canBeLoadedFromDisk() {
        return true;
    }
    
    @Override
    public String getContentAsFileName() {
        return "item_" + Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(contents()))
                                 .getNamespace() + "_" + Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(contents())).getPath();
    }
    
    @Override
    public ICompoundContainerType<Item> type() {
        return ModContainerTypes.ITEM.get();
    }
    
    @Override
    public int compareTo(@NotNull final ICompoundContainer<?> o) {
        //Dummies are after us. :D
        if (o instanceof Dummy)
            return -1;
        
        final Object contents = Validate.notNull(o.contents());
        if (!(contents instanceof final Item otherItem)) {
            return Item.class.getName().compareTo(contents.getClass().getName());
        }
        
        return Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(otherItem)).compareTo(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(contents())));
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final ItemContainer that)) {
            return false;
        }
        
        if (Double.compare(that.count, count) != 0) {
            return false;
        }
        return Objects.equals(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(contents())), Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(that.contents())));
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }
    
    @Override
    public String toString() {
        return String.format("%s x Item: %s", count, Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(contents())));
    }
}
