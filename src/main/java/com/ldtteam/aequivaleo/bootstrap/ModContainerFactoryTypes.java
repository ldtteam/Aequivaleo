package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.heat.Heat;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraftforge.fluids.FluidStack;

public final class ModContainerFactoryTypes
{
    private ModContainerFactoryTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModContainerFactoryTypes but this is a Utility class.");
    }

    public static ICompoundContainerFactory<Item> ITEM;
    public static ICompoundContainerFactory<ItemStack> ITEMSTACK;
    public static ICompoundContainerFactory<Fluid> FLUID;
    public static ICompoundContainerFactory<FluidStack> FLUIDSTACK;
    public static ICompoundContainerFactory<Heat>          HEAT;
    public static ICompoundContainerFactory<ICompoundType> COMPOUND_TYPE;
    public static ICompoundContainerFactory<Tag.Named> TAG;
}
