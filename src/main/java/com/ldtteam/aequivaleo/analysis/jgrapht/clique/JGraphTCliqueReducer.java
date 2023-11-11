package com.ldtteam.aequivaleo.analysis.jgrapht.clique;

import com.google.common.collect.*;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.*;
import com.ldtteam.aequivaleo.analysis.jgrapht.clique.graph.CliqueDetectionEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.clique.graph.CliqueDetectionGraph;
import com.ldtteam.aequivaleo.api.util.QuadFunction;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.clique.BronKerboschCliqueFinder;
import org.jgrapht.alg.clique.PivotBronKerboschCliqueFinder;
import org.jgrapht.alg.interfaces.MaximalCliqueEnumerationAlgorithm;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JGraphTCliqueReducer<G extends Graph<INode, IEdge>>
{

    private static final Logger LOGGER = LogManager.getLogger();

    private final BiFunction<G, Set<INode>, INode> vertexReplacerFunction;
    private final TriConsumer<INode, INode, INode> onNeighborNodeReplacedCallback;

    public JGraphTCliqueReducer(
      final BiFunction<G, Set<INode>, INode> vertexReplacerFunction,
      final TriConsumer<INode, INode, INode> onNeighborNodeReplacedCallback)
    {
        this.vertexReplacerFunction = vertexReplacerFunction;
        this.onNeighborNodeReplacedCallback = onNeighborNodeReplacedCallback;
    }

    @SuppressWarnings("DuplicatedCode")
    public void reduce(final G graph)
    {
        final CliqueDetectionGraph detectionGraph = buildDetectionGraph(graph);

        final MaximalCliqueEnumerationAlgorithm<INode, CliqueDetectionEdge> cliqueFinder = new BronKerboschDirectedCliqueFinder<>(detectionGraph);
        final Set<Set<INode>> cliquesCandidates = new HashSet<>();
        cliqueFinder.forEach(cliquesCandidates::add);
        
        final LinkedList<Set<INode>> cliques = new LinkedList<>(cliquesCandidates);
        cliques.sort(Comparator.comparing((Function<Set<INode>, Integer>) Set::size).reversed());
        
        final Map<INode, INode> replacements = new HashMap<>();

        while(!cliques.isEmpty()) {
            //Get the next clique
            final Set<INode> clique = cliques.pollFirst();
            //Should not happen, but just to be sure.
            if (clique == null)
                break;

            //Collect all nodes that are part of the clique within the normal graph.
            final Set<INode> recipeClique = new LinkedHashSet<>();
            //For all nodes in the detection graphs clique collect their intermediary nodes.
            for (INode sourceNode : clique) {
                //We know the complete clique in the full graph will contain at least the detection graphs node.
                recipeClique.add(sourceNode);

                //Loop over all nodes again in the detection clique.
                for (INode targetNode : clique) {
                    //Skip the self edge, if it were to exist.
                    if (targetNode == sourceNode) continue;

                    //Get the detection graphs edge, so we can grab the intermediary nodes that are made up out of it.
                    final CliqueDetectionEdge detectionEdge = detectionGraph.getEdge(sourceNode, targetNode);
                    //Add all the intermediary nodes as well.
                    recipeClique.addAll(detectionEdge.getIntermediaryNodes());
                }
            }

            //Since a given node can be part of multiple cliques;
            //we need to check if we already replaced any given node that we think is part of the clique
            //To do this we check our replacements map, and potentially replace the node.
            final Set<INode> currentRecipeClique = new LinkedHashSet<>();
            for (INode node : recipeClique) {
                currentRecipeClique.add(getReplacedNode(node, replacements));
            }

            //Now we know exactly which nodes are part of the clique we generate a replacement node.
            final INode replacedNode = vertexReplacerFunction.apply(graph, currentRecipeClique);
            //Store the replacements for the next clique.
            for (INode node : currentRecipeClique) {
                replacements.put(node, replacedNode);
            }

            //We need a list of edges to keep which are pointing into the clique from outside of it.
            final Table<INode, INode, IEdge> incomingEdgesToKeep = HashBasedTable.create();
            final Table<INode, INode, IEdge> outgoingEdgesToKeep = HashBasedTable.create();
            //Loop over all nodes in the clique.
            for (INode node : currentRecipeClique) {
                //Get the incoming edges of the node, some are in the clique, some might not be in the clique.
                final Set<IEdge> incomingEdges = graph.incomingEdgesOf(node);

                //Now for each edge check if said edge is part of the clique or not.
                for (IEdge incomingEdge : incomingEdges) {
                    //Get the source node of the incoming edge.
                    final INode sourceNode = Graphs.getOppositeVertex(graph, incomingEdge, node);

                    //Check whether the source node is part of the clique
                    if (!currentRecipeClique.contains(sourceNode)) {
                        //The source is not part of the clique, we need to keep that incoming edge.
                        incomingEdgesToKeep.put(sourceNode, node, incomingEdge);
                    }
                }

                //Get the outgoing edges of the node, some are in the clique, some might not be in the clique.
                final Set<IEdge> outgoingEdges = graph.outgoingEdgesOf(node);

                //Now for each edge check if said edge is part of the clique or not.
                for (IEdge outgoingEdge : outgoingEdges) {
                    //Get the target node of the outgoing edge.
                    final INode targetNode = Graphs.getOppositeVertex(graph, outgoingEdge, node);

                    //Check whether the target node is part of the clique
                    if (!currentRecipeClique.contains(targetNode)) {
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
            for (INode sourceNode : incomingEdgesToKeep.rowKeySet()) {
                //Get all edges to keep for a given source node.
                final Map<INode, IEdge> edgesToKeepForSourceNode = incomingEdgesToKeep.row(sourceNode);

                //Check if data is valid.
                if (edgesToKeepForSourceNode.isEmpty())
                    continue;

                //Create a new incoming edge, we know we need it anyway since an incoming edge exists.
                final IEdge newIncomingEdge = graph.addEdge(sourceNode, replacedNode);

                //Short circuit to prevent the creation of streams when there is only a single incoming edge from the source node.
                if (edgesToKeepForSourceNode.size() == 1) {
                    //Get the only incoming edge directly.
                    final IEdge currentEdge = edgesToKeepForSourceNode.values().iterator().next();

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
            for(INode targetNode : outgoingEdgesToKeep.columnKeySet()) {
                //Get all edges to keep for a given target node.
                final Map<INode, IEdge> edgesToKeepForTargetNode = outgoingEdgesToKeep.column(targetNode);

                //Check if the data is valid.
                if (edgesToKeepForTargetNode.isEmpty())
                    continue;

                //Create a new outgoing edge, we know we need it anyway since an outgoing edge exists.
                final IEdge newOutgoingEdge = graph.addEdge(replacedNode, targetNode);

                //Short circuit to prevent the creation of streams when there is only a single outgoing edge to the target node.
                if (edgesToKeepForTargetNode.size() == 1) {
                    //Get the only outgoing edge directly
                    final IEdge currentEdge = edgesToKeepForTargetNode.values().iterator().next();

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

            for (Table.Cell<INode, INode, IEdge> outsideNodeCliqueNodeEdge : incomingEdgesToKeep.cellSet()) {
                final INode sourceNode = outsideNodeCliqueNodeEdge.getRowKey();
                final INode oldNode = outsideNodeCliqueNodeEdge.getColumnKey();

                onNeighborNodeReplacedCallback.accept(sourceNode, oldNode, replacedNode);
            }

            for (Table.Cell<INode, INode, IEdge> cliqueNodeOutsideNodeEdge : outgoingEdgesToKeep.cellSet()) {
                final INode oldNode = cliqueNodeOutsideNodeEdge.getRowKey();
                final INode targetNode = cliqueNodeOutsideNodeEdge.getColumnKey();

                onNeighborNodeReplacedCallback.accept(targetNode, oldNode, replacedNode);
            }

            removeNodes(graph, currentRecipeClique);

            AnalysisLogHandler.debug(LOGGER, String.format(" > Removed clique: %s", recipeClique));
        }
    }

    private INode getReplacedNode(final INode node, final Map<INode, INode> replacements) {
        final INode replacedNode = replacements.getOrDefault(node, node);
        if (replacedNode == node)
            return replacedNode;

        return getReplacedNode(replacedNode, replacements);
    }

    private void removeNodes(final Graph<INode, IEdge> graph, final Set<INode> nodes) {
        for (final INode node : nodes)
        {
            graph.removeVertex(node);
        }
    }

    public CliqueDetectionGraph buildDetectionGraph(final G graph)
    {
        final CliqueDetectionGraph target = new CliqueDetectionGraph();
        for (INode node : graph.vertexSet())
        {
            if (node instanceof IRecipeNode recipeNode)
            {
                final Set<IRecipeInputNode> inputNodes = graph.incomingEdgesOf(recipeNode)
                        .stream()
                        .map(edge -> Graphs.getOppositeVertex(graph, edge, recipeNode))
                        .filter(IRecipeInputNode.class::isInstance)
                        .map(IRecipeInputNode.class::cast)
                        .collect(Collectors.toSet());

                final Set<IContainerNode> outputNodes = graph.outgoingEdgesOf(recipeNode)
                        .stream()
                        .map(edge -> Graphs.getOppositeVertex(graph, edge, recipeNode))
                        .filter(IContainerNode.class::isInstance)
                        .map(IContainerNode.class::cast)
                        .collect(Collectors.toSet());

                for (IRecipeInputNode inputNode : inputNodes) {
                    final double inWeight = graph.getEdgeWeight(graph.getEdge(inputNode, recipeNode));

                    final Set<IContainerNode> candidateInputNodes = graph.incomingEdgesOf(inputNode)
                            .stream()
                            .map(edge -> Graphs.getOppositeVertex(graph, edge, inputNode))
                            .filter(IContainerNode.class::isInstance)
                            .map(IContainerNode.class::cast)
                            .collect(Collectors.toSet());

                    for (IContainerNode candidateInputNode : candidateInputNodes) {
                        for (IContainerNode outputNode : outputNodes) {
                            if (candidateInputNode.equals(outputNode))
                                continue;

                            final double outWeight = graph.getEdgeWeight(graph.getEdge(recipeNode, outputNode));

                            if (Math.abs((inWeight - outWeight)) < 0.00001) {
                                target.addVertex(candidateInputNode);
                                target.addVertex(outputNode);
                                if (!target.containsEdge(candidateInputNode, outputNode)) {
                                    target.addEdge(candidateInputNode, outputNode, new CliqueDetectionEdge(Set.of(inputNode, recipeNode)));
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
