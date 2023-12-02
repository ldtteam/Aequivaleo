package com.ldtteam.aequivaleo.vanilla.compound;

import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.vanilla.Registry;
import com.mojang.serialization.Codec;

public class ValidationCompoundType implements ICompoundType {
    
    public ValidationCompoundType() {
    }
    
    @Override
    public ICompoundTypeGroup getGroup() {
        return Registry.TYPE_GROUP.get();
    }
}
