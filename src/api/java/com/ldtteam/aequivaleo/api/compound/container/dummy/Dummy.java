package com.ldtteam.aequivaleo.api.compound.container.dummy;

import com.google.gson.JsonElement;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents an unknown type.
 * Will contain the original JsonData in case of recovery.
 */
public record Dummy(@NotNull JsonElement originalData) implements ICompoundContainer<Dummy> {
    
    public static final Codec<Dummy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.JSON.fieldOf("originalData").forGetter(Dummy::originalData)
    ).apply(instance, Dummy::new));
    
    public Dummy(@NotNull final JsonElement originalData) {
        this.originalData = Objects.requireNonNull(originalData);
    }
    
    @Override
    public boolean isValid() {
        return false;
    }
    
    @Override
    public Dummy contents() {
        return this;
    }
    
    @Override
    public Double contentsCount() {
        return 0d;
    }
    
    @Override
    public String getContentAsFileName() {
        throw new IllegalStateException("Tried to access the file name for the container. Container does not support.");
    }
    
    @Override
    public ICompoundContainerType<Dummy> type() {
        return null;
    }
    
    @Override
    public int compareTo(@NotNull final ICompoundContainer<?> o) {
        if (!(o instanceof Dummy d)) {
            //If it is not a dummy then we say we are greater. Dummies end up last in the list.
            return 1;
        }
        
        //Now we can compare the data stored inside.
        return originalData.toString().compareTo(d.originalData.toString());
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Dummy dummy)) {
            return false;
        }
        
        return originalData.toString().equals(dummy.originalData.toString());
    }
    
    @Override
    public int hashCode() {
        return originalData.toString().hashCode();
    }
    
    public static final class Type implements ICompoundContainerType<Dummy> {
        
        @Override
        public @NotNull Class<Dummy> getContainedType() {
            return Dummy.class;
        }
        
        @Override
        public @NotNull ICompoundContainer<Dummy> create(@NotNull Dummy inputInstance, double count) {
            return new Dummy(inputInstance.originalData());
        }
        
        @Override
        public Codec<ICompoundContainer<Dummy>> codec() {
            return null;
        }
    }
}
