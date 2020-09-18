package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.heat.Heat;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

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
    public static ICompoundContainerFactory<Heat> HEAT;
}
