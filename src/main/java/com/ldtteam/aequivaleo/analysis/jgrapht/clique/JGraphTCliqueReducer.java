package com.ldtteam.aequivaleo.analysis.jgrapht.clique;

import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IFreeNode;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.IGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.INode;
import com.ldtteam.aequivaleo.analysis.jgrapht.clique.graph.CliqueDetectionEdge;
import com.ldtteam.aequivaleo.analysis.jgrapht.clique.graph.CliqueDetectionGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.clique.reducer.ReductionGraphBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.MaximalCliqueEnumerationAlgorithm;

import java.util.*;
import java.util.function.Function;

public class JGraphTCliqueReducer
{
    private static final Logger LOGGER = LogManager.getLogger(JGraphTCliqueReducer.class);

    private final Function<Collection<? extends INode>, INode> vertexReplacerFunction;

    public JGraphTCliqueReducer(
      final Function<Collection<? extends INode>, INode> vertexReplacerFunction)
    {
        this.vertexReplacerFunction = vertexReplacerFunction;
    }

    @SuppressWarnings("DuplicatedCode")
    public void reduce(final IGraph graph)
    {
        final ReductionGraphBuilder<IGraph, INode, IEdge> builder = new ReductionGraphBuilder.RecipeNodeReducer();
        final CliqueDetectionGraph<INode> detectionGraph = builder.reduce(graph);

        final MaximalCliqueEnumerationAlgorithm<INode, CliqueDetectionEdge<INode>> cliqueFinder = new BronKerboschDirectedCliqueFinder<>(detectionGraph);
        final Set<Set<INode>> cliquesCandidates = new HashSet<>();
        cliqueFinder.forEach(cliquesCandidates::add);
        
        final LinkedList<Set<INode>> cliques = new LinkedList<>(cliquesCandidates);
        cliques.sort(Comparator.comparing((Function<Set<INode>, Integer>) Set::size).reversed());

        final Set<Set<INode>> explodedCliques = new HashSet<>();
        for (Set<INode> clique : cliques) {
            final Set<INode> explodedClique = new HashSet<>();

            //For all nodes in the detection graphs clique collect their intermediary nodes.
            for (INode sourceNode : clique) {
                //Add the source node to the exploded clique.
                explodedClique.add(sourceNode);
                //Loop over all nodes again in the detection clique.
                for (INode targetNode : clique) {
                    //Skip the self edge, if it were to exist.
                    if (targetNode == sourceNode) continue;

                    //Add the target node to the exploded clique.
                    explodedClique.add(targetNode);

                    //Get the detection graphs edge, so we can grab the intermediary nodes that are made up out of it.
                    final CliqueDetectionEdge<INode> detectionEdge = detectionGraph.getEdge(sourceNode, targetNode);
                    explodedClique.addAll(detectionEdge.getIntermediaryNodes());
                }
            }

            //Add the exploded clique to the reduced cliques.
            explodedCliques.add(explodedClique);
        }

        final Set<Set<INode>> mergedCliques = NodeMerger.mergeSets(explodedCliques);
        final LinkedList<Set<INode>> linkedCliques = new LinkedList<>(mergedCliques);
        linkedCliques.sort(Comparator.comparing((Function<Set<INode>, Integer>) Set::size).reversed());


        for (Set<INode> clique : linkedCliques) {
            final INode replacementNode = vertexReplacerFunction.apply(clique);

            graph.addVertex(replacementNode);

            for (INode node : clique) {
                final Set<IEdge> incomingEdges = new HashSet<>(graph.incomingEdgesOf(node));
                final Set<IEdge> outgoingEdges = new HashSet<>(graph.outgoingEdgesOf(node));

                for (IEdge incomingEdge : incomingEdges) {
                    final INode sourceNode = Graphs.getOppositeVertex(graph, incomingEdge, node);

                    final boolean wouldCreateCycle = graph.containsEdge(replacementNode, sourceNode);
                    if (wouldCreateCycle) {
                        //This can happen when recipes change input and output count between clique members.
                        //We can safely skip them (For example stripped log and wood)
                        LOGGER.debug("Clique reduction would create a incoming cycle. Skipping.");
                        if (sourceNode instanceof IFreeNode) {
                            //Free nodes that have the clique both as an input and output produce issues with processing order (because now the edge that determines,
                            //when they can be processed gets removed). We can safely remove them, since the target of their operation is still present in the clique
                            //And as such would not get the value it would calculate anyway.
                            graph.removeVertex(sourceNode);
                        }
                        continue;
                    }

                    if (!clique.contains(sourceNode)) {
                        graph.addEdgeOrUpdateWeight(sourceNode, replacementNode, graph.getEdgeWeight(incomingEdge));
                    }
                }

                for (IEdge outgoingEdge : outgoingEdges) {
                    final INode targetNode = Graphs.getOppositeVertex(graph, outgoingEdge, node);

                    final boolean wouldCreateCycle = graph.containsEdge(targetNode, replacementNode);
                    if (wouldCreateCycle) {
                        //This can happen when recipes change input and output count between clique members.
                        //We can safely skip them (For example stripped log and wood)
                        LOGGER.debug("Clique reduction would create an outgoing cycle. Skipping.");
                        if (targetNode instanceof IFreeNode) {
                            //Free nodes that have the clique both as an input and output produce issues with processing order (because now the edge that determines,
                            //when they can be processed gets removed). We can safely remove them, since the target of their operation is still present in the clique
                            //And as such would not get the value it would calculate anyway.
                            graph.removeVertex(targetNode);
                        }
                        continue;
                    }

                    if (!clique.contains(targetNode)) {
                        graph.addEdgeOrUpdateWeight(replacementNode, targetNode, graph.getEdgeWeight(outgoingEdge));
                    }
                }
            }

            removeNodes(graph, clique);
        }
    }

    private void removeNodes(final Graph<INode, IEdge> graph, final Set<INode> nodes) {
        for (final INode node : nodes)
        {
            graph.removeVertex(node);
        }
    }

    public static class NodeMerger {

        private NodeMerger() {
            throw new IllegalStateException("Utility class");
        }

        @Contract("_ -> new")
        public static <T> @NotNull Set<Set<T>> mergeSets(@NotNull final Set<Set<T>> inputSets) {
            final Map<T, Set<T>> mergedSets = new HashMap<>();

            for (Set<T> inputSet : inputSets) {
                final Set<Set<T>> toMerge = new HashSet<>();
                for (T element : inputSet) {
                    if (mergedSets.containsKey(element)) {
                        toMerge.add(mergedSets.get(element));
                    }
                }

                if (toMerge.isEmpty()) {
                    for (T t : inputSet) {
                        mergedSets.put(t, inputSet);
                    }
                } else {
                    final Set<T> mergedSet = new HashSet<>();
                    for (Set<T> set : toMerge) {
                        mergedSet.addAll(set);
                    }
                    mergedSet.addAll(inputSet);
                    for (T t : mergedSet) {
                        mergedSets.put(t, mergedSet);
                    }
                }
            }

            return new HashSet<>(mergedSets.values());
        }
    }
}
