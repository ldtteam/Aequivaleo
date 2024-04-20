package com.ldtteam.aequivaleo.api.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

/**
 * A block getter that only returns a single block.
 */
public class SingleBlockBlockReader implements BlockGetter
{
    private final BlockPos   pos;
    private final BlockState blockState;
    private final BlockEntity entity;
    private final FluidState fluidState;

    /**
     * Creates a new single block {@link BlockGetter}.
     *
     * @param blockState The block state.
     */
    public SingleBlockBlockReader(final BlockState blockState)
    {
        this(BlockPos.ZERO, blockState, null, Fluids.EMPTY.defaultFluidState());
    }

    /**
     * Creates a new single block {@link BlockGetter}.
     *
     * @param blockState The block state.
     * @param entity     The block entity.
     */
    public SingleBlockBlockReader(final BlockState blockState, final BlockEntity entity)
    {
        this(BlockPos.ZERO, blockState, entity, Fluids.EMPTY.defaultFluidState());
    }

    /**
     * Creates a new single block {@link BlockGetter}.
     *
     * @param pos        The position.
     * @param blockState The block state.
     * @param entity     The block entity.
     * @param fluidState The fluid state.
     */
    public SingleBlockBlockReader(final BlockPos pos, final BlockState blockState, final BlockEntity entity, final FluidState fluidState)
    {
        this.pos = pos;
        this.blockState = blockState;
        this.entity = entity;
        this.fluidState = fluidState;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(final BlockPos pos)
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

        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(final BlockPos pos)
    {
        if (pos == this.pos)
            return fluidState;

        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public int getHeight()
    {
        return pos.getY();
    }

    @Override
    public int getMinBuildHeight()
    {
        return getHeight();
    }
}
