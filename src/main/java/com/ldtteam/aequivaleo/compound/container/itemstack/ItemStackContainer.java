package com.ldtteam.aequivaleo.compound.container.itemstack;

import com.google.gson.*;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.dummy.Dummy;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.AequivaleoLogger;
import com.ldtteam.aequivaleo.api.util.Comparators;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.ItemStackUtils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Collection;

public class ItemStackContainer implements ICompoundContainer<ItemStack>
{

    public static final class Factory extends ForgeRegistryEntry<ICompoundContainerFactory<?>> implements ICompoundContainerFactory<ItemStack>
    {

        public Factory()
        {
            setRegistryName(Constants.MOD_ID, "itemstack");
        }

        @NotNull
        @Override
        public Class<ItemStack> getContainedType()
        {
            return ItemStack.class;
        }

        @Override
        public ICompoundContainer<ItemStack> create(@NotNull final ItemStack instance, @NotNull final double count)
        {
            final ItemStack stack = instance.copy();
            stack.setCount(1);
            return new ItemStackContainer(stack, count);
        }

        @Override
        public ICompoundContainer<ItemStack> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
        {
            try
            {
                return new ItemStackContainer(ItemStack.read(JsonToNBT.getTagFromJson(json.getAsJsonObject().get("stack").getAsString())), json.getAsJsonObject().get("count").getAsDouble());
            }
            catch (CommandSyntaxException e)
            {
                AequivaleoLogger.getLogger().error(e);
            }

            return null;
        }

        @Override
        public JsonElement serialize(final ICompoundContainer<ItemStack> src, final Type typeOfSrc, final JsonSerializationContext context)
        {
            final JsonObject object = new JsonObject();
            object.addProperty("count", src.getContentsCount());
            object.addProperty("stack", src.getContents().write(new CompoundNBT()).toString());
            return object;
        }

        @Override
        public void write(final ICompoundContainer<ItemStack> object, final PacketBuffer buffer)
        {
            buffer.writeItemStack(object.getContents());
            buffer.writeDouble(object.getContentsCount());
        }

        @Override
        public ICompoundContainer<ItemStack> read(final PacketBuffer buffer)
        {
            return new ItemStackContainer(
              buffer.readItemStack(),
              buffer.readDouble()
            );
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

        this.hashCode = stack.write(new CompoundNBT()).hashCode();
    }

    /**
     * The contents of this container.
     * Set to the 1 unit of the content type {@link ItemStack}
     *
     * @return The contents.
     */
    @Override
    public ItemStack getContents()
    {
        return stack;
    }

    /**
     * The amount of {@link ItemStack}s contained in this wrapper.
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
        if (!(contents instanceof ItemStack))
        {
            return ItemStack.class.getName().compareTo(contents.getClass().getName());
        }

        final ItemStack otherStack = (ItemStack) contents;
        return Comparators.ID_COMPARATOR.compare(stack, otherStack);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ItemStackContainer))
        {
            return false;
        }

        final ItemStackContainer that = (ItemStackContainer) o;

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
        return "ItemStackWrapper{" +
                 "stack=" + stack +
                 ", count=" + count +
                 '}';
    }
}
