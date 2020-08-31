package com.ldtteam.aequivaleo.api.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.Nullable;

public class SingleBlockBlockReader implements IBlockReader
{
    private final BlockPos   pos;
    private final BlockState blockState;
    private final TileEntity entity;
    private final FluidState fluidState;

    public SingleBlockBlockReader(final BlockState blockState)
    {
        this(BlockPos.ZERO, blockState, null, Fluids.EMPTY.getDefaultState());
    }

    public SingleBlockBlockReader(final BlockState blockState, final TileEntity entity)
    {
        this(BlockPos.ZERO, blockState, entity, Fluids.EMPTY.getDefaultState());
    }

    public SingleBlockBlockReader(final BlockPos pos, final BlockState blockState, final TileEntity entity, final FluidState fluidState)
    {
        this.pos = pos;
        this.blockState = blockState;
        this.entity = entity;
        this.fluidState = fluidState;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(final BlockPos pos)
    {
        if (pos == this.pos)
            return this.entity;

        return null;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        if (pos == this.pos)
            return blockState;

        return Blocks.AIR.getDefaultState();
    }

    @Override
    public FluidState getFluidState(final BlockPos pos)
    {
        if (pos == this.pos)
            return fluidState;

        return Fluids.EMPTY.getDefaultState();
    }
}
