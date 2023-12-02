package com.ldtteam.aequivaleo.api.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FluidStackUtils
{

    private FluidStackUtils()
    {
        throw new IllegalStateException("Tried to initialize: FluidStackUtilsUtils but this is a Utility class.");
    }


    /**
     * Method to compare to stacks, ignoring their stacksize.
     *
     * @param fluidStack1 The left stack to compare.
     * @param fluidStack2 The right stack to compare.
     * @return True when they are equal except the stacksize, false when not.
     */
    @NotNull
    public static Boolean compareFluidStacksIgnoreStackSize(final FluidStack fluidStack1, final FluidStack fluidStack2)
    {
        return compareFluidStacksIgnoreStackSize(fluidStack1, fluidStack2, true);
    }

    /**
     * Method to compare to stacks, ignoring their stacksize.
     *
     * @param fluidStack1 The left stack to compare.
     * @param fluidStack2 The right stack to compare.
     * @param matchNBT   Set to true to match nbt
     * @return True when they are equal except the stacksize, false when not.
     */
    @SuppressWarnings("SameParameterValue")
    @NotNull
    private static Boolean compareFluidStacksIgnoreStackSize(final FluidStack fluidStack1, final FluidStack fluidStack2, final boolean matchNBT)
    {
        if (Objects.equals(
                BuiltInRegistries.FLUID.getKey(fluidStack1.getFluid()),
                BuiltInRegistries.FLUID.getKey(fluidStack2.getFluid())))
        {
            // Then sort on NBT
            if (fluidStack1.hasTag() && fluidStack2.hasTag())
            {
                // Then sort on stack size
                return FluidStack.areFluidStackTagsEqual(fluidStack1, fluidStack2) || !matchNBT;
            }
            else
            {
                return true;
            }
        }
        return false;
    }
}
