package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;

import java.util.*;
import java.util.stream.Collectors;

public class IngredientCandidateGraphNode extends AbstractAnalysisGraphNode
{
    @NotNull
    private final IRecipeIngredient ingredient;

    public IngredientCandidateGraphNode(@NotNull final IRecipeIngredient ingredient) {this.ingredient = ingredient;}

    @NotNull
    public IRecipeIngredient getIngredient()
    {
        return ingredient;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof IngredientCandidateGraphNode))
        {
            return false;
        }

        final IngredientCandidateGraphNode that = (IngredientCandidateGraphNode) o;

        return getIngredient().equals(that.getIngredient());
    }

    @Override
    public int hashCode()
    {
        return getIngredient().hashCode();
    }

    @Override
    public String toString()
    {
        return "IngredientCandidateGraphNode{" +
                 "ingredient=" + ingredient +
                 '}';
    }

    @Override
    public void onReached(final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> graph)
    {
        doNodeCalculation(isComplete(graph));
        super.onReached(graph);
    }

    private void doNodeCalculation(final boolean incomplete)
    {
        Map<ICompoundTypeGroup, List<Triple<ICompoundTypeGroup, ICompoundContainer<?>, Set<CompoundInstance>>>> map = new HashMap<>();
        for (IAnalysisGraphNode<Set<CompoundInstance>> edgeSource : this.getAnalyzedNeighbors())
        {
            if (edgeSource instanceof ContainerWrapperGraphNode)
            {
                ContainerWrapperGraphNode n = (ContainerWrapperGraphNode) edgeSource;
                Pair<? extends ICompoundContainer<?>, Set<CompoundInstance>> of = Pair.of(n.getWrapper(), n.getResultingValue().orElse(Collections.emptySet()));
                Pair<? extends ICompoundContainer<?>, Collection<Collection<CompoundInstance>>> collectionPair =
                  Pair.of(of.getLeft(), GroupingUtils.groupBy(of.getValue(), i -> i.getType().getGroup()));
                for (Collection<CompoundInstance> l : collectionPair.getRight())
                {
                    Triple<ICompoundTypeGroup, ICompoundContainer<?>, Set<CompoundInstance>> iCompoundTypeGroupSetTriple =
                      Triple.of(l.iterator().next().getType().getGroup(), collectionPair.getLeft(), new HashSet<>(l));
                    map.computeIfAbsent(iCompoundTypeGroupSetTriple.getLeft(), k -> new ArrayList<>()).add(iCompoundTypeGroupSetTriple);
                }
            }
        }

        final Set<CompoundInstance> resultingSet = Sets.newHashSet();

        map
          .entrySet()
          .stream()
          .map(e -> Pair.of(e.getKey(), e.getValue()
                                          .stream()
                                          .map(t -> Pair.of(t.getMiddle(), t.getRight()))
                                          .collect(Collectors.toMap(Pair::getLeft, Pair::getRight))))
          .forEach(p -> {
              final ICompoundTypeGroup group = p.getKey();
              final Map<? extends ICompoundContainer<?>, Set<CompoundInstance>> data = p.getRight();

              if (data.size() == 1 && !incomplete) {
                  resultingSet.addAll(data.values().iterator().next());
              }
              else if (data.size() > 1 || incomplete) {
                  final Set<CompoundInstance> handledData = group.handleIngredient(data, incomplete);
                  resultingSet.addAll(handledData);
              }
          });

        this.getCandidates().add(resultingSet);
    }
}
