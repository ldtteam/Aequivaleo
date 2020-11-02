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
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import com.ldtteam.aequivaleo.utils.GraphUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class InnerNode
  implements IInnerNode, IContainerNode, IIOAwareNode, IRecipeInputNode, IRecipeResidueNode, IRecipeOutputNode, INodeWithoutResult, IStartAnalysisNode
{
    private static final Logger LOGGER = LogManager.getLogger();

    private final IGraph ioGraph    = new AequivaleoGraph();
    private final IGraph innerGraph = new AequivaleoGraph();
    private       int    hash;

    private final Multimap<INode, Optional<Set<CompoundInstance>>> candidates = ArrayListMultimap.create();

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

        Set<ICompoundContainer<?>> set = new HashSet<>();
        for (IEdge iEdge : ioGraph.outgoingEdgesOf(sourceNeighbor))
        {
            INode edgeTarget = ioGraph.getEdgeTarget(iEdge);
            if (edgeTarget instanceof IContainerNode)
            {
                IContainerNode cn = (IContainerNode) edgeTarget;
                set.addAll(cn.getTargetedWrapper(sourceNeighbor));
            }
        }
        return set;
    }

    @Override
    public Set<ICompoundContainer<?>> getSourcedWrapper(final INode targetNeighbor)
    {
        if (!ioGraph.containsVertex(targetNeighbor))
        {
            return Collections.emptySet();
        }

        Set<ICompoundContainer<?>> set = new HashSet<>();
        for (IEdge iEdge : ioGraph.incomingEdgesOf(targetNeighbor))
        {
            INode edgeSource = ioGraph.getEdgeSource(iEdge);
            if (edgeSource instanceof IContainerNode)
            {
                IContainerNode cn = (IContainerNode) edgeSource;
                set.addAll(cn.getSourcedWrapper(targetNeighbor));
            }
        }
        return set;
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

        Set<INode> set = new HashSet<>();
        for (IEdge iEdge : ioGraph.outgoingEdgesOf(neighbor))
        {
            INode edgeTarget = ioGraph.getEdgeTarget(iEdge);
            set.add(edgeTarget);
        }
        return set;
    }

    @Override
    public void addCandidateResult(final INode neighbor, final IEdge sourceEdge, final Optional<Set<CompoundInstance>> instances)
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
              final Optional<Set<CompoundInstance>> workingSet = instances.map(innerInstances -> {
                  Set<CompoundInstance> set = new HashSet<>();
                  for (CompoundInstance ci : innerInstances)
                  {
                      CompoundInstance instance = new CompoundInstance(ci.getType(),
                        ci.getAmount() * (ioGraph.getEdgeWeight(ioGraph.getEdge(neighbor, node)) / totalOutgoingEdgeWeight));
                      set.add(instance);
                  }
                  return set;
              });
              node.addCandidateResult(neighbor, sourceEdge, workingSet);
              candidates.put(neighbor, instances);
          });
    }

    @NotNull
    @Override
    public Set<Set<CompoundInstance>> getCandidates()
    {
        return candidates.values().stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
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
        for (IEdge edge : ioGraph.edgeSet())
        {
            if (!innerGraph.containsVertex(ioGraph.getEdgeTarget(edge)))
            {
                final INode source = ioGraph.getEdgeSource(edge);
                final INode target = ioGraph.getEdgeTarget(edge);

                target.addCandidateResult(this, graph.getEdge(this, target), source.getResultingValue());
            }
        }
    }

    @Override
    public void collectStats(final StatCollector statCollector)
    {
        statCollector.onInnerGraphNode();
    }

    @Override
    public void forceSetResult(final Set<CompoundInstance> compoundInstances)
    {
        for (INode node : innerGraph.vertexSet())
        {
            node.forceSetResult(compoundInstances);
        }
    }

    @Override
    public void determineResult(final IGraph graph)
    {
        final Set<INode> startingNodes = new HashSet<>();
        for (INode node : innerGraph.vertexSet())
        {
            if (!node.getCandidates().isEmpty() && node instanceof IStartAnalysisNode)
            {
                startingNodes.add(node);
            }
        }
        final Map<INode, Integer> nodeCandidateCounts = new HashMap<>();
        for (INode startNode : startingNodes)
        {
            if (nodeCandidateCounts.put(startNode, startNode.getCandidates().size()) != null)
            {
                throw new IllegalStateException("Duplicate node");
            }
        }

        final IGraph workingGraph = GraphUtils.mergeGraphs(this.innerGraph, this.ioGraph);

        for (final INode startNode : startingNodes)
        {
            //NOTE: Due to the way our system works, every node will have only one incoming edge!
            final Set<IEdge> workingGraphIncomingEdges = Sets.newHashSet(workingGraph.incomingEdgesOf(startNode));
            final Map<IEdge, INode> workingGraphSourceMap =
              new HashMap<>();
            final Set<IEdge> innerGraphIncomingEdges = Sets.newHashSet(innerGraph.incomingEdgesOf(startNode));
            final Map<IEdge, INode> innerGraphSourceMap =
              new HashMap<>();
            for (IEdge edge : workingGraphIncomingEdges)
            {
                if (workingGraphSourceMap.put(edge, workingGraph.getEdgeSource(edge)) != null)
                {
                    throw new IllegalStateException("Duplicate edge.");
                }
            }
            for (IEdge edge : innerGraphIncomingEdges)
            {
                if (innerGraphSourceMap.put(edge, innerGraph.getEdgeSource(edge)) != null)
                {
                    throw new IllegalStateException("Duplicate edge.");
                }
            }

            //We remove the inner edge of all edge
            for (IEdge incomingEdge : workingGraphIncomingEdges)
            {
                workingGraph.removeEdge(incomingEdge);
            }
            for (IEdge incomingEdge : innerGraphIncomingEdges)
            {
                innerGraph.removeEdge(incomingEdge);
            }

            //Run inner analysis
            final AnalysisBFSGraphIterator iterator = new AnalysisBFSGraphIterator(innerGraph, startNode, workingGraph);
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
            for (Map.Entry<IEdge, INode> entry : workingGraphSourceMap.entrySet())
            {
                IEdge edge = entry.getKey();
                INode source = entry.getValue();
                workingGraph.addEdge(source, startNode, edge);
            }
            for (Map.Entry<IEdge, INode> entry : innerGraphSourceMap.entrySet())
            {
                IEdge edge = entry.getKey();
                INode source = entry.getValue();
                innerGraph.addEdge(source, startNode, edge);
            }

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
        for (INode iNode : innerVertices)
        {
            innerGraph.addVertex(iNode);
        }
        for (INode innerVertex : innerVertices)
        {
            Set<IEdge> iEdges = graph.outgoingEdgesOf(innerVertex);
            for (IEdge e : iEdges)
            {
                if (innerVertices.contains(graph.getEdgeTarget(e)))
                {
                    innerGraph.addEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e), new Edge());
                    innerGraph.setEdgeWeight(graph.getEdgeSource(e), graph.getEdgeTarget(e), graph.getEdgeWeight(e));
                }
            }
        }

        final JGraphTCyclesReducer<IGraph, INode, IEdge> cyclesReducer = new JGraphTCyclesReducer<>(
          InnerNode::new,
          INode::onNeighborReplaced,
          false);

        cyclesReducer.reduce(innerGraph);
    }

    private void setupIOGraph(final IGraph graph, final List<INode> innerVertices)
    {
        for (INode node : innerVertices)
        {
            for (IEdge iEdge : graph.incomingEdgesOf(node))
            {
                if (!innerVertices.contains(graph.getEdgeSource(iEdge)))
                {
                    if (!ioGraph.containsVertex(graph.getEdgeSource(iEdge)))
                    {
                        ioGraph.addVertex(graph.getEdgeSource(iEdge));
                    }

                    if (!ioGraph.containsVertex(graph.getEdgeTarget(iEdge)))
                    {
                        ioGraph.addVertex(graph.getEdgeTarget(iEdge));
                    }
                    ioGraph.addEdge(graph.getEdgeSource(iEdge), node, new Edge());
                    ioGraph.setEdgeWeight(graph.getEdgeSource(iEdge), node, iEdge.getWeight());
                }
            }

            for (IEdge edge : graph.outgoingEdgesOf(node))
            {
                if (!innerVertices.contains(graph.getEdgeTarget(edge)))
                {
                    if (!ioGraph.containsVertex(graph.getEdgeSource(edge)))
                    {
                        ioGraph.addVertex(graph.getEdgeSource(edge));
                    }

                    if (!ioGraph.containsVertex(graph.getEdgeTarget(edge)))
                    {
                        ioGraph.addVertex(graph.getEdgeTarget(edge));
                    }
                    ioGraph.addEdge(node, graph.getEdgeTarget(edge), new Edge());
                    ioGraph.setEdgeWeight(node, graph.getEdgeTarget(edge), edge.getWeight());
                }
            }
        }

        validateIOGraph();
    }

    private void validateIOGraph() {
        for (INode node : ioGraph.vertexSet())
        {
            if (innerGraph.containsVertex(node))
            {
                Optional<INode> found = Optional.empty();
                for (IEdge iEdge : ioGraph.incomingEdgesOf(node))
                {
                    INode edgeSource = ioGraph.getEdgeSource(iEdge);
                    if (innerGraph.containsVertex(edgeSource))
                    {
                        found = Optional.of(edgeSource);
                        break;
                    }
                }
                found
                  .ifPresent(illegalNode -> {
                      throw new IllegalStateException("The build IO Graph contains a inner edge, which is illegal. Between: " + node + " and: " + illegalNode);
                  });

                Optional<INode> result = Optional.empty();
                for (IEdge iEdge : ioGraph.outgoingEdgesOf(node))
                {
                    INode edgeTarget = ioGraph.getEdgeTarget(iEdge);
                    if (innerGraph.containsVertex(edgeTarget))
                    {
                        result = Optional.of(edgeTarget);
                        break;
                    }
                }
                result
                  .ifPresent(illegalNode -> {
                      throw new IllegalStateException("The build IO Graph contains a inner edge, which is illegal. Between: " + node + " and: " + illegalNode);
                  });
            }
        }
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

        final Set<CompoundInstance> result = Sets.newHashSet();
        for (final IEdge iEdge : ioGraph.incomingEdgesOf(recipeNode))
        {
            final INode source = ioGraph.getEdgeSource(iEdge);
            if (source instanceof IRecipeInputNode)
            {
                final IRecipeInputNode residueNode = (IRecipeInputNode) source;
                result.addAll(residueNode.getInputInstances(recipeNode));
            }
        }

        Set<CompoundInstance> compoundedResult = new HashSet<>();
        for (Collection<CompoundInstance> sameType : GroupingUtils.groupByUsingList(result, CompoundInstance::getType))
        {
            if (!sameType.isEmpty())
            {
                CompoundInstance instance = new CompoundInstance(sameType.iterator().next().getType(), sameType.stream().mapToDouble(CompoundInstance::getAmount).sum());
                compoundedResult.add(instance);
            }
        }
        return compoundedResult;
    }


    @Override
    public Set<IRecipeInputNode> getInputNodes(final IRecipeNode recipeNode)
    {
        if (!ioGraph.containsVertex(recipeNode))
        {
            return Collections.emptySet();
        }

        final Set<IRecipeInputNode> result = Sets.newHashSet();
        for (final IEdge iEdge : ioGraph.incomingEdgesOf(recipeNode))
        {
            final INode source = ioGraph.getEdgeSource(iEdge);
            if (source instanceof IRecipeInputNode)
            {
                final IRecipeInputNode residueNode = (IRecipeInputNode) source;
                result.addAll(residueNode.getInputNodes(recipeNode));
            }
        }

        return result;
    }

    @Override
    public Set<CompoundInstance> getResidueInstances(final IRecipeNode recipeNode)
    {
        if (!ioGraph.containsVertex(recipeNode))
        {
            return Collections.emptySet();
        }

        final Set<CompoundInstance> result = Sets.newHashSet();
        for (final IEdge iEdge : ioGraph.incomingEdgesOf(recipeNode))
        {
            final INode source = ioGraph.getEdgeSource(iEdge);
            if (source instanceof IRecipeResidueNode)
            {
                final IRecipeResidueNode residueNode = (IRecipeResidueNode) source;
                result.addAll(residueNode.getResidueInstances(recipeNode));
            }
        }

        Set<CompoundInstance> compoundedResult = new HashSet<>();
        for (Collection<CompoundInstance> sameType : GroupingUtils.groupByUsingList(result, CompoundInstance::getType))
        {
            if (!sameType.isEmpty())
            {
                CompoundInstance instance = new CompoundInstance(sameType.iterator().next().getType(), sameType.stream().mapToDouble(CompoundInstance::getAmount).sum());
                compoundedResult.add(instance);
            }
        }
        return compoundedResult;
    }
}
