package com.ldtteam.aequivaleo.compound.container.itemstack;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.dummy.Dummy;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.RegistryUtils;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Objects;

public class ItemContainer implements ICompoundContainer<Item>
{

    public static final class Factory extends ForgeRegistryEntry<ICompoundContainerFactory<?>> implements ICompoundContainerFactory<Item>
    {

        public Factory()
        {
            setRegistryName(Constants.MOD_ID, "item");
        }

        @NotNull
        @Override
        public Class<Item> getContainedType()
        {
            return Item.class;
        }

        @Override
        public ICompoundContainer<Item> create(@NotNull final Item instance, final double count)
        {
            return new ItemContainer(instance, count);
        }

        @Override
        public ICompoundContainer<Item> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
        {
            return new ItemContainer(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(json.getAsJsonObject().get("item").getAsString()))), json.getAsJsonObject().get("count").getAsDouble());
        }

        @Override
        public JsonElement serialize(final ICompoundContainer<Item> src, final Type typeOfSrc, final JsonSerializationContext context)
        {
            final JsonObject object = new JsonObject();
            object.addProperty("count", src.getContentsCount());
            object.addProperty("item", Objects.requireNonNull(src.getContents().getRegistryName()).toString());
            return object;
        }

        @Override
        public void write(final ICompoundContainer<Item> object, final PacketBuffer buffer)
        {
            buffer.writeVarInt(RegistryUtils.getFull(Item.class).getID(object.getContents()));
            buffer.writeDouble(object.getContentsCount());
        }

        @Override
        public ICompoundContainer<Item> read(final PacketBuffer buffer)
        {
            return new ItemContainer(
              RegistryUtils.getFull(Item.class).getValue(buffer.readVarInt()),
              buffer.readDouble()
            );
        }
    }

    private final Item   item;
    private final double count;
    private final int hashCode;

    public ItemContainer(final Item item, final double count) {
        this.item = item;
        this.count = count;
        this.hashCode = Objects.requireNonNull(item.getRegistryName()).hashCode();
    }

    /**
     * The contents of this container.
     * Set to the 1 unit of the content type {@link Item}
     *
     * @return The contents.
     */
    @Override
    public Item getContents()
    {
        return item;
    }

    /**
     * The amount of {@link Item}s contained in this wrapper.
     *
     * @return The amount.
     */
    @Override
    public Double getContentsCount()
    {
        return count;
    }

    @Override
    public int compareTo(@NotNull final ICompoundContainer<?> o)
    {
        //Dummies are after us. :D
        if (o instanceof Dummy)
            return -1;

        final Object contents = Validate.notNull(o.getContents());
        if (!(contents instanceof Item))
        {
            return Item.class.getName().compareTo(contents.getClass().getName());
        }

        final Item otherItem = (Item) contents;
        if (item.getItem().getTags().stream().anyMatch(r -> otherItem.getItem().getTags().contains(r)))
            return 0;

        return Objects.requireNonNull(otherItem.getRegistryName()).compareTo(item.getRegistryName());
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ItemContainer))
        {
            return false;
        }

        final ItemContainer that = (ItemContainer) o;

        if (Double.compare(that.count, count) != 0)
        {
            return false;
        }
        return Objects.equals(item.getRegistryName(), that.getContents().getRegistryName());
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    @Override
    public String toString()
    {
        return "ItemWrapper{" +
                 "stack=" + item +
                 ", count=" + count +
                 '}';
    }
}
