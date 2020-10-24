package com.ldtteam.aequivaleo.analyzer.jgrapht.clique;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.*;
import com.ldtteam.aequivaleo.analyzer.jgrapht.clique.graph.CliqueDetectionEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.clique.graph.CliqueDetectionGraph;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisEdge;
import com.ldtteam.aequivaleo.api.util.QuadFunction;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;
import org.jgrapht.Graph;
import org.jgrapht.alg.clique.BronKerboschCliqueFinder;
import org.jgrapht.alg.interfaces.MaximalCliqueEnumerationAlgorithm;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JGraphTCliqueReducer<G extends Graph<INode, IEdge>>
{

    private static final Logger LOGGER = LogManager.getLogger();

    private final QuadFunction<G, Set<INode>, Set<IRecipeNode>, Set<IRecipeInputNode>, INode> vertexReplacerFunction;
    private final Function<List<Set<IRecipeNode>>, Set<IRecipeNode>> cliqueRecipeExtractor;
    private final TriConsumer<INode, INode, INode>                 onNeighborNodeReplacedCallback;

    public JGraphTCliqueReducer(
      final QuadFunction<G, Set<INode>, Set<IRecipeNode>, Set<IRecipeInputNode>, INode> vertexReplacerFunction,
      final Function<List<Set<IRecipeNode>>, Set<IRecipeNode>> cliqueRecipeExtractor,
      final TriConsumer<INode, INode, INode> onNeighborNodeReplacedCallback)
    {
        this.vertexReplacerFunction = vertexReplacerFunction;
        this.cliqueRecipeExtractor = cliqueRecipeExtractor;
        this.onNeighborNodeReplacedCallback = onNeighborNodeReplacedCallback;
    }

    @SuppressWarnings({"SuspiciousMethodCalls", "DuplicatedCode"})
    public void reduce(final G graph)
    {
        final CliqueDetectionGraph detectionGraph = buildDetectionGraph(graph);

        final MaximalCliqueEnumerationAlgorithm<INode, CliqueDetectionEdge> cliqueFinder = new BronKerboschCliqueFinder<>(detectionGraph);
        final List<Set<INode>> foundCliques = Lists.newArrayList(cliqueFinder);
        foundCliques.sort(Comparator.comparing(Set::size));
        LinkedHashSet<Set<INode>> sortedCliques = new LinkedHashSet<>(foundCliques);

        while(!sortedCliques.isEmpty()) {
            final Set<INode> clique = sortedCliques.iterator().next();
            List<Set<IRecipeNode>> list = new ArrayList<>();
            for (INode cliqueEntry : clique)
            {
                for (INode secondEntry : clique)
                {
                    if (secondEntry != cliqueEntry)
                    {
                        CliqueDetectionEdge detectionGraphEdge = detectionGraph.getEdge(cliqueEntry, secondEntry);
                        if (detectionGraphEdge != null)
                        {
                            Set<IRecipeNode> recipeNodes = new HashSet<>(detectionGraphEdge.getRecipeNodes());
                            recipeNodes.removeIf(n -> !graph.containsVertex(n));

                            if (!recipeNodes.isEmpty())
                            {
                                list.add(recipeNodes);
                            }
                        }
                    }
                }
            }


            final Set<IRecipeNode> recipesToRemove = this.cliqueRecipeExtractor.apply(list);

            if (recipesToRemove.isEmpty())
            {
                sortedCliques.remove(clique);
                continue;
            }

            final Set<IRecipeInputNode> relevantInputNodes = new HashSet<>();
            for (IRecipeNode cliqueRecipeNode : recipesToRemove)
            {
                for (IEdge iEdge : graph.incomingEdgesOf(cliqueRecipeNode))
                {
                    INode edgeSource = graph.getEdgeSource(iEdge);
                    if (edgeSource instanceof IRecipeInputNode)
                    {
                        IRecipeInputNode iRecipeInputNode = (IRecipeInputNode) edgeSource;
                        relevantInputNodes.add(iRecipeInputNode);
                    }
                }
            }

            final Set<IRecipeInputNode> inputNodesToDelete = new HashSet<>();
            for (IRecipeInputNode inputNode : relevantInputNodes)
            {
                Set<INode> set = new HashSet<>();
                for (IEdge iEdge : graph.outgoingEdgesOf(inputNode))
                {
                    INode edgeTarget = graph.getEdgeTarget(iEdge);
                    set.add(edgeTarget);
                }
                if (recipesToRemove.containsAll(set))
                {
                    inputNodesToDelete.add(inputNode);
                }
            }

            final INode replacementNode = vertexReplacerFunction.apply(
              graph,
              clique,
              recipesToRemove,
              inputNodesToDelete
            );

            sortedCliques = updateRemainingCliquesAfterReplacement(
              sortedCliques,
              clique,
              replacementNode
            );

            final Map<IEdge, INode> incomingEdges = Maps.newHashMap();
            final Map<IEdge, INode> outgoingEdges = Maps.newHashMap();
            final Multimap<INode, IEdge> incomingEdgesTo = HashMultimap.create();
            final Multimap<INode, IEdge> incomingEdgesOf = HashMultimap.create();
            final Multimap<INode, IEdge> outgoingEdgesOf = HashMultimap.create();
            final Multimap<INode, IEdge> outgoingEdgesTo = HashMultimap.create();

            final Multimap<INode, CliqueDetectionEdge> incomingEdgesOfDetection = HashMultimap.create();
            final Multimap<INode, CliqueDetectionEdge> outgoingEdgesToDetection = HashMultimap.create();

            //Collect all the edges which are relevant to keep.
            for (INode iNode : clique)
            {
                for (IEdge iEdge : graph.incomingEdgesOf(iNode))
                {
                    if (!clique.contains(graph.getEdgeSource(iEdge)))
                    {
                        if (!recipesToRemove.contains(graph.getEdgeSource(iEdge)))
                        {
                            if (!inputNodesToDelete.contains(graph.getEdgeSource(iEdge)))
                            {
                                if (!incomingEdgesTo.containsEntry(iNode, iEdge))
                                {
                                    incomingEdgesTo.put(iNode, iEdge);
                                    incomingEdgesOf.put(graph.getEdgeSource(iEdge), iEdge);
                                    incomingEdges.put(iEdge, graph.getEdgeSource(iEdge));
                                }
                            }
                        }
                    }
                }

                for (IEdge iEdge : graph.outgoingEdgesOf(iNode))
                {
                    if (!clique.contains(graph.getEdgeTarget(iEdge)))
                    {
                        if (!recipesToRemove.contains(graph.getEdgeTarget(iEdge)))
                        {
                            if (!inputNodesToDelete.contains(graph.getEdgeTarget(iEdge)))
                            {
                                if (!outgoingEdgesOf.containsEntry(iNode, iEdge))
                                {
                                    outgoingEdgesOf.put(iNode, iEdge);
                                    outgoingEdgesTo.put(graph.getEdgeTarget(iEdge), iEdge);
                                    outgoingEdges.put(iEdge, graph.getEdgeTarget(iEdge));
                                }
                            }
                        }
                    }
                }

                for (CliqueDetectionEdge cliqueDetectionEdge : detectionGraph.incomingEdgesOf(iNode))
                {
                    if (!clique.contains(detectionGraph.getEdgeSource(cliqueDetectionEdge)))
                    {
                        incomingEdgesOfDetection.put(detectionGraph.getEdgeSource(cliqueDetectionEdge), cliqueDetectionEdge);
                    }
                }

                for (CliqueDetectionEdge edge : detectionGraph.outgoingEdgesOf(iNode))
                {
                    if (!clique.contains(detectionGraph.getEdgeTarget(edge)))
                    {
                        outgoingEdgesToDetection.put(detectionGraph.getEdgeTarget(edge), edge);
                    }
                }
            }

            for (IEdge iEdge : incomingEdges.keySet())
            {
                outgoingEdges.remove(iEdge);
            }

            AnalysisLogHandler.debug(LOGGER, String.format("  > Detected: %s as incoming edges to keep.", incomingEdges));
            AnalysisLogHandler.debug(LOGGER, String.format("  > Detected: %s as outgoing edges to keep.", outgoingEdges));

            //Create the new cycle construct.
            graph.addVertex(replacementNode);
            detectionGraph.addVertex(replacementNode);
            for (INode iNode : incomingEdgesOf.keySet())
            {
                double newEdgeWeight = 0.0;
                for (IEdge iEdge : incomingEdgesOf.get(iNode))
                {
                    double weight = iEdge.getWeight();
                    newEdgeWeight += weight;
                }
                graph.addEdge(iNode, replacementNode);
                graph.setEdgeWeight(iNode, replacementNode, newEdgeWeight);
            }
            for (INode iNode : outgoingEdgesTo.keySet())
            {
                double newEdgeWeight = 0.0;
                for (IEdge iEdge : outgoingEdgesTo.get(iNode))
                {
                    double weight = iEdge.getWeight();
                    newEdgeWeight += weight;
                }
                graph.addEdge(replacementNode, iNode);
                graph.setEdgeWeight(replacementNode, iNode, newEdgeWeight);
            }
            for (INode incomingSource : incomingEdgesOfDetection.keySet())
            {
                double newEdgeWeight = 0.0;
                for (CliqueDetectionEdge cliqueDetectionEdge : incomingEdgesOfDetection.get(incomingSource))
                {
                    double weight = cliqueDetectionEdge.getWeight();
                    newEdgeWeight += weight;
                }
                final Set<IRecipeNode> newRecipes = new HashSet<>();
                for (CliqueDetectionEdge e : incomingEdgesOfDetection.get(incomingSource))
                {
                    newRecipes.addAll(e.getRecipeNodes());
                }
                detectionGraph.addEdge(incomingSource, replacementNode, new CliqueDetectionEdge(newRecipes));
                detectionGraph.setEdgeWeight(incomingSource, replacementNode, newEdgeWeight);
            }
            for (INode outgoingTarget : outgoingEdgesToDetection.keySet())
            {
                double newEdgeWeight = 0.0;
                for (CliqueDetectionEdge cliqueDetectionEdge : outgoingEdgesToDetection.get(outgoingTarget))
                {
                    double weight = cliqueDetectionEdge.getWeight();
                    newEdgeWeight += weight;
                }
                final Set<IRecipeNode> newRecipes = new HashSet<>();
                for (CliqueDetectionEdge e : outgoingEdgesToDetection.get(outgoingTarget))
                {
                    newRecipes.addAll(e.getRecipeNodes());
                }
                detectionGraph.addEdge(replacementNode, outgoingTarget, new CliqueDetectionEdge(newRecipes));
                detectionGraph.setEdgeWeight(replacementNode, outgoingTarget, newEdgeWeight);
            }

            graph.removeAllVertices(clique);
            detectionGraph.removeAllVertices(clique);
            graph.removeAllVertices(recipesToRemove);
            graph.removeAllVertices(inputNodesToDelete);

            incomingEdgesTo.forEach((cycleNode, edge) -> onNeighborNodeReplacedCallback.accept(incomingEdges.get(edge), cycleNode, replacementNode));
            outgoingEdgesOf.forEach((cycleNode, edge) -> onNeighborNodeReplacedCallback.accept(outgoingEdges.get(edge), cycleNode, replacementNode));

            AnalysisLogHandler.debug(LOGGER, String.format(" > Removed clique: %s", clique));
        }
    }

    private LinkedHashSet<Set<INode>> updateRemainingCliquesAfterReplacement(final LinkedHashSet<Set<INode>> cliques, final Set<INode> replacedClique, final INode replacementNode) {
        cliques.remove(replacedClique);

        List<Set<INode>> toSort = new ArrayList<>();
        for (Set<INode> cycle : cliques)
        {
            final List<INode> intersectingNodes = new ArrayList<>();
            for (INode iNode : cycle)
            {
                if (replacedClique.contains(iNode))
                {
                    intersectingNodes.add(iNode);
                }
            }
            if (!intersectingNodes.isEmpty())
            {
                cycle.removeAll(intersectingNodes);
                cycle.add(replacementNode);
            }
            if (cycle.size() > 1)
            {
                toSort.add(cycle);
            }
        }
        toSort.sort(Comparator.comparing(Set::size));
        return new LinkedHashSet<>(toSort);
    }

    public CliqueDetectionGraph buildDetectionGraph(final G graph)
    {
        final CliqueDetectionGraph target = new CliqueDetectionGraph();
        for (INode iNode : graph.vertexSet())
        {
            if (iNode instanceof IRecipeNode)
            {
                IRecipeNode r = (IRecipeNode) iNode;
                if (graph.incomingEdgesOf(r).size() == 1)
                {
                    if (graph.outgoingEdgesOf(r).size() == 1)
                    {
                        if (graph.outgoingEdgesOf(r).iterator().next().getWeight() == graph.incomingEdgesOf(r).iterator().next().getWeight())
                        {
                            final Set<IContainerNode> inputs = new HashSet<>();
                            for (IEdge iEdge : graph.incomingEdgesOf(
                              graph.getEdgeSource(
                                graph.incomingEdgesOf(r).iterator().next()
                              )
                            ))
                            {
                                INode edgeSource = graph.getEdgeSource(iEdge);
                                IContainerNode containerNode = (IContainerNode) edgeSource;
                                inputs.add(containerNode);
                            }

                            final IContainerNode output = (IContainerNode) graph.getEdgeTarget(graph.outgoingEdgesOf(r).iterator().next());

                            inputs.remove(output);

                            for (IContainerNode input : inputs)
                            {
                                if (!target.containsVertex(input))
                                {
                                    target.addVertex(input);
                                }

                                if (!target.containsVertex(output))
                                {
                                    target.addVertex(output);
                                }

                                if (target.containsEdge(input, output))
                                {
                                    target.getEdge(input, output).getRecipeNodes().add(r);
                                }
                                else if (!input.equals(output))
                                {
                                    target.addEdge(input, output, new CliqueDetectionEdge(r));
                                }
                            }
                        }
                    }
                }
            }
        }

        return target;
    }
}
