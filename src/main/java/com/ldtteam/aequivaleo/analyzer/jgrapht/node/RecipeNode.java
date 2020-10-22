package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.analyzer.StatCollector;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.*;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class RecipeNode extends AbstractNode implements IRecipeNode
{
    @NotNull
    private final IEquivalencyRecipe recipe;

    public RecipeNode(@NotNull final IEquivalencyRecipe recipe) {this.recipe = recipe;}

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
        if (!(o instanceof RecipeNode))
        {
            return false;
        }

        final RecipeNode that = (RecipeNode) o;

        return getRecipe().equals(that.getRecipe());
    }

    @Override
    public int hashCode()
    {
        return getRecipe().hashCode();
    }

    @Override
    public void determineResult(final IGraph graph)
    {
        final Set<IRecipeResidueNode> requiredKnownOutputs = graph.incomingEdgesOf(this).stream().map(graph::getEdgeSource)
                                                               .filter(IRecipeResidueNode.class::isInstance)
                                                               .map(IRecipeResidueNode.class::cast)
                                                               .collect(Collectors.toSet());
        final Set<IRecipeInputNode> inputNeighbors = graph.incomingEdgesOf(this).stream().map(graph::getEdgeSource)
                                                       .filter(IRecipeInputNode.class::isInstance)
                                                       .map(IRecipeInputNode.class::cast)
                                                       .collect(Collectors.toSet());

        final boolean isComplete = !hasIncompleteChildren(graph);

        final Set<CompoundInstance> summedCompoundInstances = new HashSet<>();
        for (Map.Entry<ICompoundType, Double> entry : inputNeighbors
                                                        .stream()
                                                        .flatMap(inputNeighbor -> inputNeighbor
                                                                                    .getInputInstances(this)
                                                                                    .stream()
                                                                                    .filter(compoundInstance -> isComplete || compoundInstance.getType()
                                                                                                                                .getGroup()
                                                                                                                                .shouldIncompleteRecipeBeProcessed(getRecipe()))
                                                                                    .filter(compoundInstance -> compoundInstance.getType().getGroup().canContributeToRecipeAsInput(recipe, compoundInstance))
                                                                                    .map(compoundInstance -> new HashMap.SimpleEntry<>(compoundInstance.getType(),
                                                                                      compoundInstance.getAmount()
                                                                                        * graph.getEdgeWeight(graph.getEdge(inputNeighbor, this)))))
                                                        .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, Double::sum))
                                                        .entrySet())
        {
            double amount = entry.getValue();
            for (final IRecipeResidueNode requiredKnownOutput : requiredKnownOutputs)
            {
                for (final CompoundInstance compoundInstance : requiredKnownOutput.getResidueInstances(this))
                {
                    if (compoundInstance.getType().equals(entry.getKey()))
                    {
                        amount = Math.max(0, amount - (compoundInstance.getAmount() * graph.getEdgeWeight(graph.getEdge(requiredKnownOutput, this))));
                    }
                }
            }

            if (amount >= 0)
            {
                CompoundInstance instance = new CompoundInstance(entry.getKey(), amount);
                summedCompoundInstances.add(instance);
            }
        }

        this.forceSetResult(summedCompoundInstances, false);
    }

    @Override
    public void onReached(final IGraph graph)
    {
        for (IEdge accessibleWeightEdge : graph.outgoingEdgesOf(this))
        {
            INode v = graph.getEdgeTarget(accessibleWeightEdge);
            if (this.isIncomplete())
                v.setIncomplete();
        }

        final Set<IRecipeOutputNode> resultNeighbors = new HashSet<>();
        for (IEdge weightEdge : graph
                                  .outgoingEdgesOf(this))
        {
            INode edgeTarget = graph.getEdgeTarget(weightEdge);
            if (edgeTarget instanceof IRecipeOutputNode)
            {
                IRecipeOutputNode containerNode = (IRecipeOutputNode) edgeTarget;
                resultNeighbors.add(containerNode);
            }
        }

        final double totalOutgoingEdgeWeight = graph.outgoingEdgesOf(this)
                                                 .stream()
                                                 .mapToDouble(graph::getEdgeWeight)
                                                 .sum();

        for (IRecipeOutputNode neighbor : resultNeighbors)
        {
            Set<CompoundInstance> set = new HashSet<>();
            for (CompoundInstance totalCompoundInstance : getResultingValue().orElse(Collections.emptySet()))
            {
                final Double unitAmount = Math.floor(
                  totalCompoundInstance.getAmount() / totalOutgoingEdgeWeight
                );

                CompoundInstance compoundInstance = new CompoundInstance(totalCompoundInstance.getType(), unitAmount);
                if (!compoundInstance.getType().getGroup().canContributeToRecipeAsOutput(recipe, compoundInstance))
                {
                    continue;
                }

                set.add(compoundInstance);
            }

            neighbor.addCandidateResult(this, graph.getEdge(this, neighbor), set);
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
        return "RecipeNode{" + recipe +
                 '}';
    }

    @Override
    public void addCandidateResult(final INode neighbor, final IEdge sourceEdge, final Set<CompoundInstance> instances)
    {
        super.addCandidateResult(neighbor, sourceEdge, Collections.emptySet());
    }

    @Override
    public void forceSetResult(final Set<CompoundInstance> compoundInstances)
    {
        //Again recipe nodes do not care.
        forceSetResult(compoundInstances, true);
    }

    private void forceSetResult(final Set<CompoundInstance> compoundInstances, boolean external)
    {
        //Again recipe nodes do not care.
        super.forceSetResult(external ? null : compoundInstances);
    }
}
