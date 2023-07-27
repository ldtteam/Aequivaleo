package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.heat.Heat;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

public final class ModContainerFactoryTypes {
    private ModContainerFactoryTypes() {
        throw new IllegalStateException("Tried to initialize: ModContainerFactoryTypes but this is a Utility class.");
    }

    public static RegistryObject<ICompoundContainerFactory<Item>> ITEM;
    public static RegistryObject<ICompoundContainerFactory<ItemStack>> ITEMSTACK;
    public static RegistryObject<ICompoundContainerFactory<Fluid>> FLUID;
    public static RegistryObject<ICompoundContainerFactory<FluidStack>> FLUIDSTACK;
    public static RegistryObject<ICompoundContainerFactory<Heat>> HEAT;
    public static RegistryObject<ICompoundContainerFactory<ICompoundType>> COMPOUND_TYPE;
    public static RegistryObject<ICompoundContainerFactory<TagKey<?>>> TAG;
}
