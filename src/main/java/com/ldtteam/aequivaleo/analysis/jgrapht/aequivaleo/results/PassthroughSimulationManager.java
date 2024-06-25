package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.ISimulateableNode;

public record PassthroughSimulationManager(INode[] nodes) implements ISimulationManager {


    @Override
    public void push() throws ResultsAlreadyPolledException {
        for (INode node : nodes) {
            if (node instanceof ISimulateableNode simulateableNode) {
                simulateableNode.simulationManager().push();
            }
        }
    }

    @Override
    public void pop() throws ResultsAlreadyPolledException {
        for (INode node : nodes) {
            if (node instanceof ISimulateableNode simulateableNode) {
                simulateableNode.simulationManager().pop();
            }
        }
    }

    @Override
    public void complete() throws ResultsAlreadyPolledException {
        for (INode node : nodes) {
            if (node instanceof ISimulateableNode simulateableNode) {
                simulateableNode.simulationManager().complete();
            }
        }
    }

    @Override
    public void commit() throws ResultsAlreadyPolledException {
        for (INode node : nodes) {
            if (node instanceof ISimulateableNode simulateableNode) {
                simulateableNode.simulationManager().commit();
            }
        }
    }
}
