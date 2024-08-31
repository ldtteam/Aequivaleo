package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.ISimulateableNode;

public record PassthroughSimulationManager(INode[] nodes) implements ISimulationManager {


    @Override
    public void push() {
        for (INode node : nodes) {
            if (node instanceof ISimulateableNode simulateableNode) {
                simulateableNode.simulationManager().push();
            }
        }
    }

    @Override
    public void pop() {
        for (INode node : nodes) {
            if (node instanceof ISimulateableNode simulateableNode) {
                simulateableNode.simulationManager().pop();
            }
        }
    }

    @Override
    public void complete() {
        for (INode node : nodes) {
            if (node instanceof ISimulateableNode simulateableNode) {
                simulateableNode.simulationManager().complete();
            }
        }
    }

    @Override
    public void commit() {
        for (INode node : nodes) {
            if (node instanceof ISimulateableNode simulateableNode) {
                simulateableNode.simulationManager().commit();
            }
        }
    }
}
