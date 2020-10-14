package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.google.common.collect.*;
import com.ldtteam.aequivaleo.analyzer.StatCollector;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.iterator.AnalysisBFSGraphIterator;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SubCycleGraphNode implements IAnalysisGraphNode<Set<CompoundInstance>>
{
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<IAnalysisGraphNode<Set<CompoundInstance>>, IAnalysisGraphNode<Set<CompoundInstance>>> incomingNeighbors = Maps.newHashMap();
    private final Multimap<IAnalysisGraphNode<Set<CompoundInstance>>, IAnalysisGraphNode<Set<CompoundInstance>> neighborsIncoming = HashMultimap.create();
    private final Map<IAnalysisGraphNode<Set<CompoundInstance>>, IAnalysisGraphNode<Set<CompoundInstance>>> outgoingNeighbors = Maps.newHashMap();
    private final Map<IAnalysisGraphNode<Set<CompoundInstance>>, Set<Set<CompoundInstance>>> neighborCandidateValues = Maps.newHashMap();
    private final Set<Set<CompoundInstance>> fullCandidatesSet = Sets.newHashSet();
    private final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> innerGraph = new DefaultDirectedWeightedGraph<>(AccessibleWeightEdge.class);
    private boolean inComplete = false;

    public SubCycleGraphNode(
      Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> graph,
      List<IAnalysisGraphNode<Set<CompoundInstance>>> vertices
    ) {
        vertices.forEach(innerGraph::addVertex);
        vertices.stream()
          .peek(n -> graph.incomingEdgesOf(n).stream()
            .map(graph::getEdgeSource)
            .filter(s -> !vertices.contains(s))
            .distinct()
            .peek(s -> neighborsIncoming.put(n, s))
            .forEach(s -> incomingNeighbors.put(s, n)))
          .peek(n -> graph.outgoingEdgesOf(n).stream()
            .map(graph::getEdgeTarget)
            .filter(s -> !vertices.contains(s))
            .distinct()
            .forEach(t -> outgoingNeighbors.put(t, n)))
          .map(graph::outgoingEdgesOf)
          .flatMap(Collection::stream)
          .filter(e -> vertices.contains(graph.getEdgeTarget(e)))
          .forEach(e -> innerGraph.addEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e), e));
    }

    @NotNull
    @Override
    public Optional<Set<CompoundInstance>> getResultingValue()
    {
        return Optional.empty();
    }

    @Override
    public void addCandidateResult(final IAnalysisGraphNode<Set<CompoundInstance>> neighbor, final Set<CompoundInstance> instances)
    {
        if (!incomingNeighbors.containsKey(neighbor))
            return;

        fullCandidatesSet.add(instances);
        neighborCandidateValues.computeIfAbsent(neighbor, n -> Sets.newHashSet()).add(instances);
        incomingNeighbors.get(neighbor).addCandidateResult(neighbor, instances);
    }

    @NotNull
    @Override
    public Set<Set<CompoundInstance>> getCandidates()
    {
        return ImmutableSet.copyOf(fullCandidatesSet);
    }

    @NotNull
    @Override
    public Set<IAnalysisGraphNode<Set<CompoundInstance>>> getAnalyzedNeighbors()
    {
        return neighborCandidateValues.keySet();
    }

    @Override
    public void onReached(final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> graph)
    {
        for (AccessibleWeightEdge accessibleWeightEdge : graph.outgoingEdgesOf(this))
        {
            final IAnalysisGraphNode<Set<CompoundInstance>> v = graph.getEdgeTarget(accessibleWeightEdge);
            final IAnalysisGraphNode<Set<CompoundInstance>> innerSource = outgoingNeighbors.get(v);

            v.addCandidateResult(this, innerSource.getResultingValue().orElse(Collections.emptySet()));

            if (this.isIncomplete())
                v.setIncomplete();
        }
    }

    @Override
    public void collectStats(final StatCollector statCollector)
    {
        statCollector.onSubCycleNode();
    }

    @Override
    public void forceSetResult(final Set<CompoundInstance> compoundInstances)
    {
        innerGraph.vertexSet().forEach(n -> n.forceSetResult(compoundInstances));
    }

    @Override
    public void determineResult(final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> graph)
    {
        for (IAnalysisGraphNode<Set<CompoundInstance>> n : neighborCandidateValues.keySet())
        {
            if (!neighborCandidateValues.get(n).isEmpty())
            {
                IAnalysisGraphNode<Set<CompoundInstance>> startNode = incomingNeighbors.get(n);
                LOGGER.debug(String.format("Processing inner circle for: %s", startNode));
                //NOTE: Due to the way our system works, every node will have only one incoming edge!
                final Set<AccessibleWeightEdge> incomingEdges = Sets.newHashSet(innerGraph.incomingEdgesOf(startNode));
                final Map<AccessibleWeightEdge, IAnalysisGraphNode<Set<CompoundInstance>>> sourceMap =
                  incomingEdges.stream().collect(Collectors.toMap(Function.identity(), innerGraph::getEdgeSource));

                //We remove the inner edge of all edge
                for (AccessibleWeightEdge incomingEdge : incomingEdges)
                {
                    innerGraph.removeEdge(incomingEdge);
                }

                //Add the candidate values.
                neighborCandidateValues.get(n).forEach(candidates -> {
                    startNode.addCandidateResult(incomingNeighbors.get(startNode), candidates);
                });

                //Run inner analysis
                final AnalysisBFSGraphIterator<Set<CompoundInstance>> iterator = new AnalysisBFSGraphIterator<>(innerGraph, startNode);
                final StatCollector innerStatCollector = new StatCollector("Inner node analysis.", innerGraph.vertexSet().size());
                while (iterator.hasNext())
                {
                    iterator.next().collectStats(innerStatCollector);
                }

                //Re-add the original edges that where removed.
                sourceMap.forEach((edge, source) -> innerGraph.addEdge(source, startNode, edge));
            }
        }
    }

    @Override
    public void setIncomplete()
    {
        this.inComplete = true;
        innerGraph.vertexSet().forEach(IAnalysisGraphNode::setIncomplete);
    }

    @Override
    public boolean isIncomplete()
    {
        return this.inComplete || innerGraph.vertexSet().stream().anyMatch(IAnalysisGraphNode::isIncomplete);
    }

    @Override
    public void onNeighborReplaced(
      final IAnalysisGraphNode<Set<CompoundInstance>> originalNeighbor, final IAnalysisGraphNode<Set<CompoundInstance>> newNeighbor)
    {
        if (incomingNeighbors.containsKey(originalNeighbor)) {
            final IAnalysisGraphNode<Set<CompoundInstance>> inSource = incomingNeighbors.get(originalNeighbor);
            incomingNeighbors.remove(originalNeighbor);
            incomingNeighbors.put(newNeighbor, inSource);

            if (neighborsIncoming.containsValue(originalNeighbor)){
                final IAnalysisGraphNode<Set<CompoundInstance>>
            }
        }


        if (outgoingNeighbors.containsKey(originalNeighbor)) {
            final IAnalysisGraphNode<Set<CompoundInstance>> outSource = outgoingNeighbors.get(originalNeighbor);
            outgoingNeighbors.remove(originalNeighbor);
            outgoingNeighbors.put(newNeighbor, outSource);
        }
    }
}
