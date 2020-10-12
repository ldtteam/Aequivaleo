package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.ldtteam.aequivaleo.analyzer.StatCollector;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;

import java.util.*;
import java.util.stream.Collectors;

public class RecipeGraphNode extends AbstractAnalysisGraphNode
{
    @NotNull
    private final IEquivalencyRecipe recipe;

    public RecipeGraphNode(@NotNull final IEquivalencyRecipe recipe) {this.recipe = recipe;}

    @NotNull
    public IEquivalencyRecipe getRecipe()
    {
        return recipe;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RecipeGraphNode))
        {
            return false;
        }

        final RecipeGraphNode that = (RecipeGraphNode) o;

        return getRecipe().equals(that.getRecipe());
    }

    @Override
    public int hashCode()
    {
        return getRecipe().hashCode();
    }

    @Override
    public void onReached(final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> graph)
    {
        super.onReached(graph);

        final Set<ContainerWrapperGraphNode> resultNeighbors = new HashSet<>();
        for (AccessibleWeightEdge weightEdge : graph
                                                 .outgoingEdgesOf(this))
        {
            IAnalysisGraphNode<Set<CompoundInstance>> edgeTarget = graph.getEdgeTarget(weightEdge);
            if (edgeTarget instanceof ContainerWrapperGraphNode)
            {
                ContainerWrapperGraphNode wrapperGraphNode = (ContainerWrapperGraphNode) edgeTarget;
                resultNeighbors.add(wrapperGraphNode);
            }
        }

        final Set<ContainerWrapperGraphNode> requiredKnownOutputs = new HashSet<>();
        for (AccessibleWeightEdge accessibleWeightEdge : graph
                                                           .incomingEdgesOf(this))
        {
            IAnalysisGraphNode<Set<CompoundInstance>> v = graph.getEdgeSource(accessibleWeightEdge);
            if (v instanceof ContainerWrapperGraphNode)
            {
                ContainerWrapperGraphNode containerWrapperGraphNode = (ContainerWrapperGraphNode) v;
                requiredKnownOutputs.add(containerWrapperGraphNode);
            }
        }

        final Set<IngredientCandidateGraphNode> inputNeighbors = new HashSet<>();
        for (AccessibleWeightEdge accessibleWeightEdge : graph
                                                           .incomingEdgesOf(this))
        {
            IAnalysisGraphNode<Set<CompoundInstance>> v = graph.getEdgeSource(accessibleWeightEdge);
            if (v instanceof IngredientCandidateGraphNode)
            {
                IngredientCandidateGraphNode ingredientGraphNode = (IngredientCandidateGraphNode) v;
                inputNeighbors.add(ingredientGraphNode);
            }
        }

        final boolean isComplete = !hasIncompleteChildren(graph);

        final Set<CompoundInstance> summedCompoundInstances = new HashSet<>();
        for (Map.Entry<ICompoundType, Double> entry : inputNeighbors
                                                        .stream()
                                                        .peek(n -> n.determineResult(graph))
                                                        .flatMap(inputNeighbor -> inputNeighbor
                                                                                    .getResultingValue()
                                                                                    .orElseThrow(() -> new IllegalStateException("Calculation of node value not completed."))
                                                                                    .stream()
                                                                                    .filter(compoundInstance -> isComplete || compoundInstance.getType().getGroup().shouldIncompleteRecipeBeProcessed(getRecipe()))
                                                                                    .filter(compoundInstance -> compoundInstance.getType().getGroup().canContributeToRecipeAsInput(compoundInstance, getRecipe()))
                                                                                    .map(compoundInstance -> new HashMap.SimpleEntry<>(compoundInstance.getType(),
                                                                                      compoundInstance.getAmount()
                                                                                        * graph.getEdgeWeight(graph.getEdge(inputNeighbor, this)))))
                                                        .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, Double::sum))
                                                        .entrySet())
        {
            double amount = entry.getValue();
            for (final ContainerWrapperGraphNode requiredKnownOutput : requiredKnownOutputs)
            {
                for (final CompoundInstance compoundInstance : requiredKnownOutput.getResultingValue().orElse(Collections.emptySet()))
                {
                    if (compoundInstance.getType().equals(entry.getKey()))
                        amount = Math.max(0, amount - (compoundInstance.getAmount() * graph.getEdgeWeight(graph.getEdge(requiredKnownOutput, this))));
                }
            }

            if (amount >= 0) {
                CompoundInstance instance = new CompoundInstance(entry.getKey(), amount);
                summedCompoundInstances.add(instance);
            }
        }

        final double totalOutgoingEdgeWeight = graph.outgoingEdgesOf(this)
                                                 .stream()
                                                 .mapToDouble(graph::getEdgeWeight)
                                                 .sum();

        for (ContainerWrapperGraphNode neighbor : resultNeighbors)
        {
            Set<CompoundInstance> set = new HashSet<>();
            for (CompoundInstance compoundInstance : summedCompoundInstances)
            {
                final Double unitAmount = Math.floor(
                  compoundInstance.getAmount() / totalOutgoingEdgeWeight
                );

                CompoundInstance simpleCompoundInstance = new CompoundInstance(compoundInstance.getType(), unitAmount);
                if (simpleCompoundInstance.getAmount() >= 0)
                {
                    if (compoundInstance.getType().getGroup().isValidFor(neighbor.getWrapper(), simpleCompoundInstance))
                    {
                        if (compoundInstance.getType().getGroup().canContributeToRecipeAsOutput(neighbor.getWrapper(), this.getRecipe(), simpleCompoundInstance))
                        {
                            set.add(simpleCompoundInstance);
                        }
                    }
                }
            }

            neighbor.addCandidateResult(this, set);
        }
    }

    @Override
    public void collectStats(final StatCollector statCollector)
    {
        statCollector.onVisitRecipeNode();
    }

    @Override
    public String toString()
    {
        return "RecipeGraphNode{" +
                 "recipe=" + recipe +
                 '}';
    }

    @Override
    public void addCandidateResult(final IAnalysisGraphNode<Set<CompoundInstance>> neighbor, final Set<CompoundInstance> instances)
    {
        //Recipe nodes are special. They really do not care what their neighbor thinks of themselfs.
        //It will determine the value on its own and then update
        //Since the candidates are passed on as markers for when a neighbor is analyzed
        //We still need to update the super class.
        super.addCandidateResult(neighbor, Collections.emptySet());
    }

    @Override
    public void forceSetResult(final Set<CompoundInstance> compoundInstances)
    {
        //Again recipe nodes do not care.
        super.forceSetResult(null);
    }
}
