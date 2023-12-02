package com.ldtteam.aequivaleo.api.util;

import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ModRegistryKeys {
    
    private ModRegistryKeys() {
        throw new IllegalStateException("Tried to create utility class!");
    }
    
    public static final ResourceKey<Registry<ICompoundTypeGroup>> COMPOUND_TYPE_GROUP =
            ResourceKey.createRegistryKey(new ResourceLocation(Constants.MOD_ID, "compound_type_group"));
}
