package com.ldtteam.aequivaleo.api.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;
import org.jetbrains.annotations.NotNull;

public final class BlockUtils
{

    private BlockUtils()
    {
        throw new IllegalStateException("Tried to initialize: BlockUtils but this is a Utility class.");
    }

    public static ItemStack getHarvestingToolForBlock(@NotNull final Block block)
    {
        final BlockState defaultState = block.getDefaultState();
        final ToolType toolType = defaultState.getHarvestTool();
        if (toolType == null)
            return ItemStack.EMPTY;

        return ItemStackUtils.getBestTool(toolType);
    }
}
