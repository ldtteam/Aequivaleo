package com.ldtteam.aequivaleo.compound.container.itemstack;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.dummy.Dummy;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.RegistryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Objects;

public class ItemContainer implements ICompoundContainer<Item>
{

    public static final class Factory implements ICompoundContainerFactory<Item>
    {

        public Factory()
        {

        }

        @NotNull
        @Override
        public Class<Item> getContainedType()
        {
            return Item.class;
        }

        @Override
        public @NotNull ICompoundContainer<Item> create(@NotNull final Item instance, final double count)
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
            object.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(src.getContents())).toString());
            return object;
        }

        @Override
        public void write(final ICompoundContainer<Item> object, final FriendlyByteBuf buffer)
        {
            buffer.writeVarInt(RegistryUtils.getFull(ForgeRegistries.ITEMS.getRegistryKey()).getID(object.getContents()));
            buffer.writeDouble(object.getContentsCount());
        }

        @Override
        public ICompoundContainer<Item> read(final FriendlyByteBuf buffer)
        {
            return new ItemContainer(
              RegistryUtils.getFull(ForgeRegistries.ITEMS.getRegistryKey()).getValue(buffer.readVarInt()),
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
        this.hashCode = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).hashCode();
    }

    @Override
    public boolean isValid()
    {
        return item != Items.AIR;
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
    public boolean canBeLoadedFromDisk()
    {
        return true;
    }

    @Override
    public String getContentAsFileName()
    {
        return "item_" + Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(getContents()))
                            .getNamespace() + "_" + Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(getContents())).getPath();
    }

    @Override
    public int compareTo(@NotNull final ICompoundContainer<?> o)
    {
        //Dummies are after us. :D
        if (o instanceof Dummy)
            return -1;

        final Object contents = Validate.notNull(o.getContents());
        if (!(contents instanceof final Item otherItem))
        {
            return Item.class.getName().compareTo(contents.getClass().getName());
        }

        return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(otherItem)).compareTo(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(getContents())));
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof final ItemContainer that))
        {
            return false;
        }

        if (Double.compare(that.count, count) != 0)
        {
            return false;
        }
        return Objects.equals(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(getContents())), Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(that.getContents())));
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    @Override
    public String toString()
    {
        return String.format("%s x Item: %s", count, Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(getContents())));
    }
}
