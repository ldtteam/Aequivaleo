package com.ldtteam.aequivaleo.mekanism.api.util;

import mekanism.api.MekanismAPI;
import net.minecraft.util.ResourceLocation;

public class Constants
{

    public static final ResourceLocation SIMPLE_ITEMSTACK_TO_ITEMSTACK             = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "stack_to_stack");
    public static final ResourceLocation CHEMICAL_INFUSING    = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "chemical_infusing");
    public static final ResourceLocation COMBINING            = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "combining");
    public static final ResourceLocation SEPARATING           = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "separating");
    public static final ResourceLocation WASHING              = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "washing");
    public static final ResourceLocation EVAPORATING       = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "evaporating");
    public static final ResourceLocation SIMPLE_GAS_TO_GAS = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "gas_to_gas");
    public static final ResourceLocation CRYSTALLIZING        = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "crystallizing");
    public static final ResourceLocation DISSOLUTION                       = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "dissolution");
    public static final ResourceLocation SIMPLE_ITEMSTACK_GAS_TO_ITEMSTACK = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "itemstack_gas_to_itemstack");
    public static final ResourceLocation NUCLEOSYNTHESIZING   = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "nucleosynthesizing");
    public static final ResourceLocation ENERGY_CONVERSION       = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "energy_conversion");
    public static final ResourceLocation SIMPLE_ITEMSTACK_TO_GAS = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "itemstack_to_gas");
    public static final ResourceLocation INFUSION_CONVERSION  = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "infusion_conversion");
    public static final ResourceLocation METALLURGIC_INFUSING = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "metallurgic_infusing");
    public static final ResourceLocation REACTION             = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "reaction");
    public static final ResourceLocation ROTARY               = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "rotary");
    public static final ResourceLocation SAWING               = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "sawing");
    
    private Constants()
    {
        throw new IllegalStateException("Can not instantiate an instance of: Constants. This is a utility class");
    }
}
