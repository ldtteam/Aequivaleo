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

public final class CompoundInstanceData
{
    public enum Mode
    {
        DISABLED((iCompoundContainerSetMap, compoundInstanceData) -> {
            //Noop since this is disabled.
        }),
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

        public void handleData(
          final Map<ICompoundContainer<?>, Set<CompoundInstance>> target,
          final CompoundInstanceData dataDrivenCompoundInstanceData
        )
        {
            this.handler.accept(target, dataDrivenCompoundInstanceData);
        }
    }

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

    public Mode getMode()
    {
        return mode;
    }

    public Set<ICompoundContainer<?>> getContainers()
    {
        return containers;
    }

    public Set<CompoundInstanceRef> getCompoundInstances()
    {
        return compoundInstances;
    }

    public Set<ICondition> getConditions()
    {
        return conditions;
    }

    public void handle(
      final Map<ICompoundContainer<?>, Set<CompoundInstance>> data
    )
    {
        getMode().handleData(data, this);
    }
}
