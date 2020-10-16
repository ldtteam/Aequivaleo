package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.analyzer.StatCollector;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.*;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.Edge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.graph.AequivaleoGraph;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
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

    private final IGraph ioGraph    = new AequivaleoGraph();
    private final IGraph innerGraph = new AequivaleoGraph();

    private final Set<Set<CompoundInstance>> candidates = Sets.newHashSet();

    public InnerGraphNode(
      final IGraph sourceGraph,
      final List<INode> innerVertices
    )
    {
        setupGraphs(sourceGraph, innerVertices);
    }

    @Override
    public IGraph getIOGraph(final IGraph graph)
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
        {
            return Collections.emptySet();
        }

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
        {
            return Collections.emptySet();
        }

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
        {
            return Collections.emptySet();
        }

        return ioGraph.incomingEdgesOf(neighbor)
                 .stream()
                 .map(ioGraph::getEdgeSource)
                 .collect(Collectors.toSet());
    }

    @Override
    public Set<INode> getTargetNeighborOf(final INode neighbor)
    {
        if (!ioGraph.containsVertex(neighbor))
        {
            return Collections.emptySet();
        }

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
        throw new UnsupportedOperationException("Use the edge aware version to set the candidate result on this node type.");
    }

    @Override
    public void addCandidateResult(final INode neighbor, final IEdge sourceEdge, final Set<CompoundInstance> instances)
    {
        if (!ioGraph.containsVertex(neighbor))
            return;

        ioGraph.outgoingEdgesOf(neighbor)
          .stream()
          .filter(ioEdge -> ioEdge.getEdgeIdentifier() == sourceEdge.getEdgeIdentifier())
          .map(ioGraph::getEdgeTarget)
          .findFirst()
          .ifPresent(node -> {
              node.addCandidateResult(neighbor, sourceEdge, instances);
              candidates.add(instances);
          });
    }

    @NotNull
    @Override
    public Set<Set<CompoundInstance>> getCandidates()
    {
        return ImmutableSet.copyOf(candidates);
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
    public void onReached(final IGraph graph)
    {
        final boolean inComplete = isIncomplete();

        ioGraph.edgeSet()
          .stream()
          .filter(edge -> !innerGraph.containsVertex(ioGraph.getEdgeTarget(edge)))
          .forEach(edge -> {
              final INode source = ioGraph.getEdgeSource(edge);
              final INode target = ioGraph.getEdgeTarget(edge);

              target.addCandidateResult(this, source.getResultingValue().orElse(Collections.emptySet()));
              if (inComplete)
                  target.setIncomplete();
          });
    }

    @Override
    public void collectStats(final StatCollector statCollector)
    {
        statCollector.onInnerGraphNode();
    }

    @Override
    public void forceSetResult(final Set<CompoundInstance> compoundInstances)
    {
        innerGraph.vertexSet().forEach(node -> node.forceSetResult(compoundInstances));
    }

    @Override
    public void determineResult(final IGraph graph)
    {

        final Set<INode> startingNodes = innerGraph.vertexSet().stream().filter(node -> !node.getCandidates().isEmpty()).collect(Collectors.toSet());
        for (final INode startingNode : startingNodes)
        {

        }

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
        innerGraph.vertexSet().forEach(INode::setIncomplete);
    }

    @Override
    public boolean isIncomplete()
    {
        return innerGraph.vertexSet().stream().anyMatch(INode::isIncomplete);
    }

    private void setupGraphs(final IGraph graph, final List<INode> innerVertices) {
        setupInnerGraph(graph, innerVertices);
        setupIOGraph(graph, innerVertices);
    }

    private void setupInnerGraph(final IGraph graph, final List<INode> innerVertices)
    {
        innerVertices.forEach(innerGraph::addVertex);
        innerVertices.stream()
          .map(graph::outgoingEdgesOf)
          .flatMap(Collection::stream)
          .filter(e -> innerVertices.contains(graph.getEdgeTarget(e)))
          .peek(e -> innerGraph.addEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e), new Edge(e.getEdgeIdentifier())))
          .forEach(e -> innerGraph.setEdgeWeight(graph.getEdgeSource(e), graph.getEdgeTarget(e), graph.getEdgeWeight(e)));
    }

    private void setupIOGraph(final IGraph graph, final List<INode> innerVertices)
    {
        innerVertices.forEach(node -> {
            if (!ioGraph.containsVertex(node))
                ioGraph.addVertex(node);

            graph.incomingEdgesOf(node)
              .stream()
              .filter(edge -> !innerVertices.contains(graph.getEdgeSource(edge)))
              .peek(edge -> {
                  if (!ioGraph.containsVertex(graph.getEdgeSource(edge)))
                      ioGraph.addVertex(graph.getEdgeSource(edge));
              })
              .peek(edge -> ioGraph.addEdge(graph.getEdgeSource(edge), node, new Edge(edge.getEdgeIdentifier())))
              .forEach(edge -> ioGraph.setEdgeWeight(graph.getEdgeSource(edge), node, edge.getWeight()));

            graph.outgoingEdgesOf(node)
              .stream()
              .filter(edge -> !innerVertices.contains(graph.getEdgeTarget(edge)))
              .peek(edge -> {
                  if (!ioGraph.containsVertex(graph.getEdgeTarget(edge)))
                      ioGraph.addVertex(graph.getEdgeTarget(edge));
              })
              .peek(edge -> ioGraph.addEdge(node, graph.getEdgeTarget(edge), new Edge(edge.getEdgeIdentifier())))
              .forEach(edge -> ioGraph.setEdgeWeight(node, graph.getEdgeTarget(edge), edge.getWeight()));
        });
    }

/*
    @Override
    public void determineResult(final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> graph)
    {

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
