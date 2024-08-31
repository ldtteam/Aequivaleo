package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.base;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.*;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.ISimulationManager;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.PassthroughSimulationManager;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.utils.NodeUtils;

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
    public Iterator<ICoreNode> flatten() {
        return NodeUtils.flatten(nodes());
    }

    @Override
    public ISimulationManager simulationManager() {
        return simulationManager;
    }

    @Override
    public boolean requiresCalculation() {
        final List<INode> analysisStartingPoints = determineStartingPoints();

        if (analysisStartingPoints.isEmpty()) {
            return false;
        }

        for (INode analysisStartingPoint : analysisStartingPoints) {
            if (analysisStartingPoint instanceof ISimulateableNode simulateableNode && simulateableNode.requiresCalculation()) {
                return true;
            }
        }

        return false;
    }

    protected List<INode> determineStartingPoints() {
        final List<INode> analysisStartingPoints = new ArrayList<>(nodes().length);
        for (INode node : nodes()) {
            if (node.canPropagate()) {
                analysisStartingPoints.add(node);
            }
        }

        return analysisStartingPoints;
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
