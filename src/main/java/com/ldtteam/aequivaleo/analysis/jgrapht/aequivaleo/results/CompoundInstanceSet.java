package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results;

import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CompoundInstanceSet implements Set<CompoundInstance> {

    public static CompoundInstanceSet of() {
        return new CompoundInstanceSet(new HashSet<>());
    }

    public static CompoundInstanceSet of(Object2DoubleMap<ICompoundType> inputs) {
        Set<CompoundInstance> compoundInstances = new HashSet<>();
        for (Object2DoubleMap.Entry<ICompoundType> entry : inputs.object2DoubleEntrySet()) {
            compoundInstances.add(new CompoundInstance(entry.getKey(), entry.getDoubleValue()));
        }
        return new CompoundInstanceSet(compoundInstances);
    }

    public static CompoundInstanceSet of(Set<CompoundInstance> compoundInstances) {
        return new CompoundInstanceSet(compoundInstances);
    }

    public static CompoundInstanceSet of(Collection<CompoundInstance> compoundInstances) {
        return new CompoundInstanceSet(new TreeSet<>(compoundInstances));
    }

    private final Set<CompoundInstance> compoundInstances;

    private CompoundInstanceSet(Set<CompoundInstance> compoundInstances) {
        this.compoundInstances = compoundInstances;
    }

    @Override
    public int size() {
        return compoundInstances.size();
    }

    @Override
    public boolean isEmpty() {
        return compoundInstances.isEmpty() || compoundInstances.stream().allMatch(CompoundInstance::isEmpty);
    }

    @Override
    public boolean contains(Object o) {
        return compoundInstances.contains(o);
    }

    @NotNull
    @Override
    public Iterator<CompoundInstance> iterator() {
        return compoundInstances.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return compoundInstances.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return compoundInstances.toArray(a);
    }

    @Override
    public boolean add(CompoundInstance compoundInstance) {
        return compoundInstances.add(compoundInstance);
    }

    @Override
    public boolean remove(Object o) {
        return compoundInstances.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return compoundInstances.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends CompoundInstance> c) {
        return compoundInstances.addAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return compoundInstances.retainAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return compoundInstances.removeAll(c);
    }

    @Override
    public void clear() {
        compoundInstances.clear();
    }

    @Override
    public Spliterator<CompoundInstance> spliterator() {
        return compoundInstances.spliterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompoundInstanceSet that = (CompoundInstanceSet) o;
        return Objects.equals(compoundInstances, that.compoundInstances);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(compoundInstances);
    }

    public CompoundInstanceSet scaled(double scale) {
        Set<CompoundInstance> scaledSet = new HashSet<>();
        for (CompoundInstance compoundInstance : compoundInstances) {
            scaledSet.add(compoundInstance.scaled(scale));
        }
        return new CompoundInstanceSet(scaledSet);
    }

    public CompoundInstanceSet combine(@Nullable CompoundInstanceSet other) {
        if (other == null) {
            return this;
        }

        final Object2DoubleMap<ICompoundType> combined = new Object2DoubleOpenHashMap<>();
        for (CompoundInstance compoundInstance : compoundInstances) {
            combined.compute(compoundInstance.getType(), (iCompoundType, current) -> {
                if (current == null) {
                    return compoundInstance.getAmount();
                } else {
                    return current + compoundInstance.getAmount();
                }
            });
        }
        for (CompoundInstance compoundInstance : other) {
            combined.compute(compoundInstance.getType(), (iCompoundType, current) -> {
                if (current == null) {
                    return compoundInstance.getAmount();
                } else {
                    return current + compoundInstance.getAmount();
                }
            });
        }

        return of(combined);
    }
}
