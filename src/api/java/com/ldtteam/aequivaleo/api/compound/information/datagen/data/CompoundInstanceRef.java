package com.ldtteam.aequivaleo.api.compound.information.datagen.data;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record CompoundInstanceRef(ResourceLocation type, Double amount)
{
    public static final Codec<CompoundInstanceRef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      ResourceLocation.CODEC.fieldOf("type").forGetter(CompoundInstanceRef::type),
      Codec.DOUBLE.fieldOf("amount").forGetter(CompoundInstanceRef::amount)
    ).apply(instance, CompoundInstanceRef::new));
    
    public CompoundInstance get()
    {
        return ModRegistries.COMPOUND_TYPE
                .get(type())
          .map(type -> new CompoundInstance(type, amount()))
          .orElseThrow();
    }
}
