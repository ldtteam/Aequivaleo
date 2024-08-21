package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.base;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.ICoreNode;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSets;

import java.util.Collection;

public abstract class CoreNode extends Node implements ICoreNode  {

    protected final Object2DoubleMap<ICoreNode> inputs = new Object2DoubleOpenHashMap<>();
    protected final Object2DoubleMap<ICoreNode> outputs = new Object2DoubleOpenHashMap<>();

    @Override
    public void addInput(ICoreNode input, double weight) {
        inputs.put(input, weight);
    }

    @Override
    public void addOutput(ICoreNode output, double weight) {
        outputs.put(output, weight);
    }

    @Override
    public Collection<? extends ICoreNode> inputs() {
        return ObjectSets.unmodifiable(inputs.keySet());
    }

    @Override
    public Collection<? extends ICoreNode> outputs() {
        return ObjectSets.unmodifiable(outputs.keySet());
    }

    @Override
    public void clearInputs() {
        inputs.clear();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeOutput(ICoreNode input) {
        outputs.remove(input);
    }
}
