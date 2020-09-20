package com.ldtteam.aequivaleo.api.compound.information;

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
            iCompoundContainerSetMap.computeIfAbsent(dataDrivenCompoundInstanceData.getContainer()
              , (d) -> Sets.newHashSet()
            ).addAll(dataDrivenCompoundInstanceData
                       .getCompoundInstances()
                       .stream()
                       .map(CompoundInstanceRef::get)
                       .collect(Collectors.toSet()));
        }),
        REPLACING((iCompoundContainerSetMap, dataDrivenCompoundInstanceData) -> {
            iCompoundContainerSetMap.putIfAbsent(dataDrivenCompoundInstanceData.getContainer(),
              dataDrivenCompoundInstanceData
                .getCompoundInstances()
                .stream()
                .map(CompoundInstanceRef::get)
                .collect(Collectors.toSet()));
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

    private final Mode                     mode;
    private final ICompoundContainer<?>    container;
    private final Set<CompoundInstanceRef> compoundInstances;

    public CompoundInstanceData(
      final Mode mode,
      final ICompoundContainer<?> container,
      final Set<CompoundInstanceRef> compoundInstances)
    {
        this.mode = mode;
        this.container = container;
        this.compoundInstances = compoundInstances;
    }

    public Mode getMode()
    {
        return mode;
    }

    public ICompoundContainer<?> getContainer()
    {
        return container;
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
