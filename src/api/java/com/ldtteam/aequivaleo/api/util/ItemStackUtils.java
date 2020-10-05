package com.ldtteam.aequivaleo.api.util;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ItemStackUtils
{

    private static final Map<ToolType, Tuple<ItemStack, Integer>> toolTypeBestToolMap = Maps.newConcurrentMap();

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
    @NotNull
    private static Boolean compareItemStacksIgnoreStackSize(final ItemStack itemStack1, final ItemStack itemStack2, final boolean matchMeta, final boolean matchNBT)
    {
        if (!isEmpty(itemStack1) &&
              !isEmpty(itemStack2) &&
              itemStack1.getItem().getRegistryName() == itemStack2.getItem().getRegistryName() &&
              (itemStack1.getDamage() == itemStack2.getDamage() || !matchMeta))
        {
            // Then sort on NBT
            if (itemStack1.hasTag() && itemStack2.hasTag())
            {
                // Then sort on stack size
                return ItemStack.areItemStackTagsEqual(itemStack1, itemStack2) || !matchNBT;
            }
            else
            {
                return true;
            }
        }
        return false;
    }

    public static synchronized ItemStack getBestTool(@NotNull final ToolType toolType)
    {
        if (toolTypeBestToolMap.isEmpty())
        {
            ForgeRegistries.ITEMS.getValues().parallelStream().forEach(item -> {
                item.getToolTypes(new ItemStack(item)).forEach(toolTypeOnItem -> {
                    final Integer toolLevel = item.getHarvestLevel(new ItemStack(item), toolTypeOnItem, null, null);

                    synchronized (toolTypeBestToolMap)
                    {
                        if (toolTypeBestToolMap.containsKey(toolTypeOnItem) && toolTypeBestToolMap.get(toolTypeOnItem).getB() < toolLevel)
                        {
                            toolTypeBestToolMap.remove(toolTypeOnItem);
                        }

                        toolTypeBestToolMap.put(toolTypeOnItem, new Tuple<>(new ItemStack(item), toolLevel));
                    }
                });
            });
        }

        return toolTypeBestToolMap.getOrDefault(toolType, new Tuple<>(ItemStack.EMPTY, 0)).getA();
    }
}
