package com.ldtteam.aequivaleo.api.compound.information.datagen.data;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.util.AequivaleoExtraCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public record CompoundInstanceData(
        CompoundInstanceData.Mode mode,
        Set<ICompoundContainer<?>> containers,
        Set<CompoundInstanceRef> compoundInstances) {
    
    public enum Mode {
        DISABLED((iCompoundContainerSetMap, compoundInstanceData) -> {
            //Noop since this is disabled.
        }),
        ADDITIVE((iCompoundContainerSetMap, dataDrivenCompoundInstanceData) -> {
            dataDrivenCompoundInstanceData.containers().forEach(container -> {
                iCompoundContainerSetMap.computeIfAbsent(container
                        , (d) -> Sets.newLinkedHashSet()
                ).addAll(dataDrivenCompoundInstanceData
                                 .compoundInstances()
                                 .stream()
                                 .map(CompoundInstanceRef::get)
                                 .collect(Collectors.toCollection(LinkedHashSet::new)));
            });
        }),
        REPLACING((iCompoundContainerSetMap, dataDrivenCompoundInstanceData) -> {
            dataDrivenCompoundInstanceData.containers().forEach(container -> {
                iCompoundContainerSetMap.put(container,
                        dataDrivenCompoundInstanceData
                                .compoundInstances()
                                .stream()
                                .map(CompoundInstanceRef::get)
                                .collect(Collectors.toCollection(LinkedHashSet::new)));
            });
        });
        
        public static final Codec<Mode> CODEC = Codec.STRING.xmap(
                CompoundInstanceData.Mode::valueOf,
                CompoundInstanceData.Mode::name
        );
        
        private final BiConsumer<Map<ICompoundContainer<?>, Set<CompoundInstance>>, CompoundInstanceData> handler;
        
        Mode(final BiConsumer<Map<ICompoundContainer<?>, Set<CompoundInstance>>, CompoundInstanceData> handler) {
            this.handler = handler;
        }
        
        public void handleData(
                final Map<ICompoundContainer<?>, Set<CompoundInstance>> target,
                final CompoundInstanceData dataDrivenCompoundInstanceData
        ) {
            this.handler.accept(target, dataDrivenCompoundInstanceData);
        }
    }
    
    public static final Codec<CompoundInstanceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Mode.CODEC.fieldOf("mode").forGetter(CompoundInstanceData::mode),
            AequivaleoExtraCodecs.setOf(ICompoundContainer.CODEC).fieldOf("containers").forGetter(CompoundInstanceData::containers),
            AequivaleoExtraCodecs.setOf(CompoundInstanceRef.CODEC).fieldOf("compoundInstances").forGetter(CompoundInstanceData::compoundInstances)
    ).apply(instance, CompoundInstanceData::new));
    
    public static final CompoundInstanceData DISABLED = new CompoundInstanceData(
            Mode.DISABLED,
            Sets.newLinkedHashSet(),
            Sets.newLinkedHashSet()
    );
    
    public void handle(
            final Map<ICompoundContainer<?>, Set<CompoundInstance>> data
    ) {
        mode().handleData(data, this);
    }
}
