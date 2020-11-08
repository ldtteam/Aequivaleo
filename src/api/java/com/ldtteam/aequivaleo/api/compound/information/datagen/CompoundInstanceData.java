package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class CompoundInstanceData
{
    public enum Mode
    {
        ADDITIVE((iCompoundContainerSetMap, dataDrivenCompoundInstanceData) -> {
            dataDrivenCompoundInstanceData.getContainers().forEach(container -> {
                iCompoundContainerSetMap.computeIfAbsent(container
                  , (d) -> Sets.newHashSet()
                ).addAll(dataDrivenCompoundInstanceData
                           .getCompoundInstances()
                           .stream()
                           .map(CompoundInstanceRef::get)
                           .collect(Collectors.toSet()));
            });
        }),
        REPLACING((iCompoundContainerSetMap, dataDrivenCompoundInstanceData) -> {
            dataDrivenCompoundInstanceData.getContainers().forEach(container -> {
                iCompoundContainerSetMap.put(container,
                  dataDrivenCompoundInstanceData
                    .getCompoundInstances()
                    .stream()
                    .map(CompoundInstanceRef::get)
                    .collect(Collectors.toSet()));
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

    private final Mode                       mode;
    private final Set<ICompoundContainer<?>> containers;
    private final Set<CompoundInstanceRef>   compoundInstances;

    public CompoundInstanceData(
      final Mode mode,
      final Set<ICompoundContainer<?>> containers,
      final Set<CompoundInstanceRef> compoundInstances)
    {
        this.mode = mode;
        this.containers = containers;
        this.compoundInstances = compoundInstances;
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

    public void handle(
      final Map<ICompoundContainer<?>, Set<CompoundInstance>> data
    )
    {
        getMode().handleData(data, this);
    }
}
