package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import net.minecraft.util.ResourceLocation;

public class CompoundInstanceRef
{

    private final ResourceLocation type;
    private final Double amount;

    public CompoundInstanceRef(final ResourceLocation type, final Double amount) {
        this.type = type;
        this.amount = amount;
    }

    public ResourceLocation getType()
    {
        return type;
    }

    public Double getAmount()
    {
        return amount;
    }

    public CompoundInstance get() {
        return new CompoundInstance(
          ModRegistries.COMPOUND_TYPE.getValue(getType()),
          getAmount()
        );
    }
}
