package com.ldtteam.aequivaleo.vanilla.compound;

import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class ValidationCompoundType implements ICompoundType {

    public static final ResourceLocation NAME = new ResourceLocation(Constants.MOD_ID, "validation");
    
    private final Supplier<ICompoundTypeGroup> group;
    
    public ValidationCompoundType(Supplier<ICompoundTypeGroup> group) {
        this.group = group;
    }
    
    @Override
    public ICompoundTypeGroup getGroup() {
        return group.get();
    }
    
    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }
}
