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

/**
 * Defines the data structure that is used to load compound instance data specifications from disk.
 *
 * @param mode The mode of the data application. Used when multiple compound instance data specifications are loaded for the same container.
 * @param containers The containers that this data applies to.
 * @param compoundInstances The compound instances that are applied to the containers.
 */
public record CompoundInstanceData(
        CompoundInstanceData.Mode mode,
        Set<ICompoundContainer<?>> containers,
        Set<CompoundInstanceRef> compoundInstances) {

    /**
     * Defines the mode of the data application.
     * Used when multiple compound instance data specifications are loaded for the same container.
     */
    public enum Mode {
        /**
         * The data is disabled.
         * No data will be applied.
         */
        DISABLED((iCompoundContainerSetMap, compoundInstanceData) -> {
            //Noop since this is disabled.
        }),

        /**
         * The data is additive.
         * The compound instances will be added to the existing compound instances for the container.
         */
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

        /**
         * The data is replacing.
         * The compound instances will replace the existing compound instances for the container.
         */
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

        /**
         * The codec for the mode.
         */
        public static final Codec<Mode> CODEC = Codec.STRING.xmap(
                CompoundInstanceData.Mode::valueOf,
                CompoundInstanceData.Mode::name
        );
        
        private final BiConsumer<Map<ICompoundContainer<?>, Set<CompoundInstance>>, CompoundInstanceData> handler;
        
        Mode(final BiConsumer<Map<ICompoundContainer<?>, Set<CompoundInstance>>, CompoundInstanceData> handler) {
            this.handler = handler;
        }

        /**
         * Handles the data manipulation for the given target.
         *
         * @param target The target to manipulate.
         * @param dataDrivenCompoundInstanceData The data to drive the manipulation.
         */
        public void handleData(
                final Map<ICompoundContainer<?>, Set<CompoundInstance>> target,
                final CompoundInstanceData dataDrivenCompoundInstanceData
        ) {
            this.handler.accept(target, dataDrivenCompoundInstanceData);
        }
    }

    /**
     * Defines the codec for the compound instance data.
     */
    public static final Codec<CompoundInstanceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Mode.CODEC.fieldOf("mode").forGetter(CompoundInstanceData::mode),
            AequivaleoExtraCodecs.setOf(ICompoundContainer.CODEC).fieldOf("containers").forGetter(CompoundInstanceData::containers),
            AequivaleoExtraCodecs.setOf(CompoundInstanceRef.CODEC).fieldOf("compoundInstances").forGetter(CompoundInstanceData::compoundInstances)
    ).apply(instance, CompoundInstanceData::new));

    /**
     * The disabled instance data.
     */
    public static final CompoundInstanceData DISABLED = new CompoundInstanceData(
            Mode.DISABLED,
            Sets.newLinkedHashSet(),
            Sets.newLinkedHashSet()
    );

    /**
     * Handles the data manipulation for the given target.
     *
     * @param data The data to manipulate the target with.
     */
    public void handle(
            final Map<ICompoundContainer<?>, Set<CompoundInstance>> data
    ) {
        mode().handleData(data, this);
    }
}
