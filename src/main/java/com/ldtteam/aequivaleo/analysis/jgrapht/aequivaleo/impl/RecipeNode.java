package com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.impl;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.ICoreNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IRecipeNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IResultsOwningNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.base.CoreNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.CompoundInstanceSet;
import com.ldtteam.aequivaleo.analysis.jgrapht.core.IAnalysisState;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RecipeNode extends CoreNode implements IRecipeNode {

    private final IEquivalencyRecipe recipe;

    public RecipeNode(IEquivalencyRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public @NotNull IEquivalencyRecipe recipe() {
        return recipe;
    }

    @Override
    public NodeType type() {
        return NodeType.RECIPE;
    }

    @Override
    public void addInput(ICoreNode input, double weight) {
        if (!(input instanceof IResultsOwningNode))
            throw new IllegalArgumentException("RecipeNode can only have ResultsOwningNode inputs");

        super.addInput(input, weight);
    }

    @Override
    public Collection<? extends IResultsOwningNode> inputs() {
        return super.inputs().stream()
                .map(IResultsOwningNode.class::cast)
                .collect(Collectors.toSet());
    }

    @Override
    public void analyze(IAnalysisState state) {
        super.analyze(state);

        CompoundInstanceSet workingSet = CompoundInstanceSet.of();

        final Set<ICompoundTypeGroup> groupsOnAllInputs = new HashSet<>();
        boolean initializedGroups = false;
        for (ICoreNode inputNode : inputs.keySet()) {
            if (inputNode instanceof IResultsOwningNode resultsOwningNode) {
                final CompoundInstanceSet inputResults = state.doWhen(
                        resultsOwningNode.results()::simulate,
                        resultsOwningNode.results()::results
                );
                final CompoundInstanceSet weightedResults = inputResults.scaled(inputs.getOrDefault(resultsOwningNode, 1d));
                workingSet = workingSet.combine(weightedResults);

                if (!initializedGroups) {
                    groupsOnAllInputs.addAll(inputResults.stream()
                            .map(CompoundInstance::getType)
                            .map(ICompoundType::getGroup)
                            .collect(Collectors.toSet()));
                    initializedGroups = true;
                } else {
                    groupsOnAllInputs.retainAll(inputResults.stream()
                            .map(CompoundInstance::getType)
                            .map(ICompoundType::getGroup)
                            .collect(Collectors.toSet()));
                }
            } else {
                throw new IllegalStateException("RecipeNode has an input that is not a ResultsOwningNode");
            }
        }

        final Map<ICompoundTypeGroup, Collection<CompoundInstance>> groupedInstances = GroupingUtils.groupByUsingSetToMap(
                workingSet,
                compoundInstance -> compoundInstance.getType().getGroup()
        );

        final CompoundInstanceSet reducedSet = CompoundInstanceSet.of(groupedInstances.entrySet().stream()
                .flatMap(entry -> {
                    if (!groupsOnAllInputs.contains(entry.getKey()) && !entry.getKey().shouldIncompleteRecipeBeProcessed(this.recipe))
                        return Stream.empty();

                    return entry.getKey().adaptRecipeResult(this.recipe, entry.getValue()).stream();
                })
                .collect(Collectors.toSet()));

        double totalOutputWeight = outputs.values().doubleStream().sum();
        for (ICoreNode outputNode : outputs.keySet()) {
            double scaledOutputWeight = 1d / totalOutputWeight;
            if (outputNode instanceof IResultsOwningNode resultsOwningNode) {
                resultsOwningNode.results().offer(reducedSet.scaled(scaledOutputWeight));
            } else {
                throw new IllegalStateException("RecipeNode has an output that is not a ResultsOwningNode");
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipeNode that = (RecipeNode) o;
        return Objects.equals(recipe, that.recipe);
    }

    @Override
    protected int calculateHashCode() {
        return Objects.hashCode(recipe);
    }

    @Override
    protected String calculateStringRepresentation() {
        return "RecipeNode{" +
                "recipe=" + recipe +
                '}';
    }
}
