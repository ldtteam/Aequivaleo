package com.ldtteam.aequivaleo.analyzer.jgrapht.node;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
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
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("SuspiciousMethodCalls")
public class CliqueNode
  implements IInnerNode, IContainerNode, IIOAwareNode, IRecipeInputNode, IRecipeResidueNode, IRecipeOutputNode
{
    private static final Logger LOGGER = LogManager.getLogger();

    private Set<CompoundInstance> finalResult = null;
    private final IGraph ioGraph    = new AequivaleoGraph();
    private final Set<IContainerNode> innerCliqueNodes = Sets.newHashSet();
    private       int    hash;

    private final Multimap<INode, Set<CompoundInstance>> candidates = ArrayListMultimap.create();

    public CliqueNode(
      final IGraph sourceGraph,
      final Set<INode> innerVertices
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
        return ImmutableSet.copyOf(innerCliqueNodes);
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
        return Optional.ofNullable(finalResult);
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
          .filter(edge -> !innerCliqueNodes.contains(ioGraph.getEdgeTarget(edge)))
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
        innerCliqueNodes.forEach(node -> node.forceSetResult(compoundInstances));
    }

    @Override
    public void determineResult(final IGraph graph)
    {
        final Set<INode> startingNodes = innerCliqueNodes.stream().filter(node -> !node.getCandidates().isEmpty()).collect(Collectors.toSet());
        final Set<Set<CompoundInstance>> candidates = startingNodes
          .stream()
          .peek(node -> node.determineResult(graph))
          .map(s -> s.getResultingValue().orElse(Sets.newHashSet()))
          .collect(Collectors.toSet());

        Set<CompoundInstance> result = getResultingValue().orElse(null);

        //Short cirquit empty result.
        if (candidates.size() == 0)
        {
            if (result != null)
            {
                AnalysisLogHandler.debug(LOGGER, String.format("  > No candidates available. Using current value: %s", result));
            }
            else
            {
                AnalysisLogHandler.debug(LOGGER, "  > No candidates available, and result not forced. Setting empty collection!");
                result = Collections.emptySet();
                finalResult = result;
                innerCliqueNodes.forEach(node -> node.forceSetResult(finalResult));
            }
            return;
        }

        //If we have only one other data set we have nothing to choose from.
        //So we take that.
        if (candidates.size() == 1)
        {
            result = candidates.iterator().next();
            finalResult = result;
            innerCliqueNodes.forEach(node -> node.forceSetResult(finalResult));
            AnalysisLogHandler.debug(LOGGER, String.format("  > Candidate data contained exactly one entry: %s", result));
            return;
        }

        AnalysisLogHandler.debug(LOGGER, "  > Candidate data contains more then one entry. Mediation is required. Invoking type group callbacks to determine value.");
        //If we have multiples we group them up by type group and then let it decide.
        //Then we collect them all back together into one list
        //Bit of a mess but works.
        result = GroupingUtils.groupByUsingSet(candidates
                                                      .stream()
                                                      .flatMap(candidate -> GroupingUtils.groupByUsingSet(candidate, compoundInstance -> compoundInstance.getType().getGroup()).stream()) //Split apart each of the initial candidate lists into several smaller list based on their group.
                                                      .filter(collection -> !collection.isEmpty())
                                                      .map(Sets::newHashSet)
                                                      .map(hs -> (Set<CompoundInstance>) hs)
                                                      .collect(Collectors.toList()), compoundInstances -> compoundInstances.iterator().next().getType().getGroup()) //Group each of the list again on their group, so that all candidates with the same group are together.
                        .stream()
                        .map(Sets::newHashSet)
                        .filter(s -> !s.isEmpty())
                        .map(s -> s.iterator().next().iterator().next().getType().getGroup().determineResult(s, canResultBeCalculated(graph))) //For each type invoke the determination routine.
                        .collect(Collectors.toSet()) //
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet()); //Group all of them together.

        AnalysisLogHandler.debug(LOGGER, String.format("  > Mediation completed. Determined value is: %s", result));

        finalResult = result;
        innerCliqueNodes.forEach(node -> node.forceSetResult(finalResult));
    }

    @Override
    public void setIncomplete()
    {
        innerCliqueNodes.forEach(INode::setIncomplete);
    }

    @Override
    public boolean isIncomplete()
    {
        return innerCliqueNodes.stream().anyMatch(INode::isIncomplete);
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

    private void setupGraphs(final IGraph graph, final Set<INode> innerVertices)
    {
        setupInnerGraph(graph, innerVertices);
        setupIOGraph(graph, innerVertices);

        this.hash = Objects.hash(ioGraph, innerCliqueNodes);
    }

    private void setupInnerGraph(final IGraph graph, final Set<INode> innerVertices)
    {
        this.innerCliqueNodes.addAll(
          innerVertices.stream().filter(IContainerNode.class::isInstance).map(IContainerNode.class::cast).collect(Collectors.toSet())
        );
    }

    private void setupIOGraph(final IGraph graph, final Set<INode> innerVertices)
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
            if (innerCliqueNodes.contains(node)) {
                ioGraph.incomingEdgesOf(node)
                  .stream()
                  .map(ioGraph::getEdgeSource)
                  .filter(innerCliqueNodes::contains)
                  .findAny()
                  .ifPresent(illegalNode -> {
                      throw new IllegalStateException("The build IO Graph contains a inner edge, which is illegal. Between: " + node + " and: " + illegalNode);
                  });

                ioGraph.outgoingEdgesOf(node)
                  .stream()
                  .map(ioGraph::getEdgeTarget)
                  .filter(innerCliqueNodes::contains)
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
        return "CliqueNode@" + Integer.toHexString(hashCode());
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
        final CliqueNode that = (CliqueNode) o;
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
