package com.ldtteam.aequivaleo.api.compound.information.datagen.data;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * Defines a reference to a compound instance.
 *
 * @param type The type of the compound instance.
 * @param amount The amount of the compound instance.
 */
public record CompoundInstanceRef(ResourceLocation type, Double amount)
{
    /**
     * The codec for the compound instance reference.
     */
    public static final Codec<CompoundInstanceRef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      ResourceLocation.CODEC.fieldOf("type").forGetter(CompoundInstanceRef::type),
      Codec.DOUBLE.fieldOf("amount").forGetter(CompoundInstanceRef::amount)
    ).apply(instance, CompoundInstanceRef::new));

    /**
     * Converts the reference to a compound instance.
     * Looking up the referenced type in the registry and creating a new instance with the amount.
     *
     * @return The compound instance.
     */
    public CompoundInstance get()
    {
        return ModRegistries.COMPOUND_TYPE
                .get(type())
          .map(type -> new CompoundInstance(type, amount()))
          .orElseThrow();
    }
}
