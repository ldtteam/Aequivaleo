package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.impl;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IInnerNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IResultsOwningNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.base.InnerNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.core.IAnalysisState;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class CycleNode extends InnerNode {

    public CycleNode(Collection<? extends INode> nodes) {
        super(Validate.notEmpty(nodes));
    }

    @Override
    protected INode[] rotateNodesIfNeedBe(INode[] nodes) {
        if (nodes[0] instanceof IResultsOwningNode || nodes[0] instanceof IInnerNode)
            return nodes;

        INode[] rotatedNodes = new INode[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            rotatedNodes[i] = nodes[(i + 1) % nodes.length];
        }

        return this.rotateNodesIfNeedBe(rotatedNodes);
    }

    @Override
    public NodeType type() {
        return NodeType.CYCLE;
    }

    @Override
    public void analyze(IAnalysisState state) {
        super.analyze(state);
        
        startSimulation();
        
        final IAnalysisState simulationState = state.simulate();
        final List<INode> analysisStartingPoints = new ArrayList<>(nodes().length);
        determineStartingPoints(analysisStartingPoints);
        
        if (analysisStartingPoints.size() == 1) {
            runAnalysisWithStartPoint(analysisStartingPoints, simulationState, 0);
        } else {
            //For each of the starting nodes: Create an iteration order and analyze the nodes.
            for (int i = 0; i < analysisStartingPoints.size(); i++) {
                runAnalysisWithStartPoint(analysisStartingPoints, simulationState, i);
            }
        }

        commitSimulation();
    }

    private void runAnalysisWithStartPoint(List<INode> analysisStartingPoints, IAnalysisState simulationState, int i) {

        startSimulation();

        INode node = analysisStartingPoints.get(i);
        node.analyze(simulationState);

        final INode[] iterationOrder = createIterationOrder(node);

        for (INode innerNode : iterationOrder) {
            innerNode.analyze(simulationState);
        }

        commitSimulation();
    }

    private int indexOfNodeInNodes(INode node) {
        for (int i = 0; i < nodes().length; i++) {
            if (nodes()[i] == node) {
                return i;
            }
        }

        throw new IllegalStateException("Node not found in nodes.");
    }

    private INode[] createIterationOrder(INode node) {
        if (nodes().length == 1) {
            return new INode[0];
        }

        if (nodes().length == 2) {
            return new INode[] {nodes()[0]};
        }

        if (nodes()[0] == node) {
            return Arrays.copyOfRange(nodes(), 1, nodes().length);
        }

        if (nodes()[nodes().length - 1] == node) {
            return Arrays.copyOfRange(nodes(), 0, nodes().length - 1);
        }

        int indexOfNode = indexOfNodeInNodes(node);
        INode[] iterationOrder = new INode[nodes().length - 1];
        for (int i = 0; i < nodes().length - 1; i++) {
            iterationOrder[i] = nodes()[(indexOfNode + i + 1) % nodes().length];
        }

        return iterationOrder;
    }
}
