package com.ldtteam.aequivaleo.api.util;

import net.minecraft.core.Direction;

public final class DataGeneratorUtils
{

    private DataGeneratorUtils()
    {
        throw new IllegalStateException("Tried to initialize: DataGeneratorUtils but this is a Utility class.");
    }

    /**
     * Calculates the rotation on the x axis for a given model from its facing direction.
     *
     * @param direction The direction to get the rotation for. The normal direction is NORTH.
     * @return The rotation on the X axis for the model in the given direction in degrees.
     */
    public static int getXRotationFromFacing(final Direction direction)
    {
        switch (direction)
        {
            case NORTH:
            case SOUTH:
                return 0;
            case UP:
            case DOWN:
            case EAST:
                return 90;
            case WEST:
                return 270;
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
    }

    /**
     * Calculates the rotation on the y axis for a given model from its facing direction.
     *
     * @param direction The direction to get the rotation for. The normal direction is NORTH.
     * @return The rotation on the y axis for the model in the given direction in degrees.
     */
    public static int getYRotationFromFacing(final Direction direction)
    {
        switch (direction)
        {
            case UP:
            case NORTH:
                return 0;
            case DOWN:
            case SOUTH:
                return 180;
            case WEST:
                return 270;
            case EAST:
                return 90;
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
    }
}
