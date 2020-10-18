package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.ldtteam.aequivaleo.analyzer.StatCollector;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.*;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class RecipeNode extends AbstractNode
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
        final Set<IRecipeResidueNode> requiredKnownOutputs = extractRequiredKnownOutputNeighborsFromGraph(graph.incomingEdgesOf(this).stream().map(graph::getEdgeSource).collect(
          Collectors.toSet()));
        final Set<IRecipeInputNode> inputNeighbors = extractInputNeighborsFromGraph(graph.incomingEdgesOf(this).stream().map(graph::getEdgeSource).collect(Collectors.toSet()));

        final boolean isComplete = !hasIncompleteChildren(graph);

        final Set<CompoundInstance> summedCompoundInstances = new HashSet<>();
        for (Map.Entry<ICompoundType, Double> entry : inputNeighbors
                                                        .stream()
                                                        .peek(n -> n.determineResult(graph))
                                                        .flatMap(inputNeighbor -> inputNeighbor
                                                                                    .getResultingValue()
                                                                                    .orElseThrow(() -> new IllegalStateException("Calculation of node value not completed."))
                                                                                    .stream()
                                                                                    .filter(compoundInstance -> isComplete || compoundInstance.getType()
                                                                                                                                .getGroup()
                                                                                                                                .shouldIncompleteRecipeBeProcessed(getRecipe()))
                                                                                    .filter(compoundInstance -> compoundInstance.getType()
                                                                                                                  .getGroup()
                                                                                                                  .canContributeToRecipeAsInput(getRecipe(), compoundInstance))
                                                                                    .map(compoundInstance -> new HashMap.SimpleEntry<>(compoundInstance.getType(),
                                                                                      compoundInstance.getAmount()
                                                                                        * graph.getEdgeWeight(graph.getEdge(inputNeighbor, this)))))
                                                        .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, Double::sum))
                                                        .entrySet())
        {
            double amount = entry.getValue();
            for (final IContainerNode requiredKnownOutput : requiredKnownOutputs)
            {
                for (final CompoundInstance compoundInstance : requiredKnownOutput.getResultingValue().orElse(Collections.emptySet()))
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

    private double getSourceEdgeWeight(final IGraph graph, final INode source) {
        if (source instanceof IIOAwareNode) {
            final IGraph ioGraph = ((IIOAwareNode) source).getIOGraph(graph);
            return ioGraph.getEdgeWeight(ioGraph.getEdge())
        }
    }

    @Override
    public void onReached(final IGraph graph)
    {
        super.onReached(graph);

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

        for (INode neighbor : resultNeighbors)
        {
            Set<CompoundInstance> set = new HashSet<>();
            for (CompoundInstance compoundInstance : getResultingValue().orElse(Collections.emptySet()))
            {
                final Double unitAmount = Math.floor(
                  compoundInstance.getAmount() / totalOutgoingEdgeWeight
                );

                CompoundInstance simpleCompoundInstance = new CompoundInstance(compoundInstance.getType(), unitAmount);
                if (!simpleCompoundInstance.getType().getGroup().canContributeToRecipeAsOutput(getRecipe(), simpleCompoundInstance))
                    continue;

                set.add(simpleCompoundInstance);
            }

            neighbor.addCandidateResult(this, set);
        }
    }

    private Set<IRecipeInputNode> extractInputNeighborsFromGraph(
      final Set<INode> targetVertices
    ) {
        final Set<IRecipeInputNode> inputNeighbors = new HashSet<>();
        for (INode v : targetVertices)
        {
            if (v instanceof IInnerGraphNode)
            {
                final IInnerGraphNode s = (IInnerGraphNode) v;
                inputNeighbors.addAll(extractInputNeighborsFromGraph(s.getSourceNeighborOf(this)));
            }
            else if (v instanceof IRecipeInputNode) {
                inputNeighbors.add((IRecipeInputNode) v);
            }
        }

        return inputNeighbors;
    }

    private Set<IRecipeResidueNode> extractRequiredKnownOutputNeighborsFromGraph(
      final Set<INode> targetVertices
    ) {
        final Set<IRecipeResidueNode> requiredKnownOutputNeighbors = new HashSet<>();
        for (INode v : targetVertices)
        {
            if (v instanceof IInnerGraphNode)
            {
                final IInnerGraphNode s = (IInnerGraphNode) v;
                requiredKnownOutputNeighbors.addAll(extractRequiredKnownOutputNeighborsFromGraph(s.getSourceNeighborOf(this)));
            }
            else if (v instanceof IRecipeResidueNode) {
                requiredKnownOutputNeighbors.add((IRecipeResidueNode) v);
            }
        }

        return requiredKnownOutputNeighbors;
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
    public void addCandidateResult(final INode neighbor, final Set<CompoundInstance> instances)
    {
        //Recipe nodes are special. They really do not care what their neighbor thinks of them selfs.
        //It will determine the value on its own and then update
        //Since the candidates are passed on as markers for when a neighbor is analyzed
        //We still need to update the super class.
        super.addCandidateResult(neighbor, Collections.emptySet());
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
