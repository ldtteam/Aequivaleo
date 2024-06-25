package com.ldtteam.aequivaleo.analysis.jgrapht.clique;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.*;
import com.ldtteam.aequivaleo.analysis.jgrapht.clique.graph.CliqueDetectionEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.clique.graph.CliqueDetectionGraph;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.MaximalCliqueEnumerationAlgorithm;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JGraphTCliqueReducer<G extends Graph<N, E>, N, E>
{

    private static final Logger LOGGER = LogManager.getLogger();

    private final Function<Collection<? extends N>, N> vertexReplacerFunction;

    public JGraphTCliqueReducer(
      final Function<Collection<? extends N>, N> vertexReplacerFunction)
    {
        this.vertexReplacerFunction = vertexReplacerFunction;
    }

    @SuppressWarnings("DuplicatedCode")
    public void reduce(final G graph)
    {
        final CliqueDetectionGraph<N> detectionGraph = buildDetectionGraph(graph);

        final MaximalCliqueEnumerationAlgorithm<N, CliqueDetectionEdge<N>> cliqueFinder = new BronKerboschDirectedCliqueFinder<>(detectionGraph);
        final Set<Set<N>> cliquesCandidates = new HashSet<>();
        cliqueFinder.forEach(cliquesCandidates::add);
        
        final LinkedList<Set<N>> cliques = new LinkedList<>(cliquesCandidates);
        cliques.sort(Comparator.comparing((Function<Set<N>, Integer>) Set::size).reversed());

        final Map<N, Set<N>> replacementNodes = new HashMap<>();
        final Map<N, N> replacements = new HashMap<>();

        final Map<N, Set<N>> reducedCliques = new HashMap<>();
        for (Set<N> clique : cliques) {
            //For all nodes in the detection graphs clique collect their intermediary nodes.
            for (N sourceNode : clique) {
                //We know the complete clique in the full graph will contain at least the detection graphs node.
                reducedCliques.computeIfAbsent(sourceNode, k -> new HashSet<>()).add(sourceNode);

                //Loop over all nodes again in the detection clique.
                for (N targetNode : clique) {
                    //Skip the self edge, if it were to exist.
                    if (targetNode == sourceNode) continue;

                    //Get the detection graphs edge, so we can grab the intermediary nodes that are made up out of it.
                    final CliqueDetectionEdge<N> detectionEdge = detectionGraph.getEdge(sourceNode, targetNode);
                    //Add all the intermediary nodes as well.
                    for (N intermediaryNode : detectionEdge.getIntermediaryNodes()) {
                        reducedCliques.computeIfAbsent(intermediaryNode, k -> new HashSet<>()).add(intermediaryNode);
                    }
                }
            }
        }

        final Set<Set<N>> reducedCliquesSet = new HashSet<>(reducedCliques.values());

        for (Set<N> clique : reducedCliquesSet) {
            final N replacementNode = vertexReplacerFunction.apply(clique);

            graph.addVertex(replacementNode);

            for (N node : clique) {
                replacements.put(node, replacementNode);
                final N workingNode = replacements.getOrDefault(node, node);
                final Set<E> incomingEdges = new HashSet<>(graph.incomingEdgesOf(workingNode));
                final Set<E> outgoingEdges = new HashSet<>(graph.outgoingEdgesOf(workingNode));

                for (E incomingEdge : incomingEdges) {
                    final N sourceNode = Graphs.getOppositeVertex(graph, incomingEdge, workingNode);
                    if (!clique.contains(sourceNode)) {
                        final E newEdge = graph.addEdge(sourceNode, replacementNode);
                        graph.setEdgeWeight(newEdge, graph.getEdgeWeight(incomingEdge));
                    }
                }
            }
        }

        while(!cliques.isEmpty()) {
            //Get the next clique
            final Set<N> clique = cliques.pollFirst();
            //Should not happen, but just to be sure.
            if (clique == null)
                break;



            //Since a given node can be part of multiple cliques;
            //we need to check if we already replaced any given node that we think is part of the clique
            //To do this we check our replacements map, and potentially replace the node.
            final Set<N> currentRecipeClique = new LinkedHashSet<>();
            for (N node : recipeClique) {
                currentRecipeClique.add(getReplacedNode(node, replacements));
            }

            final Set<N> flattenedRecipeClique = new LinkedHashSet<>();
            for (N node : currentRecipeClique) {
                if (replacementNodes.containsKey(node)) {
                    flattenedRecipeClique.addAll(replacementNodes.get(node));
                } else {
                    flattenedRecipeClique.add(node);
                }
            }

            //Now we know exactly which nodes are part of the clique we generate a replacement node.
            final N replacedNode = vertexReplacerFunction.apply(flattenedRecipeClique);

            //We need a list of edges to keep which are pointing into the clique from outside of it.
            final Table<N, N, E> incomingEdgesToKeep = HashBasedTable.create();
            final Table<N, N, E> outgoingEdgesToKeep = HashBasedTable.create();
            //Loop over all nodes in the clique.
            for (N flattenedNode : flattenedRecipeClique) {
                final N node = getReplacedNode(flattenedNode, replacements);
                //Get the incoming edges of the node, some are in the clique, some might not be in the clique.
                final Set<E> incomingEdges = graph.incomingEdgesOf(node);

                //Now for each edge check if said edge is part of the clique or not.
                for (E incomingEdge : incomingEdges) {
                    //Get the source node of the incoming edge.
                    final N sourceNode = Graphs.getOppositeVertex(graph, incomingEdge, node);

                    //Check whether the source node is part of the clique
                    if (!flattenedRecipeClique.contains(sourceNode)) {
                        //The source is not part of the clique, we need to keep that incoming edge.
                        incomingEdgesToKeep.put(sourceNode, node, incomingEdge);
                    }
                }

                //Get the outgoing edges of the node, some are in the clique, some might not be in the clique.
                final Set<E> outgoingEdges = graph.outgoingEdgesOf(node);

                //Now for each edge check if said edge is part of the clique or not.
                for (E outgoingEdge : outgoingEdges) {
                    //Get the target node of the outgoing edge.
                    final N targetNode = Graphs.getOppositeVertex(graph, outgoingEdge, node);

                    //Check whether the target node is part of the clique
                    if (!flattenedRecipeClique.contains(targetNode)) {
                        //The target is not part of the clique we need to keep that outgoing edge.
                        outgoingEdgesToKeep.put(node, targetNode, outgoingEdge);
                    }
                }
            }

            //Output debugging information.
            AnalysisLogHandler.debug(LOGGER, String.format("  > Detected: %s as incoming edges to keep.", incomingEdgesToKeep.values()));
            AnalysisLogHandler.debug(LOGGER, String.format("  > Detected: %s as outgoing edges to keep.", outgoingEdgesToKeep.values()));

            //Now add new replacement node
            graph.addVertex(replacedNode);

            //Loop over all incoming edges to stay and then check if weights need to be updated.
            for (N sourceNode : incomingEdgesToKeep.rowKeySet()) {
                //Get all edges to keep for a given source node.
                final Map<N, E> edgesToKeepForSourceNode = incomingEdgesToKeep.row(sourceNode);

                //Check if data is valid.
                if (edgesToKeepForSourceNode.isEmpty())
                    continue;

                //Create a new incoming edge, we know we need it anyway since an incoming edge exists.
                final E newIncomingEdge = graph.addEdge(sourceNode, replacedNode);

                //Short circuit to prevent the creation of streams when there is only a single incoming edge from the source node.
                if (edgesToKeepForSourceNode.size() == 1) {
                    //Get the only incoming edge directly.
                    final E currentEdge = edgesToKeepForSourceNode.values().iterator().next();

                    //Gets its edge weight
                    final double weight = graph.getEdgeWeight(currentEdge);

                    //Update the edge weight and then continue with the next source node.
                    graph.setEdgeWeight(newIncomingEdge, weight);
                    continue;
                }

                //Sum up the total incoming edge weight
                double weight = edgesToKeepForSourceNode.values()
                        .stream().mapToDouble(graph::getEdgeWeight).sum();

                //Update the edge weight.
                graph.setEdgeWeight(newIncomingEdge, weight);
            }

            //Loop over all outgoing edges to stay and then check if weights need to be updated.
            for(N targetNode : outgoingEdgesToKeep.columnKeySet()) {
                //Get all edges to keep for a given target node.
                final Map<N, E> edgesToKeepForTargetNode = outgoingEdgesToKeep.column(targetNode);

                if (graph.getEdge(targetNode, replacedNode) != null) {
                    throw new IllegalStateException("Clique does not contain all the relevant nodes.");
                }

                //Check if the data is valid.
                if (edgesToKeepForTargetNode.isEmpty())
                    continue;

                //Create a new outgoing edge, we know we need it anyway since an outgoing edge exists.
                final E newOutgoingEdge = graph.addEdge(replacedNode, targetNode);

                //Short circuit to prevent the creation of streams when there is only a single outgoing edge to the target node.
                if (edgesToKeepForTargetNode.size() == 1) {
                    //Get the only outgoing edge directly
                    final E currentEdge = edgesToKeepForTargetNode.values().iterator().next();

                    //Get its edge weight
                    final double weight = graph.getEdgeWeight(currentEdge);

                    //Update the edge weight and then continue with the next source node.
                    graph.setEdgeWeight(newOutgoingEdge, weight);
                    continue;
                }

                //Sum up the total incoming edge weight
                double weight = edgesToKeepForTargetNode.values()
                        .stream().mapToDouble(graph::getEdgeWeight).sum();

                //Update the edge weight.
                graph.setEdgeWeight(newOutgoingEdge, weight);
            }

            removeNodes(graph, flattenedRecipeClique, replacements);

            replacementNodes.put(replacedNode, flattenedRecipeClique);
            //Store the replacements for the next clique.
            for (N node : flattenedRecipeClique) {
                replacements.put(node, replacedNode);
            }

            AnalysisLogHandler.debug(LOGGER, String.format(" > Removed clique: %s", recipeClique));
        }
    }

    private N getReplacedNode(final N node, final Map<N, N> replacements) {
        final N replacedNode = replacements.getOrDefault(node, node);
        if (replacedNode == node)
            return replacedNode;

        return getReplacedNode(replacedNode, replacements);
    }

    private void removeNodes(final Graph<N, E> graph, final Set<N> nodes, Map<N, N> replacements) {
        for (final N node : nodes)
        {
            graph.removeVertex(getReplacedNode(node, replacements));
        }
    }

    public CliqueDetectionGraph<N> buildDetectionGraph(final G graph)
    {
        final CliqueDetectionGraph<N> target = new CliqueDetectionGraph<>();
        for (N node : graph.vertexSet())
        {
            if (node instanceof IRecipeNode)
            {
                final Set<N> inputNodes = graph.incomingEdgesOf(node)
                        .stream()
                        .map(edge -> Graphs.getOppositeVertex(graph, edge, node))
                        .peek(n -> {
                            if (!(n instanceof IIngredientNode))
                                throw new IllegalStateException("Recipe node has non-ingredient input node.");
                        })
                        .collect(Collectors.toSet());

                final Set<N> outputNodes = graph.outgoingEdgesOf(node)
                        .stream()
                        .map(edge -> Graphs.getOppositeVertex(graph, edge, node))
                        .peek(n -> {
                            if (!(n instanceof IContainerNode))
                                throw new IllegalStateException("Recipe node has non-container output node.");
                        })
                        .collect(Collectors.toSet());

                for (N inputNode : inputNodes) {
                    final double inWeight = graph.getEdgeWeight(graph.getEdge(inputNode, node));

                    final Set<N> candidateInputNodes = graph.incomingEdgesOf(inputNode)
                            .stream()
                            .map(edge -> Graphs.getOppositeVertex(graph, edge, inputNode))
                            .peek(n -> {
                                if (!(n instanceof IContainerNode))
                                    throw new IllegalStateException("Ingredient node has non-container input node.");
                            })
                            .collect(Collectors.toSet());

                    for (N candidateInputNode : candidateInputNodes) {
                        for (N outputNode : outputNodes) {
                            if (candidateInputNode.equals(outputNode))
                                continue;

                            final double outWeight = graph.getEdgeWeight(graph.getEdge(node, outputNode));

                            if (Math.abs((inWeight - outWeight)) < 0.00001) {
                                target.addVertex(candidateInputNode);
                                target.addVertex(outputNode);
                                if (!target.containsEdge(candidateInputNode, outputNode)) {
                                    target.addEdge(candidateInputNode, outputNode, new CliqueDetectionEdge<>(Set.of(inputNode, node)));
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
