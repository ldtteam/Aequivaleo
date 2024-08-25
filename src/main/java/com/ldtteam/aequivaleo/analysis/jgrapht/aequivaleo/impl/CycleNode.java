package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.impl;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IInnerNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IResultsOwningNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.base.InnerNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.core.IAnalysisState;
import org.apache.commons.lang3.Validate;

import java.util.*;

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
        final List<INode> analysisStartingPoints = determineStartingPoints();

        if (analysisStartingPoints.size() == 1) {
            runAnalysisWithStartPoint(analysisStartingPoints, simulationState, 0);
        } else {
            //For each of the starting nodes: Create an iteration order and analyze the nodes.
            for (int i = 0; i < analysisStartingPoints.size(); i++) {
                if (!runAnalysisWithStartPoint(analysisStartingPoints, simulationState, i)) {
                    //No reanalysis required, break out of the loop.
                    break;
                }
            }
        }

        commitSimulation();
    }

    private boolean runAnalysisWithStartPoint(List<INode> analysisStartingPoints, IAnalysisState simulationState, int i) {

        startSimulation();

        INode node = analysisStartingPoints.get(i);
        node.analyze(simulationState);

        final Iterator<INode> iterationOrder = createIterationOrder(node);

        boolean requiresReanalysis = false;

        while (iterationOrder.hasNext()) {
            INode nextNode = iterationOrder.next();
            nextNode.analyze(simulationState);

            if (nextNode instanceof IResultsOwningNode resultsOwningNode && resultsOwningNode.results().requiresCalculation()) {
                requiresReanalysis = true;
            }
        }

        commitSimulation();

        return requiresReanalysis;
    }

    private int indexOfNodeInNodes(INode node) {
        for (int i = 0; i < nodes().length; i++) {
            if (nodes()[i] == node) {
                return i;
            }
        }

        throw new IllegalStateException("Node not found in nodes.");
    }

    private Iterator<INode> createIterationOrder(INode node) {
        if (nodes().length == 1) {
            return Collections.emptyIterator();
        }

        return new Iterator<>() {

            private final int startIndex = indexOfNodeInNodes(node);
            private int currentIndex = (startIndex + 1) % nodes().length;

            @Override
            public boolean hasNext() {
                return currentIndex != startIndex;
            }

            @Override
            public INode next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                INode nextNode = nodes()[currentIndex];
                currentIndex = (currentIndex + 1) % nodes().length;
                return nextNode;
            }
        };
    }
}
