package com.ldtteam.aequivaleo.api.compound.information.datagen.data;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import net.minecraft.resources.ResourceLocation;

public record CompoundInstanceRef(ResourceLocation type, Double amount)
{
    public CompoundInstance get()
    {
        return ModRegistries.COMPOUND_TYPE
          .get(type())
          .map(type -> new CompoundInstance(type, amount()))
          .orElseThrow();
    }
}
