package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.analyzer.StatCollector;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.*;
import com.ldtteam.aequivaleo.analyzer.jgrapht.cycles.JGraphTCyclesReducer;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.Edge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.graph.AequivaleoGraph;
import com.ldtteam.aequivaleo.analyzer.jgrapht.iterator.AnalysisBFSGraphIterator;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.util.CompoundInstanceCollectors;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InnerNode
  implements IInnerNode, IContainerNode, IIOAwareNode, IRecipeInputNode, IRecipeResidueNode, IRecipeOutputNode
{
    private static final Logger LOGGER = LogManager.getLogger();

    private final IGraph ioGraph    = new AequivaleoGraph();
    private final IGraph innerGraph = new AequivaleoGraph();
    private       int    hash;

    private final Multimap<INode, Set<CompoundInstance>> candidates = ArrayListMultimap.create();

    public InnerNode(
      final IGraph sourceGraph,
      final List<INode> innerVertices
    )
    {
        setupGraphs(sourceGraph, innerVertices);
        AnalysisLogHandler.debug(LOGGER, String.format("Created inner graph node: %s", toString()));
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
    public void addCandidateResult(final INode neighbor, final IEdge sourceEdge, final Set<CompoundInstance> instances)
    {
        if (!ioGraph.containsVertex(neighbor))
        {
            return;
        }

        final double totalOutgoingEdgeWeight = sourceEdge.getWeight();

        ioGraph.outgoingEdgesOf(neighbor)
          .stream()
          .map(ioGraph::getEdgeTarget)
          .findFirst()
          .ifPresent(node -> {
              final Set<CompoundInstance> workingSet = instances
                                                         .stream()
                                                         .map(ci -> new CompoundInstance(ci.getType(),
                                                           ci.getAmount() * (ioGraph.getEdgeWeight(ioGraph.getEdge(neighbor, node)) / totalOutgoingEdgeWeight)))
                                                         .collect(Collectors.toSet());
              node.addCandidateResult(neighbor, sourceEdge, workingSet);
              candidates.put(neighbor, instances);
          });
    }

    @NotNull
    @Override
    public Set<Set<CompoundInstance>> getCandidates()
    {
        return new HashSet<>(candidates.values());
    }

    @NotNull
    @Override
    public Set<INode> getAnalyzedNeighbors()
    {
        return candidates.keySet();
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

              target.addCandidateResult(this, graph.getEdge(this, target), source.getResultingValue().orElse(Collections.emptySet()));
              if (inComplete)
              {
                  target.setIncomplete();
              }
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
        final Map<INode, Integer> nodeCandidateCounts = startingNodes.stream().collect(Collectors.toMap(
          Function.identity(),
          n -> n.getCandidates().size()
          )
        );
        for (final INode startNode : startingNodes)
        {
            //NOTE: Due to the way our system works, every node will have only one incoming edge!
            final Set<IEdge> incomingEdges = Sets.newHashSet(innerGraph.incomingEdgesOf(startNode));
            final Map<IEdge, INode> sourceMap =
              incomingEdges.stream().collect(Collectors.toMap(Function.identity(), innerGraph::getEdgeSource));

            //We remove the inner edge of all edge
            for (IEdge incomingEdge : incomingEdges)
            {
                innerGraph.removeEdge(incomingEdge);
            }

            //Run inner analysis
            final AnalysisBFSGraphIterator iterator = new AnalysisBFSGraphIterator(innerGraph, startNode);
            final StatCollector innerStatCollector = new StatCollector("Inner node analysis.", innerGraph.vertexSet().size())
            {

                @Override
                protected void logState()
                {
                    //Noop
                }
            };
            while (iterator.hasNext())
            {
                iterator.next().collectStats(innerStatCollector);
            }

            //Re-add the original edges that where removed.
            sourceMap.forEach((edge, source) -> innerGraph.addEdge(source, startNode, edge));

            if (startingNodes.stream().allMatch(node -> node.getCandidates().size() == nodeCandidateCounts.get(node)))
            {
                //The loop ran over every node in the network
                //Since we still have the same candidates on all nodes,
                //We can short circuit the remaining node calculations.
                break;
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

    @Override
    public void onNeighborReplaced(final INode originalNeighbor, final INode newNeighbor)
    {
        if (ioGraph.containsVertex(originalNeighbor))
        {
            AnalysisLogHandler.debug(LOGGER, "Updating neighbor data from: " + originalNeighbor + " to: " + newNeighbor);
            ioGraph.addVertex(newNeighbor);
            for (IEdge edge : ioGraph.outgoingEdgesOf(originalNeighbor))
            {
                ioGraph.addEdge(newNeighbor, ioGraph.getEdgeTarget(edge));
                ioGraph.setEdgeWeight(newNeighbor, ioGraph.getEdgeTarget(edge), ioGraph.getEdgeWeight(edge));
            }
            for (IEdge edge : ioGraph.incomingEdgesOf(originalNeighbor))
            {
                ioGraph.addEdge(ioGraph.getEdgeSource(edge), newNeighbor, edge);
                ioGraph.setEdgeWeight(ioGraph.getEdgeSource(edge), newNeighbor, ioGraph.getEdgeWeight(edge));
            }
            ioGraph.removeVertex(originalNeighbor);

            validateIOGraph();
        }
    }

    private void setupGraphs(final IGraph graph, final List<INode> innerVertices)
    {
        setupInnerGraph(graph, innerVertices);
        setupIOGraph(graph, innerVertices);

        this.hash = Objects.hash(ioGraph, innerGraph);
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

        final JGraphTCyclesReducer<IGraph, INode, IEdge> cyclesReducer = new JGraphTCyclesReducer<>(
          InnerNode::new,
          INode::onNeighborReplaced,
          false);

        cyclesReducer.reduce(innerGraph);
    }

    private void setupIOGraph(final IGraph graph, final List<INode> innerVertices)
    {
        innerVertices.forEach(node -> {
            graph.incomingEdgesOf(node)
              .stream()
              .filter(edge -> !innerVertices.contains(graph.getEdgeSource(edge)))
              .peek(edge -> {
                  if (!ioGraph.containsVertex(graph.getEdgeSource(edge)))
                  {
                      ioGraph.addVertex(graph.getEdgeSource(edge));
                  }

                  if (!ioGraph.containsVertex(graph.getEdgeTarget(edge)))
                  {
                      ioGraph.addVertex(graph.getEdgeTarget(edge));
                  }
              })
              .peek(edge -> ioGraph.addEdge(graph.getEdgeSource(edge), node, new Edge(edge.getEdgeIdentifier())))
              .forEach(edge -> ioGraph.setEdgeWeight(graph.getEdgeSource(edge), node, edge.getWeight()));

            graph.outgoingEdgesOf(node)
              .stream()
              .filter(edge -> !innerVertices.contains(graph.getEdgeTarget(edge)))
              .peek(edge -> {
                  if (!ioGraph.containsVertex(graph.getEdgeSource(edge)))
                  {
                      ioGraph.addVertex(graph.getEdgeSource(edge));
                  }

                  if (!ioGraph.containsVertex(graph.getEdgeTarget(edge)))
                  {
                      ioGraph.addVertex(graph.getEdgeTarget(edge));
                  }
              })
              .peek(edge -> ioGraph.addEdge(node, graph.getEdgeTarget(edge), new Edge(edge.getEdgeIdentifier())))
              .forEach(edge -> ioGraph.setEdgeWeight(node, graph.getEdgeTarget(edge), edge.getWeight()));
        });

        validateIOGraph();
    }

    private void validateIOGraph() {
        ioGraph.vertexSet().forEach(node -> {
            if (innerGraph.containsVertex(node)) {
                ioGraph.incomingEdgesOf(node)
                  .stream()
                  .map(ioGraph::getEdgeSource)
                  .filter(innerGraph::containsVertex)
                  .findAny()
                  .ifPresent(illegalNode -> {
                      throw new IllegalStateException("The build IO Graph contains a inner edge, which is illegal. Between: " + node + " and: " + illegalNode);
                  });

                ioGraph.outgoingEdgesOf(node)
                  .stream()
                  .map(ioGraph::getEdgeTarget)
                  .filter(innerGraph::containsVertex)
                  .findAny()
                  .ifPresent(illegalNode -> {
                      throw new IllegalStateException("The build IO Graph contains a inner edge, which is illegal. Between: " + node + " and: " + illegalNode);
                  });
            }
        });
    }

    @Override
    public String toString()
    {
        return "InnerNode@" + Integer.toHexString(hashCode());
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final InnerNode that = (InnerNode) o;
        return hash == that.hash;
    }

    @Override
    public int hashCode()
    {
        return hash;
    }

    @Override
    public Set<CompoundInstance> getInputInstances(final IRecipeNode recipeNode)
    {
        if (!ioGraph.containsVertex(recipeNode))
        {
            return Collections.emptySet();
        }

        return ioGraph.incomingEdgesOf(recipeNode)
                 .stream()
                 .map(ioGraph::getEdgeSource)
                 .filter(IRecipeInputNode.class::isInstance)
                 .map(IRecipeInputNode.class::cast)
                 .map(n -> n.getInputInstances(recipeNode))
                 .flatMap(Set::stream)
                 .collect(CompoundInstanceCollectors.reduceToSet());
    }

    @Override
    public Set<CompoundInstance> getResidueInstances(final IRecipeNode recipeNode)
    {
        if (!ioGraph.containsVertex(recipeNode))
        {
            return Collections.emptySet();
        }

        return ioGraph.incomingEdgesOf(recipeNode)
                 .stream()
                 .map(ioGraph::getEdgeSource)
                 .filter(IRecipeResidueNode.class::isInstance)
                 .map(IRecipeResidueNode.class::cast)
                 .map(n -> n.getResidueInstances(recipeNode))
                 .flatMap(Set::stream)
                 .collect(CompoundInstanceCollectors.reduceToSet());
    }
}
