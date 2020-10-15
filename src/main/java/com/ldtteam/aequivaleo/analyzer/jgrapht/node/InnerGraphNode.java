package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.ldtteam.aequivaleo.analyzer.StatCollector;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.*;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.*;
import java.util.stream.Collectors;

public class InnerGraphNode
  implements IInnerGraphNode, IContainerNode, IIOAwareNode
{
    private static final Logger LOGGER = LogManager.getLogger();

    private final Graph<INode, IEdge> ioGraph = new DirectedWeightedMultigraph<>(AccessibleWeightEdge.class);
    private final Graph<INode, IEdge> innerGraph = new DirectedWeightedMultigraph<>(AccessibleWeightEdge.class);

    @Override
    public Graph<INode, IEdge> getIOGraph(final Graph<INode, IEdge> graph)
    {
        return ioGraph;
    }

    @Override
    public Optional<ICompoundContainer<?>> getWrapper()
    {
        return Optional.empty();
    }

    @Override
    public Set<ICompoundContainer<?>> getTargetedWrapper(final INode sourceNeighbor)
    {
        if (!ioGraph.containsVertex(sourceNeighbor))
            return Collections.emptySet();

        return ioGraph.outgoingEdgesOf(sourceNeighbor)
          .stream()
          .map(ioGraph::getEdgeTarget)
          .filter(IContainerNode.class::isInstance)
          .map(IContainerNode.class::cast)
          .flatMap(cn -> cn.getTargetedWrapper(sourceNeighbor).stream())
          .collect(Collectors.toSet());
    }

    @Override
    public Set<ICompoundContainer<?>> getSourcedWrapper(final INode targetNeighbor)
    {
        if (!ioGraph.containsVertex(targetNeighbor))
        return Collections.emptySet();

        return ioGraph.incomingEdgesOf(targetNeighbor)
                 .stream()
                 .map(ioGraph::getEdgeSource)
                 .filter(IContainerNode.class::isInstance)
                 .map(IContainerNode.class::cast)
                 .flatMap(cn -> cn.getSourcedWrapper(targetNeighbor).stream())
                 .collect(Collectors.toSet());
    }

    @Override
    public Set<INode> getInnerNodes()
    {
        return innerGraph.vertexSet();
    }

    @Override
    public Set<INode> getSourceNeighborOf(final INode neighbor)
    {
        if (!ioGraph.containsVertex(neighbor))
            return Collections.emptySet();

        return ioGraph.incomingEdgesOf(neighbor)
          .stream()
          .map(ioGraph::getEdgeSource)
          .collect(Collectors.toSet());
    }

    @Override
    public Set<INode> getTargetNeighborOf(final INode neighbor)
    {
        if (!ioGraph.containsVertex(neighbor))
            return Collections.emptySet();

        return ioGraph.outgoingEdgesOf(neighbor)
                 .stream()
                 .map(ioGraph::getEdgeTarget)
                 .collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public Optional<Set<CompoundInstance>> getResultingValue()
    {
        return Optional.empty();
    }

    @Override
    public void addCandidateResult(final INode neighbor, final Set<CompoundInstance> instances)
    {
        throw new NotImplementedException("Use the edge aware version to set the candidate result on this node type.");
    }

    @NotNull
    @Override
    public Set<Set<CompoundInstance>> getCandidates()
    {
        throw new NotImplementedException("Inner graph nodes do not track the value of the candidates.");
    }

    @NotNull
    @Override
    public Set<INode> getAnalyzedNeighbors()
    {
        return ioGraph.edgeSet()
          .stream()
          .filter(edge -> !ioGraph.getEdgeTarget(edge).getAnalyzedNeighbors().isEmpty())
          .map(ioGraph::getEdgeSource)
          .collect(Collectors.toSet());
    }

    @Override
    public void onReached(final Graph<INode, IEdge> graph)
    {

    }

    @Override
    public void collectStats(final StatCollector statCollector)
    {
        statCollector.onInnerGraphNode();
    }

    @Override
    public void forceSetResult(final Set<CompoundInstance> compoundInstances)
    {
        //Noop
    }

    @Override
    public void determineResult(final Graph<INode, IEdge> graph)
    {

    }

    @Override
    public void setIncomplete()
    {
        innerGraph.vertexSet().forEach(INode::setIncomplete);
    }

    @Override
    public boolean isIncomplete()
    {
        return innerGraph.vertexSet().stream().anyMatch(INode::isIncomplete);
    }



/*
    public InnerGraphNode(
      Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> graph,
      List<IAnalysisGraphNode<Set<CompoundInstance>>> vertices
    )
    {
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
                       .peek(s -> neighborsOutgoing.put(n, s))
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
        {
            return;
        }

        fullCandidatesSet.add(instances);
        neighborCandidateValues.computeIfAbsent(neighbor, n -> Sets.newHashSet()).add(instances);
        incomingNeighbors.get(neighbor).forEach(innerNode -> innerNode.addCandidateResult(neighbor, instances));
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
            final Collection<IAnalysisGraphNode<Set<CompoundInstance>>> innerSources = outgoingNeighbors.get(v);

            for (final IAnalysisGraphNode<Set<CompoundInstance>> innerSource : innerSources)
            {
                v.addCandidateResult(this, innerSource.getResultingValue().orElse(Collections.emptySet()));
            }

            if (this.isIncomplete())
            {
                v.setIncomplete();
            }
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
                final Collection<IAnalysisGraphNode<Set<CompoundInstance>>> candidateStartNodesForNeighbor =
                  incomingNeighbors.get(n);
                candidateStartNodesForNeighbor.forEach(startNode -> {
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

                    //Run inner analysis
                    final AnalysisBFSGraphIterator<Set<CompoundInstance>> iterator = new AnalysisBFSGraphIterator<>(innerGraph, startNode);
                    final StatCollector innerStatCollector = new StatCollector("Inner node analysis.", innerGraph.vertexSet().size());
                    while (iterator.hasNext())
                    {
                        iterator.next().collectStats(innerStatCollector);
                    }

                    //Re-add the original edges that where removed.
                    sourceMap.forEach((edge, source) -> innerGraph.addEdge(source, startNode, edge));
                });
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
        if (incomingNeighbors.containsKey(originalNeighbor))
        {
            final Collection<IAnalysisGraphNode<Set<CompoundInstance>>> currentSources = incomingNeighbors.removeAll(originalNeighbor);
            incomingNeighbors.putAll(newNeighbor, currentSources);

            currentSources.forEach(inSource -> {
                if (neighborsIncoming.containsKey(inSource))
                {
                    neighborsIncoming.remove(inSource, originalNeighbor);
                    neighborsIncoming.put(inSource, newNeighbor);
                }
            });
        }


        if (outgoingNeighbors.containsKey(originalNeighbor))
        {
            final Collection<IAnalysisGraphNode<Set<CompoundInstance>>> currentTargets = outgoingNeighbors.removeAll(originalNeighbor);
            outgoingNeighbors.putAll(newNeighbor, currentTargets);

            currentTargets.forEach(outTarget -> {
                if (neighborsOutgoing.containsKey(outTarget))
                {
                    neighborsOutgoing.remove(outTarget, originalNeighbor);
                    neighborsOutgoing.put(outTarget, newNeighbor);
                }
            });
        }
    }

    @Override
    public Optional<ICompoundContainer<?>> getWrapper()
    {
        return Optional.empty();
    }

    @Override
    public Set<ICompoundContainer<?>> getTargetedWrapper(final IAnalysisGraphNode<Set<CompoundInstance>> sourceNeighbor)
    {
        if (!incomingNeighbors.containsKey(sourceNeighbor))
        {
            return Collections.emptySet();
        }

        return incomingNeighbors.get(sourceNeighbor)
          .stream()
          .filter(Objects::nonNull)
          .filter(IAnalysisNodeWithContainer.class::isInstance)
          .flatMap(n -> ((IAnalysisNodeWithContainer<Set<CompoundInstance>>) n).getTargetedWrapper(sourceNeighbor).stream())
          .collect(Collectors.toSet());
    }

    @Override
    public Set<ICompoundContainer<?>> getSourcedWrapper(final IAnalysisGraphNode<Set<CompoundInstance>> targetNeighbor)
    {
        if (!outgoingNeighbors.containsKey(targetNeighbor))
        {
            return Collections.emptySet();
        }

        return outgoingNeighbors.get(targetNeighbor)
                 .stream()
                 .filter(Objects::nonNull)
                 .filter(IAnalysisNodeWithContainer.class::isInstance)
                 .flatMap(n -> ((IAnalysisNodeWithContainer<Set<CompoundInstance>>) n).getSourcedWrapper(targetNeighbor).stream())
                 .collect(Collectors.toSet());
    }

    @Override
    public String toString()
    {
        return "SubCycleGraphNode";
    }

    @Override
    public Set<IAnalysisGraphNode<Set<CompoundInstance>>> getInnerNodes()
    {
        return innerGraph.vertexSet();
    }

    @Override
    public Set<IAnalysisGraphNode<Set<CompoundInstance>>> getSourceNeighborOf(final IAnalysisGraphNode<Set<CompoundInstance>> neighbor)
    {
        return Sets.newHashSet(outgoingNeighbors.get(neighbor));
    }

    @Override
    public Set<IAnalysisGraphNode<Set<CompoundInstance>>> getTargetNeighborOf(final IAnalysisGraphNode<Set<CompoundInstance>> neighbor)
    {
        return Sets.newHashSet(incomingNeighbors.get(neighbor));
    }*/
}
