package com.ldtteam.aequivaleo.api.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Comparator;
import java.util.Objects;

public final class Comparators
{

    private Comparators()
    {
        throw new IllegalStateException("Tried to initialize: Comparators but this is a Utility class.");
    }

    public static final Comparator<ItemStack> ITEM_STACK_COMPARATOR = (itemStack1, itemStack2) -> {
        if (itemStack1 != null && itemStack2 != null)
        {
            if (!ItemStackUtils.isEmpty(itemStack1) && !ItemStackUtils.isEmpty(itemStack2))
            {
                // Sort on id
                if (Item.getId(itemStack1.getItem()) - Item.getId(itemStack2.getItem()) == 0)
                {
                    // Sort on item
                    if (itemStack1.getItem() == itemStack2.getItem())
                    {
                        // Then sort on meta
                        if ((itemStack1.getDamageValue() == itemStack2.getDamageValue()))
                        {
                            // Then sort on NBT
                            if (itemStack1.hasTag() && itemStack2.hasTag())
                            {
                                // Then sort on stack size
                                if (ItemStack.isSameItemSameTags(itemStack1, itemStack2))
                                {
                                    return (itemStack1.getCount() - itemStack2.getCount());
                                }
                                else if (itemStack1.getTag() != null && itemStack2.getTag() == null)
                                {
                                    return -1;
                                }
                                else if (itemStack1.getTag() == null && itemStack2.getTag() != null)
                                {
                                    return 1;
                                }
                                else if (itemStack1.getTag() == null)
                                {
                                    return 0;
                                }
                                else
                                {
                                    return itemStack1.getTag().toString().compareTo(itemStack2.getTag().toString());
                                }
                            }
                            else if (!(itemStack1.hasTag()) && itemStack2.hasTag())
                            {
                                return -1;
                            }
                            else if (itemStack1.hasTag() && !(itemStack2.hasTag()))
                            {
                                return 1;
                            }
                            else
                            {
                                return (itemStack1.getCount() - itemStack2.getCount());
                            }
                        }
                        else
                        {
                            return (itemStack1.getDamageValue() - itemStack2.getDamageValue());
                        }
                    }
                    else
                    {
                        return itemStack1.getItem().getDescriptionId(itemStack1).compareToIgnoreCase(itemStack2.getItem().getDescriptionId(itemStack2));
                    }
                }
                else
                {
                    return Item.getId(itemStack1.getItem()) - Item.getId(itemStack2.getItem());
                }
            }
            else
            {
                return 0;
            }
        }
        else if (itemStack1 != null)
        {
            return -1;
        }
        else if (itemStack2 != null)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    };

    public static final Comparator<FluidStack> FLUID_STACK_COMPARATOR = (fluidStack1, fluidStack2) -> {

        if (fluidStack1 != null && fluidStack2 != null)
        {
            // Sort on id
            if (BuiltInRegistries.FLUID.getId(fluidStack1.getFluid()) - BuiltInRegistries.FLUID.getId(fluidStack2.getFluid()) == 0)
            {
                // Sort on fluid
                if (fluidStack1.getFluid() == fluidStack2.getFluid())
                {
                    // Then sort on meta
                    // Then sort on NBT
                    if (fluidStack1.hasTag() && fluidStack2.hasTag())
                    {
                        // Then sort on stack size
                        if (FluidStack.areFluidStackTagsEqual(fluidStack1, fluidStack2))
                        {
                            return (fluidStack1.getAmount() - fluidStack2.getAmount());
                        }
                        else
                        {
                            return fluidStack1.getTag().toString().compareTo(fluidStack2.getTag().toString());
                        }
                    }
                    else if (!(fluidStack1.hasTag()) && fluidStack2.hasTag())
                    {
                        return -1;
                    }
                    else if (fluidStack1.hasTag() && !(fluidStack2.hasTag()))
                    {
                        return 1;
                    }
                    else
                    {
                        return (fluidStack1.getAmount() - fluidStack2.getAmount());
                    }
                }
                else
                {
                    return Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(fluidStack1.getFluid())).compareTo(Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(fluidStack2.getFluid())));
                }
            }
            else
            {
                return (BuiltInRegistries.FLUID).getId(fluidStack1.getFluid())
                         - (BuiltInRegistries.FLUID).getId(fluidStack2.getFluid());
            }
        }
        else if (fluidStack1 != null)
        {
            return -1;
        }
        else if (fluidStack2 != null)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    };
}
