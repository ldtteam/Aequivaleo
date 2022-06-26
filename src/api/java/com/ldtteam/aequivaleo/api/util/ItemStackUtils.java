package com.ldtteam.aequivaleo.api.util;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ItemStackUtils
{

    /**
     * Private constructor to hide the implicit one.
     */
    private ItemStackUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Wrapper method to check if a stack is empty.
     * Used for easy updating to 1.11.
     *
     * @param stack The stack to check.
     * @return True when the stack is empty, false when not.
     */
    @NotNull
    public static Boolean isEmpty(@Nullable final ItemStack stack)
    {
        return stack == null || stack.isEmpty() || stack.getCount() <= 0;
    }

    /**
     * Method to compare to stacks, ignoring their stacksize.
     *
     * @param itemStack1 The left stack to compare.
     * @param itemStack2 The right stack to compare.
     * @return True when they are equal except the stacksize, false when not.
     */
    @NotNull
    public static Boolean compareItemStacksIgnoreStackSize(final ItemStack itemStack1, final ItemStack itemStack2)
    {
        return compareItemStacksIgnoreStackSize(itemStack1, itemStack2, true, true);
    }

    /**
     * Method to compare to stacks, ignoring their stacksize.
     *
     * @param itemStack1 The left stack to compare.
     * @param itemStack2 The right stack to compare.
     * @param matchMeta  Set to true to match meta data.
     * @param matchNBT   Set to true to match nbt
     * @return True when they are equal except the stacksize, false when not.
     */
    @SuppressWarnings("SameParameterValue")
    @NotNull
    private static Boolean compareItemStacksIgnoreStackSize(final ItemStack itemStack1, final ItemStack itemStack2, final boolean matchMeta, final boolean matchNBT)
    {
        if (!isEmpty(itemStack1) &&
              !isEmpty(itemStack2) &&
                Objects.equals(
                        ForgeRegistries.ITEMS.getKey(itemStack1.getItem()),
                        ForgeRegistries.ITEMS.getKey(itemStack2.getItem()))
                &&
              (itemStack1.getDamageValue() == itemStack2.getDamageValue() || !matchMeta))
        {
            // Then sort on NBT
            if (itemStack1.hasTag() && itemStack2.hasTag())
            {
                // Then sort on stack size
                return ItemStack.tagMatches(itemStack1, itemStack2) || !matchNBT;
            }
            else
            {
                return true;
            }
        }
        return false;
    }
}
