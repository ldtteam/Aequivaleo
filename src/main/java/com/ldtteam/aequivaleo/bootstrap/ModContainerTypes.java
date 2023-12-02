package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.ldtteam.aequivaleo.compound.container.compoundtype.CompoundTypeContainer;
import com.ldtteam.aequivaleo.compound.container.fluid.FluidContainer;
import com.ldtteam.aequivaleo.compound.container.fluid.FluidStackContainer;
import com.ldtteam.aequivaleo.compound.container.heat.HeatContainer;
import com.ldtteam.aequivaleo.compound.container.itemstack.ItemContainer;
import com.ldtteam.aequivaleo.compound.container.itemstack.ItemStackContainer;
import com.ldtteam.aequivaleo.compound.container.tag.TagContainer;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModContainerTypes {
    private ModContainerTypes() {
        throw new IllegalStateException("Tried to initialize: ModContainerFactoryTypes but this is a Utility class.");
    }

    public static DeferredHolder<ICompoundContainerType<?>, ItemContainer.Type> ITEM;
    public static DeferredHolder<ICompoundContainerType<?>, ItemStackContainer.Type> ITEMSTACK;
    public static DeferredHolder<ICompoundContainerType<?>, FluidContainer.Type> FLUID;
    public static DeferredHolder<ICompoundContainerType<?>, FluidStackContainer.Type> FLUIDSTACK;
    public static DeferredHolder<ICompoundContainerType<?>, HeatContainer.Type> HEAT;
    public static DeferredHolder<ICompoundContainerType<?>, CompoundTypeContainer.Type> COMPOUND_TYPE;
    public static DeferredHolder<ICompoundContainerType<?>, TagContainer.Type> TAG;
}
