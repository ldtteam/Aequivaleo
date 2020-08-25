package com.ldtteam.aequivaleo.analyzer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.analyzer.jgrapht.ContainerWrapperGraphNode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.IAnalysisGraphNode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.RecipeGraphNode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.SourceGraphNode;
import com.ldtteam.aequivaleo.api.compound.ICompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.util.AequivaleoLogger;
import com.ldtteam.aequivaleo.compound.information.LockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.compound.simple.SimpleCompoundInstance;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryRegistry;
import com.ldtteam.aequivaleo.compound.information.ValidCompoundTypeInformationProviderRegistry;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class JGraphTBasedCompoundAnalyzer
{

    private static final Object ANALYSIS_LOCK = new Object();

    private final World world;

    private Map<ICompoundContainer<?>, Set<ICompoundInstance>> results = Collections.emptyMap();

    public JGraphTBasedCompoundAnalyzer(final World world) {this.world = world;}

    public void calculate()
    {
        final Map<ICompoundContainer<?>, Set<ICompoundInstance>> resultingCompounds = new ConcurrentSkipListMap<>();

        final Graph<IAnalysisGraphNode, DefaultWeightedEdge> recipeGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        final Map<ICompoundContainer<?>, IAnalysisGraphNode> nodes = Maps.newConcurrentMap();

        EquivalencyRecipeRegistry.getInstance(world.func_234923_W_())
          .get()
          .forEach(recipe -> {
              final IAnalysisGraphNode recipeGraphNode = new RecipeGraphNode(recipe);

              recipeGraph.addVertex(recipeGraphNode);

              //Process inputs
              recipe.getInputs().forEach(input -> {
                  final ICompoundContainer<?> unitInputWrapper = createUnitWrapper(input);
                  nodes.putIfAbsent(unitInputWrapper, new ContainerWrapperGraphNode(unitInputWrapper));

                  final IAnalysisGraphNode inputWrapperGraphNode = nodes.get(unitInputWrapper);

                  resultingCompounds.putIfAbsent(unitInputWrapper, new ConcurrentSkipListSet<>());
                  recipeGraph.addVertex(inputWrapperGraphNode);

                  recipeGraph.addEdge(inputWrapperGraphNode, recipeGraphNode);
                  recipeGraph.setEdgeWeight(inputWrapperGraphNode, recipeGraphNode, input.getContentsCount());
              });

              //Process outputs
              recipe.getOutputs().forEach(output -> {
                  final ICompoundContainer<?> unitOutputWrapper = createUnitWrapper(output);
                  nodes.putIfAbsent(unitOutputWrapper, new ContainerWrapperGraphNode(unitOutputWrapper));

                  final IAnalysisGraphNode outputWrapperGraphNode = nodes.get(unitOutputWrapper);

                  resultingCompounds.putIfAbsent(unitOutputWrapper, new ConcurrentSkipListSet<>());
                  recipeGraph.addVertex(outputWrapperGraphNode);

                  recipeGraph.addEdge(recipeGraphNode, outputWrapperGraphNode);
                  recipeGraph.setEdgeWeight(recipeGraphNode, outputWrapperGraphNode, output.getContentsCount());
              });
          });

        LockedCompoundInformationRegistry.getInstance(world.func_234923_W_()).get().keySet().forEach(lockedWrapper -> {
            if (!recipeGraph.containsVertex(new ContainerWrapperGraphNode(lockedWrapper)))
            {
                nodes.putIfAbsent(lockedWrapper, new ContainerWrapperGraphNode(lockedWrapper));

                final IAnalysisGraphNode inputWrapperGraphNode = nodes.get(lockedWrapper);

                resultingCompounds.putIfAbsent(lockedWrapper, new ConcurrentSkipListSet<>());
                recipeGraph.addVertex(inputWrapperGraphNode);
            }
            else
            {
                final Set<DefaultWeightedEdge> incomingEdgesToRemove = new HashSet<>(recipeGraph.incomingEdgesOf(new ContainerWrapperGraphNode(lockedWrapper)));
                recipeGraph.removeAllEdges(incomingEdgesToRemove);
            }

        });

        final Set<ContainerWrapperGraphNode> rootNodes = findRootNodes(recipeGraph);

        final Set<ContainerWrapperGraphNode> notDefinedGraphNodes = rootNodes
          .stream()
          .filter(n -> getLockedInformationInstances(n.getWrapper()).isEmpty())
          .collect(Collectors.toSet());

        rootNodes.removeAll(notDefinedGraphNodes);

        removeDanglingNodes(recipeGraph, rootNodes);

        final SourceGraphNode source = new SourceGraphNode();
        recipeGraph.addVertex(source);

        rootNodes
          .forEach(rootNode -> {
              recipeGraph.addEdge(source, rootNode);
              recipeGraph.setEdgeWeight(source, rootNode, 1d);
          });

        processRecipeGraphUsingBreathFirstSearch(recipeGraph);

        recipeGraph
          .vertexSet()
          .stream()
          .filter(v -> v instanceof ContainerWrapperGraphNode)
          .map(v-> (ContainerWrapperGraphNode) v)
          .forEach(
            v -> {
                //We could not find any information on this, possibly due to it being in a different set,
                //Or it is not producible. Register it as a not defined graph node.
                if (v.getCompoundInstances().size() == 0)
                {
                    notDefinedGraphNodes.add(v);
                }
                else
                {
                    if (!resultingCompounds.containsKey(v.getWrapper()))
                        resultingCompounds.putIfAbsent(v.getWrapper(), new ConcurrentSkipListSet<>());

                    resultingCompounds.get(v.getWrapper()).addAll(v.getCompoundInstances());
                }
            }
          );

        synchronized (ANALYSIS_LOCK)
        {
            AequivaleoLogger.startBigWarning("WARNING: Missing root equivalency data in world: " + getWorld().func_234923_W_().func_240901_a_());
            notDefinedGraphNodes
              .forEach(node -> {
                  AequivaleoLogger.bigWarningMessage(String.format("Missing root information for: %s. Removing from recipe graph.", node.getWrapper()));
                  recipeGraph.removeVertex(node);
              });
            AequivaleoLogger.endBigWarning("WARNING: Missing root equivalency data in world: " + getWorld().func_234923_W_().func_240901_a_());

            AequivaleoLogger.startBigWarning("RESULT: Compound analysis for world: " + getWorld().func_234923_W_().func_240901_a_());
            resultingCompounds.forEach((wrapper, compounds) -> {
                if (!compounds.isEmpty())
                    AequivaleoLogger.bigWarningMessage("{}: {}", wrapper, compounds);
            });
            AequivaleoLogger.endBigWarning("RESULT: Compound analysis for world: " + getWorld().func_234923_W_().func_240901_a_());
        }

        this.results = resultingCompounds;
    }

    public Map<ICompoundContainer<?>, Set<ICompoundInstance>> calculateAndGet() {
        calculate();
        return results;
    }

    private void removeDanglingNodes(@NotNull final Graph<IAnalysisGraphNode, DefaultWeightedEdge> recipeGraph,
      @NotNull final Set<ContainerWrapperGraphNode> rootNodes)
    {
        @NotNull Set<IAnalysisGraphNode> danglingNodesToDelete = findDanglingNodes(recipeGraph)
          .stream()
          .filter(n -> !rootNodes.contains(n))
          .collect(Collectors.toSet());

        while(!danglingNodesToDelete.isEmpty())
        {
            danglingNodesToDelete
              .forEach(recipeGraph::removeVertex);

            danglingNodesToDelete = findDanglingNodes(recipeGraph)
             .stream()
             .filter(n -> !rootNodes.contains(n))
             .collect(Collectors.toSet());
        }
    }

    private void processRecipeGraphUsingBreathFirstSearch(@NotNull final Graph<IAnalysisGraphNode, DefaultWeightedEdge> recipeGraph)
    {
        final LinkedHashSet<IAnalysisGraphNode> processingQueue = new LinkedHashSet<>();
        processingQueue.add(new SourceGraphNode());

        processRecipeGraphFromNodeUsingBreathFirstSearch(recipeGraph, processingQueue);
    }

    private void processRecipeGraphFromNodeUsingBreathFirstSearch(@NotNull final Graph<IAnalysisGraphNode, DefaultWeightedEdge> recipeGraph, final LinkedHashSet<IAnalysisGraphNode> processingQueue)
    {
        final Set<IAnalysisGraphNode> visitedNodes = new LinkedHashSet<>();

        while(!processingQueue.isEmpty())
        {
            final Iterator<IAnalysisGraphNode> nodeIterator = processingQueue.iterator();
            final IAnalysisGraphNode node = nodeIterator.next();
            nodeIterator.remove();

            processRecipeGraphForNodeWithBFS(recipeGraph, processingQueue, visitedNodes, node);
        }
    }

    private void processRecipeGraphForNodeWithBFS(@NotNull final Graph<IAnalysisGraphNode, DefaultWeightedEdge> recipeGraph, final LinkedHashSet<IAnalysisGraphNode> processingQueue, final Set<IAnalysisGraphNode> visitedNodes, final IAnalysisGraphNode node)
    {
        visitedNodes.add(node);
        final Class<? extends IAnalysisGraphNode> clazz = node.getClass();

        Set<IAnalysisGraphNode> nextIterationNodes = Sets.newHashSet();

        AequivaleoLogger.fine(String.format("Processing node: %s", node));

        if(clazz == SourceGraphNode.class)
        {
            final Set<ContainerWrapperGraphNode> neighbors = recipeGraph
                                                               .outgoingEdgesOf(node)
                                                               .stream()
                                                               .map(recipeGraph::getEdgeTarget)
                                                               .filter(v -> v instanceof ContainerWrapperGraphNode)
                                                               .map(v -> (ContainerWrapperGraphNode) v)
                                                               .collect(Collectors.toSet());

            neighbors.forEach(rootNode -> {
                //This root node should have embedded information.
                final ICompoundContainer<?> unitWrapper = rootNode.getWrapper();
                rootNode.getCompoundInstances().clear();
                rootNode.getCompoundInstances().addAll(getLockedInformationInstances(unitWrapper));
            });

            nextIterationNodes = new HashSet<>(neighbors);
        }
        if (clazz == ContainerWrapperGraphNode.class)
        {
            final Set<RecipeGraphNode> neighbors = recipeGraph
                                                     .outgoingEdgesOf(node)
                                                     .stream()
                                                     .map(recipeGraph::getEdgeTarget)
                                                     .filter(v -> v instanceof RecipeGraphNode)
                                                     .map(v -> (RecipeGraphNode) v)
                                                     .collect(Collectors.toSet());

            neighbors.forEach(neighbor -> neighbor.getAnalyzedInputNodes().add(node));

            nextIterationNodes = neighbors
                                   .stream()
                                   .filter(recipeGraphNode -> recipeGraphNode.getAnalyzedInputNodes().size() == recipeGraphNode.getRecipe().getInputs().size())
                                   .collect(Collectors.toSet());
        }
        if (clazz == RecipeGraphNode.class)
        {
            final Set<ContainerWrapperGraphNode> resultNeighbors = recipeGraph
                                                                     .outgoingEdgesOf(node)
                                                                     .stream()
                                                                     .map(recipeGraph::getEdgeTarget)
                                                                     .filter(v -> v instanceof ContainerWrapperGraphNode)
                                                                     .map(v -> (ContainerWrapperGraphNode) v)
                                                                     .collect(Collectors.toSet());

            final Set<ContainerWrapperGraphNode> inputNeightbors = recipeGraph
                                                                     .incomingEdgesOf(node)
                                                                     .stream()
                                                                     .map(recipeGraph::getEdgeSource)
                                                                     .filter(v -> v instanceof ContainerWrapperGraphNode)
                                                                     .map(v -> (ContainerWrapperGraphNode) v)
                                                                     .collect(Collectors.toSet());

            final Set<ICompoundInstance> summedCompoundInstances = inputNeightbors
                                                                     .stream()
                                                                     .flatMap(inputNeighbor-> inputNeighbor
                                                                                                .getCompoundInstances()
                                                                                                .stream()
                                                                                                .map(compoundInstance -> new HashMap.SimpleEntry<>(compoundInstance.getType(), compoundInstance.getAmount() * recipeGraph.getEdgeWeight(recipeGraph.getEdge(inputNeighbor, node)))))
                                                                     .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, (val1, val2)-> val1 + val2))
                                                                     .entrySet()
                                                                     .stream()
                                                                     .map(entry -> new SimpleCompoundInstance(entry.getKey(), entry.getValue()))
                                                                     .collect(Collectors.toSet());

            final Set<IAnalysisGraphNode> nextNodes = new HashSet<>();

            resultNeighbors
              .forEach(neighbor -> {
                  //As of now. We do not update once calculated.
                  if (!neighbor.getCompoundInstances().isEmpty())
                      return;

                  final Set<ICompoundInstance> compoundInstances = getLockedInformationInstances(neighbor.getWrapper());

                  if (compoundInstances.isEmpty())
                  {
                      neighbor
                        .getCompoundInstances()
                        .addAll(
                          summedCompoundInstances
                            .stream()
                            .filter(compoundInstance -> ValidCompoundTypeInformationProviderRegistry.getInstance(world.func_234923_W_()).isCompoundTypeValidForWrapper(neighbor.getWrapper(), compoundInstance.getType()))
                            .map(compoundInstance -> new SimpleCompoundInstance(compoundInstance.getType(), Math.floor(compoundInstance.getAmount() / recipeGraph.getEdgeWeight(recipeGraph.getEdge(node, neighbor)))))
                            .filter(simpleCompoundInstance -> simpleCompoundInstance.getAmount() > 0)
                            .filter(simpleCompoundInstance -> ValidCompoundTypeInformationProviderRegistry.getInstance(world.func_234923_W_()).isCompoundTypeValidForWrapper(neighbor.getWrapper(), simpleCompoundInstance.getType()))
                            .collect(Collectors.toSet())
                        );
                  }
                  else
                  {
                      neighbor
                        .getCompoundInstances()
                        .addAll(compoundInstances);
                  }

                  nextNodes.add(neighbor);
              });

            nextIterationNodes = nextNodes;
        }

        nextIterationNodes.removeIf(visitedNodes::contains);
        processingQueue.addAll(nextIterationNodes);
    }

    private ICompoundContainer<?> createUnitWrapper(@NotNull final ICompoundContainer<?> wrapper)
    {
        if (wrapper.getContentsCount() == 1d)
            return wrapper;

        return CompoundContainerFactoryRegistry.getInstance().wrapInContainer(wrapper.getContents(), 1d);
    }

    private Set<ContainerWrapperGraphNode> findRootNodes(@NotNull final Graph<IAnalysisGraphNode, DefaultWeightedEdge> graph)
    {
        return findDanglingNodes(graph)
          .stream()
          .filter(v -> v instanceof ContainerWrapperGraphNode)
          .map(v -> (ContainerWrapperGraphNode) v)
          .collect(Collectors.toSet());
    }

    private Set<IAnalysisGraphNode> findDanglingNodes(@NotNull final Graph<IAnalysisGraphNode, DefaultWeightedEdge> graph)
    {
        return graph
                 .vertexSet()
                 .stream()
                 .filter(v -> graph.incomingEdgesOf(v).isEmpty())
                 .collect(Collectors.toSet());
    }

    private Set<ICompoundInstance> getLockedInformationInstances(@NotNull final ICompoundContainer<?> wrapper)
    {
        final Set<ICompoundInstance> lockedInstances = LockedCompoundInformationRegistry.getInstance(world.func_234923_W_())
                                                         .get()
                                                         .get(createUnitWrapper(wrapper));

        if (lockedInstances != null)
            return lockedInstances;

        return new HashSet<>();
    }

    public World getWorld()
    {
        return world;
    }

    public Map<ICompoundContainer<?>, Set<ICompoundInstance>> getResults()
    {
        return results;
    }
}
