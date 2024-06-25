package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.base;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.*;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.ISimulationManager;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.PassthroughSimulationManager;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.utils.NodeUtils;
import com.ldtteam.aequivaleo.analysis.jgrapht.core.IAnalysisState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class InnerNode extends Node implements IInnerNode {

    private final INode[] nodes;
    private final ISimulationManager simulationManager;

    protected InnerNode(Collection<? extends INode> nodes) {
        this.nodes = rotateNodesIfNeedBe(nodes.toArray(INode[]::new));
        this.simulationManager = new PassthroughSimulationManager(this.nodes);
    }

    protected INode[] rotateNodesIfNeedBe(INode[] nodes) {
        return nodes;
    }

    @Override
    public INode[] nodes() {
        return nodes;
    }

    @Override
    public ICoreNode[] flatten() {
        return NodeUtils.flatten(nodes());
    }

    @Override
    public ISimulationManager simulationManager() {
        return simulationManager;
    }

    protected void determineStartingPoints(List<INode> analysisStartingPoints) {
        for (INode node : nodes()) {
            if (node instanceof IResultsOwningNode resultsOwningNode && !resultsOwningNode.results().simulate().isEmpty()) {
                analysisStartingPoints.add(node);
            }
        }

        if (analysisStartingPoints.isEmpty()) {
            for (INode node : nodes()) {
                if (!(node instanceof IRecipeNode)) {
                    analysisStartingPoints.add(node);
                }
            }
        }
    }

    protected record PreparationResult(IAnalysisState simulatedState, List<INode> analysisStartingPoints) {
    }

    protected void startSimulation() {
        for (INode coreNode : nodes()) {
            if (coreNode instanceof ISimulateableNode simulateableNode) {
                simulateableNode.simulationManager().push();
            }
        }
    }

    protected void commitSimulation() {
        for (INode coreNode : nodes()) {
            if (coreNode instanceof ISimulateableNode simulateableNode) {
                simulateableNode.simulationManager().commit();
            }
        }
    }

    protected void completeSimulation() {
        for (INode coreNode : nodes()) {
            if (coreNode instanceof ISimulateableNode simulateableNode) {
                simulateableNode.simulationManager().pop();
            }
        }
    }

    protected void analyzeSimulation(IAnalysisState state) {
        for (INode node : nodes()) {
            node.analyze(state);
        }
    }

    @Override
    protected int calculateHashCode() {
        return Arrays.hashCode(nodes);
    }

    @Override
    protected String calculateStringRepresentation() {
        return getClass().getSimpleName() + "{" +
                "nodes=" + Arrays.toString(nodes) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InnerNode innerNode = (InnerNode) o;
        return Objects.deepEquals(nodes, innerNode.nodes);
    }
}
