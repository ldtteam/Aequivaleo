package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.impl;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IRecipeNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IResultsOwningNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.base.InnerNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.CompoundInstanceSet;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.IResultsContainer;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.SimulateableResultsContainer;
import com.ldtteam.aequivaleo.analysis.jgrapht.core.IAnalysisState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class CliqueNode extends InnerNode {

    public CliqueNode(Collection<? extends INode> nodes) {
        super(nodes);

        for (INode node : nodes) {
            if (node instanceof InnerNode) {
                throw new IllegalArgumentException("Clique nodes cannot contain inner nodes.");
            }
        }
    }

    @Override
    public NodeType type() {
        return NodeType.CLIQUE;
    }

    @Override
    public void analyze(IAnalysisState state) {
        super.analyze(state);
        
        final IAnalysisState simulationState = state.simulate();

        final List<INode> analysisStartingPoints = determineStartingPoints();

        //Next collect all candidates
        final Set<CompoundInstanceSet> candidates = analysisStartingPoints
                .stream()
                .filter(IResultsOwningNode.class::isInstance)
                .map(IResultsOwningNode.class::cast)
                .map(IResultsOwningNode::results)
                .map(results -> simulationState.doWhen(
                        results::simulate,
                        results::results
                ))
                .collect(Collectors.toSet());

        final IResultsContainer container = new SimulateableResultsContainer(this);
        candidates.forEach(container::offer);
        final CompoundInstanceSet completeSimulation = simulationState.doWhen(
                container::simulate,
                container::results
        );

        for (INode node : nodes()) {
            if (node instanceof IResultsOwningNode resultsOwningNode) {
                resultsOwningNode.results().force(completeSimulation);
                resultsOwningNode.analyze(state);
            }
        }
    }

    @Override
    protected List<INode> determineStartingPoints() {
        final List<INode> analysisStartingPoints = new ArrayList<>(nodes().length);

        //First check for container nodes with results
        for (INode node : nodes()) {
            if (node instanceof IResultsOwningNode resultsOwningNode && !resultsOwningNode.results().simulate().isEmpty()) {
                analysisStartingPoints.add(node);
            }
        }

        //If we have any, we take those!
        if (!analysisStartingPoints.isEmpty()) {
            return analysisStartingPoints;
        }

        //If we don't have any, we take all nodes that are recipes, who's inputs all have results
        for (INode node : nodes()) {
            if (node instanceof IRecipeNode recipeNode) {
                if (recipeNode.inputs().stream().noneMatch(input -> input.results().simulate().isEmpty())) {
                    analysisStartingPoints.add(node);
                }
            }
        }

        return analysisStartingPoints;
    }
}
