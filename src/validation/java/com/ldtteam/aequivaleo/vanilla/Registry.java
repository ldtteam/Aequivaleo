package com.ldtteam.aequivaleo.vanilla;

import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.vanilla.compound.ValidationCompoundType;
import com.ldtteam.aequivaleo.vanilla.compound.ValidationCompoundTypeGroup;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Registry {
    
    // GOO TYPES, THIS WHOLE CHONK
    private static final ResourceLocation AEQ_COMPOUND_TYPES_LOC = new ResourceLocation(Constants.MOD_ID, "compound_type");
    private static final ResourceLocation AEQ_COMPOUND_TYPE_GROUPS_LOC = new ResourceLocation(Constants.MOD_ID, "compound_type_group");
    public static final DeferredRegister<ICompoundType> TYPES = DeferredRegister.create(AEQ_COMPOUND_TYPES_LOC, Constants.MOD_ID);
    public static final DeferredRegister<ICompoundTypeGroup> TYPE_GROUPS = DeferredRegister.create(AEQ_COMPOUND_TYPE_GROUPS_LOC, Constants.MOD_ID);
    public static final DeferredHolder<ICompoundTypeGroup, ValidationCompoundTypeGroup> TYPE_GROUP = TYPE_GROUPS.register("validation", ValidationCompoundTypeGroup::new);
    public static final DeferredHolder<ICompoundType, ValidationCompoundType> VALIDATION = TYPES.register("validation", ValidationCompoundType::new);
    
    public static void init() {
        FMLModContainer modContainer = (FMLModContainer) ModList.get().getModContainerById(Constants.MOD_ID).get();
        IEventBus bus = modContainer.getEventBus();
        TYPES.register(bus);
        TYPE_GROUPS.register(bus);
    }
}
