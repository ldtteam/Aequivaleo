package com.ldtteam.aequivaleo.vanilla;

import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.vanilla.compound.ValidationCompoundType;
import com.ldtteam.aequivaleo.vanilla.compound.ValidationCompoundTypeGroup;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class Registry {
    
    // GOO TYPES, THIS WHOLE CHONK
    private static final ResourceLocation AEQ_COMPOUND_TYPES_LOC = new ResourceLocation(Constants.MOD_ID, "compound_type");
    private static final ResourceLocation AEQ_COMPOUND_TYPE_GROUPS_LOC = new ResourceLocation(Constants.MOD_ID, "compound_type_group");
    public static final DeferredRegister<ICompoundType> TYPES = DeferredRegister.create(AEQ_COMPOUND_TYPES_LOC, Constants.MOD_ID);
    public static final DeferredRegister<ICompoundTypeGroup> TYPE_GROUPS = DeferredRegister.create(AEQ_COMPOUND_TYPE_GROUPS_LOC, Constants.MOD_ID);
    public static final RegistryObject<ICompoundTypeGroup> TYPE_GROUP = TYPE_GROUPS.register("validation", ValidationCompoundTypeGroup::new);
    public static final RegistryObject<ICompoundType> VALIDATION = TYPES.register("validation", () -> new ValidationCompoundType(TYPE_GROUP));
    
    public static void init() {
        FMLModContainer modContainer = (FMLModContainer) ModList.get().getModContainerById(Constants.MOD_ID).get();
        IEventBus bus = modContainer.getEventBus();
        TYPES.register(bus);
        TYPE_GROUPS.register(bus);
    }
}
