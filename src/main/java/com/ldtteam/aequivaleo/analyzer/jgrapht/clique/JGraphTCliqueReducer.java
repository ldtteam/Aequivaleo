package com.ldtteam.aequivaleo.analyzer.jgrapht.clique;

import com.google.common.collect.HashMultimap;
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
import java.util.stream.StreamSupport;

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

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public void reduce(final G graph)
    {
        final CliqueDetectionGraph detectionGraph = buildDetectionGraph(graph);

        final MaximalCliqueEnumerationAlgorithm<INode, CliqueDetectionEdge> cliqueFinder = new BronKerboschCliqueFinder<>(detectionGraph);
        LinkedHashSet<Set<INode>> cliques = StreamSupport.stream(cliqueFinder.spliterator(), false)
                                                             .sorted(Comparator.comparing(Set::size))
                                                             .collect(Collectors.toCollection(LinkedHashSet::new));

        while(!cliques.isEmpty()) {
            final Set<INode> clique = cliques.iterator().next();
            final Set<IRecipeNode> recipesToRemove = this.cliqueRecipeExtractor.apply(clique
                                                       .stream()
                                                       .flatMap(cliqueEntry -> clique.stream()
                                                                                 .filter(secondEntry -> secondEntry != cliqueEntry)
                                                                                 .map(secondEntry -> detectionGraph.getEdge(cliqueEntry, secondEntry))
                                                                                 .filter(Objects::nonNull)
                                                                                 .map(CliqueDetectionEdge::getRecipeNodes)
                                                       )
                                                       .collect(Collectors.toList()));

            if (recipesToRemove.isEmpty())
            {
                cliques.remove(clique);
                continue;
            }

            final Set<IRecipeInputNode> relevantInputNodes = recipesToRemove.stream()
              .flatMap(cliqueRecipeNode -> graph.incomingEdgesOf(cliqueRecipeNode).stream())
              .map(graph::getEdgeSource)
              .filter(IRecipeInputNode.class::isInstance)
              .map(IRecipeInputNode.class::cast)
              .collect(Collectors.toSet());

            final Set<IRecipeInputNode> inputNodesToDelete = relevantInputNodes
              .stream()
              .filter(inputNode -> recipesToRemove.containsAll(graph.outgoingEdgesOf(inputNode)
                .stream()
                .map(graph::getEdgeTarget)
                .collect(Collectors.toSet()))
              )
              .collect(Collectors.toSet());

            final INode replacementNode = vertexReplacerFunction.apply(
              graph,
              clique,
              recipesToRemove,
              inputNodesToDelete
            );

            cliques = updateRemainingCliquesAfterReplacement(
              cliques,
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
            clique.forEach(cycleNode -> {
                graph.incomingEdgesOf(cycleNode)
                  .stream()
                  .filter(edge -> !clique.contains(graph.getEdgeSource(edge)))
                  .filter(edge -> !recipesToRemove.contains(graph.getEdgeSource(edge)))
                  .filter(edge -> !inputNodesToDelete.contains(graph.getEdgeSource(edge)))
                  .filter(edge -> !incomingEdgesTo.containsEntry(cycleNode, edge))
                  .peek(edge -> incomingEdgesTo.put(cycleNode, edge))
                  .peek(edge -> incomingEdgesOf.put(graph.getEdgeSource(edge), edge))
                  .forEach(edge -> incomingEdges.put(edge, graph.getEdgeSource(edge)));

                graph.outgoingEdgesOf(cycleNode)
                  .stream()
                  .filter(edge -> !clique.contains(graph.getEdgeTarget(edge)))
                  .filter(edge -> !recipesToRemove.contains(graph.getEdgeTarget(edge)))
                  .filter(edge -> !inputNodesToDelete.contains(graph.getEdgeTarget(edge)))
                  .filter(edge -> !outgoingEdgesOf.containsEntry(cycleNode, edge))
                  .peek(edge -> outgoingEdgesOf.put(cycleNode, edge))
                  .peek(edge -> outgoingEdgesTo.put(graph.getEdgeTarget(edge), edge))
                  .forEach(edge -> outgoingEdges.put(edge, graph.getEdgeTarget(edge)));

                detectionGraph.incomingEdgesOf(cycleNode)
                  .stream()
                  .filter(edge -> !clique.contains(detectionGraph.getEdgeSource(edge)))
                  .forEach(edge -> incomingEdgesOfDetection.put(detectionGraph.getEdgeSource(edge), edge));

                detectionGraph.outgoingEdgesOf(cycleNode)
                  .stream()
                  .filter(edge -> !clique.contains(detectionGraph.getEdgeTarget(edge)))
                  .forEach(edge -> outgoingEdgesToDetection.put(detectionGraph.getEdgeTarget(edge), edge));
            });

            incomingEdges.keySet().forEach(outgoingEdges::remove);

            AnalysisLogHandler.debug(LOGGER, String.format("  > Detected: %s as incoming edges to keep.", incomingEdges));
            AnalysisLogHandler.debug(LOGGER, String.format("  > Detected: %s as outgoing edges to keep.", outgoingEdges));

            //Create the new cycle construct.
            graph.addVertex(replacementNode);
            detectionGraph.addVertex(replacementNode);
            incomingEdgesOf.keySet()
              .forEach(incomingSource -> {
                  final double newEdgeWeight = incomingEdgesOf.get(incomingSource).stream().mapToDouble(IAnalysisEdge::getWeight).sum();
                  graph.addEdge(incomingSource, replacementNode);
                  graph.setEdgeWeight(incomingSource, replacementNode, newEdgeWeight);
              });
            outgoingEdgesTo.keySet()
              .forEach(outgoingTarget -> {
                  final double newEdgeWeight = outgoingEdgesTo.get(outgoingTarget).stream().mapToDouble(IAnalysisEdge::getWeight).sum();
                  graph.addEdge(replacementNode, outgoingTarget);
                  graph.setEdgeWeight(replacementNode, outgoingTarget, newEdgeWeight);
              });
            incomingEdgesOfDetection.keySet()
              .forEach(incomingSource -> {
                  final double newEdgeWeight = incomingEdgesOfDetection.get(incomingSource).stream().mapToDouble(IAnalysisEdge::getWeight).sum();
                  final Set<IRecipeNode> newRecipes = incomingEdgesOfDetection.get(incomingSource).stream().flatMap(e -> e.getRecipeNodes().stream()).collect(Collectors.toSet());
                  detectionGraph.addEdge(incomingSource, replacementNode, new CliqueDetectionEdge(newRecipes));
                  detectionGraph.setEdgeWeight(incomingSource, replacementNode, newEdgeWeight);
              });
            outgoingEdgesToDetection.keySet()
              .forEach(outgoingTarget -> {
                  final double newEdgeWeight = outgoingEdgesToDetection.get(outgoingTarget).stream().mapToDouble(IAnalysisEdge::getWeight).sum();
                  final Set<IRecipeNode> newRecipes = outgoingEdgesToDetection.get(outgoingTarget).stream().flatMap(e -> e.getRecipeNodes().stream()).collect(Collectors.toSet());
                  detectionGraph.addEdge(replacementNode, outgoingTarget, new CliqueDetectionEdge(newRecipes));
                  detectionGraph.setEdgeWeight(replacementNode, outgoingTarget, newEdgeWeight);
              });

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

        return cliques.stream()
                 .peek(cycle -> {
                     final List<INode> intersectingNodes = cycle.stream().filter(replacedClique::contains).collect(Collectors.toList());
                     if (!intersectingNodes.isEmpty()) {
                         cycle.removeAll(intersectingNodes);
                         cycle.add(replacementNode);
                     }
                 })
                 .filter(cycle -> cycle.size() > 1)
                 .sorted(Comparator.comparing(Set::size))
                 .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @SuppressWarnings("unchecked")
    public CliqueDetectionGraph buildDetectionGraph(final G graph)
    {
        final CliqueDetectionGraph target = new CliqueDetectionGraph();
        graph.vertexSet()
          .stream()
          .filter(IRecipeNode.class::isInstance)
          .map(IRecipeNode.class::cast)
          .filter(r -> graph.incomingEdgesOf(r).size() == 1)
          .filter(r -> graph.outgoingEdgesOf(r).size() == 1)
          .filter(r -> graph.outgoingEdgesOf(r).iterator().next().getWeight() == graph.incomingEdgesOf(r).iterator().next().getWeight())
          .forEach(recipeNode -> {
              final Set<IContainerNode> inputs = graph.incomingEdgesOf(
                graph.getEdgeSource(
                  graph.incomingEdgesOf(recipeNode).iterator().next()
                )
              ).stream().map(graph::getEdgeSource)
                                                   .map(IContainerNode.class::cast)
                                                   .collect(Collectors.toSet());

              final IContainerNode output = (IContainerNode) graph.getEdgeTarget(graph.outgoingEdgesOf(recipeNode).iterator().next());

              inputs.forEach(input -> {
                  if (!target.containsVertex(input))
                  {
                      target.addVertex(input);
                  }

                  if (!target.containsVertex(output))
                  {
                      target.addVertex(output);
                  }

                  if (target.containsEdge(input, output)) {
                      target.getEdge(input, output).getRecipeNodes().add(recipeNode);
                  }
                  else
                  {
                      target.addEdge(input, output, new CliqueDetectionEdge(recipeNode));
                  }
              });
          });

        return target;
    }
}
