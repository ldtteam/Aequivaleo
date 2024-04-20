package com.ldtteam.aequivaleo.api.compound.information.datagen.data;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Defines the data structure that is used to load compound instance data specifications from disk.
 */
public final class CompoundInstanceData
{
    /**
     * Defines the mode of the data application.
     * Used when multiple compound instance data specifications are loaded for the same container.
     */
    public enum Mode
    {
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
            dataDrivenCompoundInstanceData.getContainers().forEach(container -> {
                iCompoundContainerSetMap.computeIfAbsent(container
                  , (d) -> Sets.newLinkedHashSet()
                ).addAll(dataDrivenCompoundInstanceData
                           .getCompoundInstances()
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
            dataDrivenCompoundInstanceData.getContainers().forEach(container -> {
                iCompoundContainerSetMap.put(container,
                  dataDrivenCompoundInstanceData
                    .getCompoundInstances()
                    .stream()
                    .map(CompoundInstanceRef::get)
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
            });
        });

        private final BiConsumer<Map<ICompoundContainer<?>, Set<CompoundInstance>>, CompoundInstanceData> handler;

        Mode(final BiConsumer<Map<ICompoundContainer<?>, Set<CompoundInstance>>, CompoundInstanceData> handler) {this.handler = handler;}

        /**
         * Handles the data manipulation for the given target.
         *
         * @param target The target to manipulate.
         * @param dataDrivenCompoundInstanceData The data to drive the manipulation.
         */
        public void handleData(
          final Map<ICompoundContainer<?>, Set<CompoundInstance>> target,
          final CompoundInstanceData dataDrivenCompoundInstanceData
        )
        {
            this.handler.accept(target, dataDrivenCompoundInstanceData);
        }
    }

    /**
     * The disabled instance data.
     */
    public static final CompoundInstanceData DISABLED = new CompoundInstanceData(
      Mode.DISABLED,
      Sets.newLinkedHashSet(),
      Sets.newLinkedHashSet(),
      Sets.newLinkedHashSet()
    );

    private final Mode                       mode;
    private final Set<ICompoundContainer<?>> containers;
    private final Set<CompoundInstanceRef>   compoundInstances;
    private final Set<ICondition>            conditions;

    /**
     * Creates a new compound instance data specification.
     *
     * @param mode The mode of the data application.
     * @param containers The containers to apply the data to.
     * @param compoundInstances The compound instances to apply.
     * @param conditions The conditions to apply the data under.
     */
    public CompoundInstanceData(
      final Mode mode,
      final Set<ICompoundContainer<?>> containers,
      final Set<CompoundInstanceRef> compoundInstances,
      final Set<ICondition> conditions)
    {
        this.mode = mode;
        this.containers = containers;
        this.compoundInstances = compoundInstances;
        this.conditions = conditions;
    }

    /**
     * Gets the mode of the data application.
     *
     * @return The mode of the data application.
     */
    public Mode getMode()
    {
        return mode;
    }

    /**
     * Gets the containers to apply the data to.
     *
     * @return The containers to apply the data to.
     */
    public Set<ICompoundContainer<?>> getContainers()
    {
        return containers;
    }

    /**
     * Gets the compound instances to apply.
     *
     * @return The compound instances to apply.
     */
    public Set<CompoundInstanceRef> getCompoundInstances()
    {
        return compoundInstances;
    }

    /**
     * Gets the conditions to apply the data under.
     *
     * @return The conditions to apply the data under.
     */
    public Set<ICondition> getConditions()
    {
        return conditions;
    }

    /**
     * Handles the data manipulation for the given target.
     *
     * @param data The data to manipulate the target with.
     */
    public void handle(
      final Map<ICompoundContainer<?>, Set<CompoundInstance>> data
    )
    {
        getMode().handleData(data, this);
    }
}
