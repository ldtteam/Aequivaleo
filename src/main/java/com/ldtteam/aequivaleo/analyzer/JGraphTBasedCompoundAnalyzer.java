package com.ldtteam.aequivaleo.analyzer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.analyzer.debug.GraphIOHandler;
import com.ldtteam.aequivaleo.analyzer.jgrapht.*;
import com.ldtteam.aequivaleo.api.AequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.util.AequivaleoLogger;
import com.ldtteam.aequivaleo.compound.information.contribution.ContributionInformationProviderRegistry;
import com.ldtteam.aequivaleo.compound.information.locked.LockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.information.validity.ValidCompoundTypeInformationProviderRegistry;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class JGraphTBasedCompoundAnalyzer
{

    private static final Object ANALYSIS_LOCK = new Object();

    private final World world;

    private Map<ICompoundContainer<?>, Set<CompoundInstance>> results = Collections.emptyMap();

    public JGraphTBasedCompoundAnalyzer(final World world) {this.world = world;}

    public void calculate()
    {
        final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds = new ConcurrentSkipListMap<>();

        final Graph<IAnalysisGraphNode, AccessibleWeightEdge> recipeGraph = new DefaultDirectedWeightedGraph<>(AccessibleWeightEdge.class);

        final Map<ICompoundContainer<?>, IAnalysisGraphNode> nodes = Maps.newConcurrentMap();

        for (IEquivalencyRecipe recipe : EquivalencyRecipeRegistry.getInstance(world.func_234923_W_())
                                           .get())
        {
            final IAnalysisGraphNode recipeGraphNode = new RecipeGraphNode(recipe);

            recipeGraph.addVertex(recipeGraphNode);

            //Process inputs
            for (ICompoundContainer<?> input : recipe.getInputs())
            {
                final ICompoundContainer<?> unitInputWrapper = createUnitWrapper(input);
                nodes.putIfAbsent(unitInputWrapper, new ContainerWrapperGraphNode(unitInputWrapper));

                final IAnalysisGraphNode inputWrapperGraphNode = nodes.get(unitInputWrapper);

                resultingCompounds.putIfAbsent(unitInputWrapper, new ConcurrentSkipListSet<>());
                recipeGraph.addVertex(inputWrapperGraphNode);

                recipeGraph.addEdge(inputWrapperGraphNode, recipeGraphNode);
                recipeGraph.setEdgeWeight(inputWrapperGraphNode, recipeGraphNode, input.getContentsCount());
            }

            //Process outputs
            for (ICompoundContainer<?> output : recipe.getOutputs())
            {
                final ICompoundContainer<?> unitOutputWrapper = createUnitWrapper(output);
                nodes.putIfAbsent(unitOutputWrapper, new ContainerWrapperGraphNode(unitOutputWrapper));

                final IAnalysisGraphNode outputWrapperGraphNode = nodes.get(unitOutputWrapper);

                resultingCompounds.putIfAbsent(unitOutputWrapper, new ConcurrentSkipListSet<>());
                recipeGraph.addVertex(outputWrapperGraphNode);

                recipeGraph.addEdge(recipeGraphNode, outputWrapperGraphNode);
                recipeGraph.setEdgeWeight(recipeGraphNode, outputWrapperGraphNode, output.getContentsCount());
            }
        }

        for (ICompoundContainer<?> lockedWrapper : LockedCompoundInformationRegistry.getInstance(world.func_234923_W_()).get().keySet())
        {
            if (!recipeGraph.containsVertex(new ContainerWrapperGraphNode(lockedWrapper)))
            {
                nodes.putIfAbsent(lockedWrapper, new ContainerWrapperGraphNode(lockedWrapper));

                final IAnalysisGraphNode inputWrapperGraphNode = nodes.get(lockedWrapper);

                resultingCompounds.putIfAbsent(lockedWrapper, new ConcurrentSkipListSet<>());
                recipeGraph.addVertex(inputWrapperGraphNode);
            }
            else
            {
                final Set<AccessibleWeightEdge> incomingEdgesToRemove = new HashSet<>(recipeGraph.incomingEdgesOf(new ContainerWrapperGraphNode(lockedWrapper)));
                recipeGraph.removeAllEdges(incomingEdgesToRemove);
            }
        }

        if (Aequivaleo.getInstance().getConfiguration().getServer().exportGraph.get())
            GraphIOHandler.getInstance().export(world.func_234923_W_().func_240901_a_().toString().replace(":", "_").concat(".json"), recipeGraph);

        final Set<ContainerWrapperGraphNode> rootNodes = findRootNodes(recipeGraph);

        final Set<ContainerWrapperGraphNode> notDefinedGraphNodes = new HashSet<>();
        for (ContainerWrapperGraphNode n : rootNodes)
        {
            if (getLockedInformationInstances(n.getWrapper()).isEmpty())
            {
                notDefinedGraphNodes.add(n);
            }
        }

        rootNodes.removeAll(notDefinedGraphNodes);

        removeDanglingNodes(recipeGraph, rootNodes);

        final SourceGraphNode source = new SourceGraphNode();
        recipeGraph.addVertex(source);

        for (ContainerWrapperGraphNode rootNode : rootNodes)
        {
            recipeGraph.addEdge(source, rootNode);
            recipeGraph.setEdgeWeight(source, rootNode, 1d);
        }

        processRecipeGraphUsingBreathFirstSearch(recipeGraph);

        for (IAnalysisGraphNode v : recipeGraph
                                      .vertexSet())
        {
            if (v instanceof ContainerWrapperGraphNode)
            {
                ContainerWrapperGraphNode containerWrapperGraphNode = (ContainerWrapperGraphNode) v;
//We could not find any information on this, possibly due to it being in a different set,
                //Or it is not producible. Register it as a not defined graph node.
                if (containerWrapperGraphNode.getCompoundInstances().size() == 0)
                {
                    notDefinedGraphNodes.add(containerWrapperGraphNode);
                }
                else
                {
                    if (!resultingCompounds.containsKey(containerWrapperGraphNode.getWrapper()))
                        resultingCompounds.putIfAbsent(containerWrapperGraphNode.getWrapper(), new ConcurrentSkipListSet<>());

                    resultingCompounds.get(containerWrapperGraphNode.getWrapper()).addAll(containerWrapperGraphNode.getCompoundInstances());
                }
            }
        }

        if (Aequivaleo.getInstance().getConfiguration().getServer().writeResultsToLog.get()) {
            synchronized (ANALYSIS_LOCK)
            {
                AequivaleoLogger.startBigWarning(String.format("WARNING: Missing root equivalency data in world: %s", getWorld().func_234923_W_().func_240901_a_()));
                for (ContainerWrapperGraphNode node : notDefinedGraphNodes)
                {
                    AequivaleoLogger.bigWarningMessage(String.format("Missing root information for: %s. Removing from recipe graph.", node.getWrapper()));
                    recipeGraph.removeVertex(node);
                }
                AequivaleoLogger.endBigWarning(String.format("WARNING: Missing root equivalency data in world: %s", getWorld().func_234923_W_().func_240901_a_()));

                AequivaleoLogger.startBigWarning(String.format("RESULT: Compound analysis for world: %s", getWorld().func_234923_W_().func_240901_a_()));
                for (Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>> entry : resultingCompounds.entrySet())
                {
                    ICompoundContainer<?> wrapper = entry.getKey();
                    Set<CompoundInstance> compounds = entry.getValue();
                    if (!compounds.isEmpty())
                        AequivaleoLogger.bigWarningMessage("{}: {}", wrapper, compounds);
                }
                AequivaleoLogger.endBigWarning(String.format("RESULT: Compound analysis for world: %s", getWorld().func_234923_W_().func_240901_a_()));
            }
        } else {
            AequivaleoLogger.bigWarningSimple(String.format("Finished the analysis of: %s", getWorld().func_234923_W_().func_240901_a_()));
        }

        this.results = resultingCompounds;
    }

    public Map<ICompoundContainer<?>, Set<CompoundInstance>> calculateAndGet() {
        calculate();
        return results;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private void removeDanglingNodes(@NotNull final Graph<IAnalysisGraphNode, AccessibleWeightEdge> recipeGraph,
      @NotNull final Set<ContainerWrapperGraphNode> rootNodes)
    {
        @NotNull Set<IAnalysisGraphNode> danglingNodesToDelete = new HashSet<>();
        for (IAnalysisGraphNode iAnalysisGraphNode : findDanglingNodes(recipeGraph))
        {
            if (!rootNodes.contains(iAnalysisGraphNode))
            {
                danglingNodesToDelete.add(iAnalysisGraphNode);
            }
        }

        while(!danglingNodesToDelete.isEmpty())
        {
            for (IAnalysisGraphNode iAnalysisGraphNode : danglingNodesToDelete)
            {
                recipeGraph.removeVertex(iAnalysisGraphNode);
            }

            Set<IAnalysisGraphNode> set = new HashSet<>();
            for (IAnalysisGraphNode n : findDanglingNodes(recipeGraph))
            {
                if (!rootNodes.contains(n))
                {
                    set.add(n);
                }
            }
            danglingNodesToDelete = set;
        }
    }

    private void processRecipeGraphUsingBreathFirstSearch(@NotNull final Graph<IAnalysisGraphNode, AccessibleWeightEdge> recipeGraph)
    {
        final LinkedHashSet<IAnalysisGraphNode> processingQueue = new LinkedHashSet<>();
        processingQueue.add(new SourceGraphNode());

        processRecipeGraphFromNodeUsingBreathFirstSearch(recipeGraph, processingQueue);
    }

    private void processRecipeGraphFromNodeUsingBreathFirstSearch(@NotNull final Graph<IAnalysisGraphNode, AccessibleWeightEdge> recipeGraph, final LinkedHashSet<IAnalysisGraphNode> processingQueue)
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

    private void processRecipeGraphForNodeWithBFS(@NotNull final Graph<IAnalysisGraphNode, AccessibleWeightEdge> recipeGraph, final LinkedHashSet<IAnalysisGraphNode> processingQueue, final Set<IAnalysisGraphNode> visitedNodes, final IAnalysisGraphNode node)
    {
        visitedNodes.add(node);
        final Class<? extends IAnalysisGraphNode> clazz = node.getClass();

        Set<IAnalysisGraphNode> nextIterationNodes = Sets.newHashSet();

        AequivaleoLogger.fine(String.format("Processing node: %s", node));

        if(clazz == SourceGraphNode.class)
        {
            final Set<ContainerWrapperGraphNode> neighbors = new HashSet<>();
            for (AccessibleWeightEdge accessibleWeightEdge : recipeGraph
                                                               .outgoingEdgesOf(node))
            {
                IAnalysisGraphNode v = recipeGraph.getEdgeTarget(accessibleWeightEdge);
                if (v instanceof ContainerWrapperGraphNode)
                {
                    ContainerWrapperGraphNode containerWrapperGraphNode = (ContainerWrapperGraphNode) v;
                    neighbors.add(containerWrapperGraphNode);
                }
            }

            for (ContainerWrapperGraphNode rootNode : neighbors)
            {//This root node should have embedded information.
                final ICompoundContainer<?> unitWrapper = rootNode.getWrapper();
                rootNode.getCompoundInstances().clear();
                rootNode.getCompoundInstances().addAll(getLockedInformationInstances(unitWrapper));
            }

            nextIterationNodes = new HashSet<>(neighbors);
        }
        if (clazz == ContainerWrapperGraphNode.class)
        {
            final Set<RecipeGraphNode> neighbors = new HashSet<>();
            for (AccessibleWeightEdge accessibleWeightEdge : recipeGraph
                                                               .outgoingEdgesOf(node))
            {
                IAnalysisGraphNode v = recipeGraph.getEdgeTarget(accessibleWeightEdge);
                if (v instanceof RecipeGraphNode)
                {
                    RecipeGraphNode graphNode = (RecipeGraphNode) v;
                    neighbors.add(graphNode);
                }
            }

            for (RecipeGraphNode neighbor : neighbors)
            {
                neighbor.getAnalyzedInputNodes().add(node);
            }

            Set<IAnalysisGraphNode> set = new HashSet<>();
            for (RecipeGraphNode recipeGraphNode : neighbors)
            {
                if (recipeGraphNode.getAnalyzedInputNodes().size() == recipeGraphNode.getRecipe().getInputs().size())
                {
                    set.add(recipeGraphNode);
                }
            }
            nextIterationNodes = set;
        }
        if (clazz == RecipeGraphNode.class)
        {
            final RecipeGraphNode recipeGraphNode = (RecipeGraphNode) node;
            final Set<ContainerWrapperGraphNode> resultNeighbors = new HashSet<>();
            for (AccessibleWeightEdge weightEdge : recipeGraph
                                                     .outgoingEdgesOf(node))
            {
                IAnalysisGraphNode edgeTarget = recipeGraph.getEdgeTarget(weightEdge);
                if (edgeTarget instanceof ContainerWrapperGraphNode)
                {
                    ContainerWrapperGraphNode wrapperGraphNode = (ContainerWrapperGraphNode) edgeTarget;
                    resultNeighbors.add(wrapperGraphNode);
                }
            }

            final Set<ContainerWrapperGraphNode> inputNeightbors = new HashSet<>();
            for (AccessibleWeightEdge accessibleWeightEdge : recipeGraph
                                                               .incomingEdgesOf(node))
            {
                IAnalysisGraphNode v = recipeGraph.getEdgeSource(accessibleWeightEdge);
                if (v instanceof ContainerWrapperGraphNode)
                {
                    ContainerWrapperGraphNode containerWrapperGraphNode = (ContainerWrapperGraphNode) v;
                    inputNeightbors.add(containerWrapperGraphNode);
                }
            }

            final Set<CompoundInstance> summedCompoundInstances = new HashSet<>();
            for (Map.Entry<ICompoundType, Double> entry : inputNeightbors
                                                            .stream()
                                                            .flatMap(inputNeighbor -> inputNeighbor
                                                                                        .getCompoundInstances()
                                                                                        .stream()
                                                                                        .filter(compoundInstance -> ContributionInformationProviderRegistry.getInstance(world.func_234923_W_())
                                                                                                                      .canCompoundTypeContributeAsInput(inputNeighbor.getWrapper(),
                                                                                                                        recipeGraphNode.getRecipe(), compoundInstance.getType()))
                                                                                        .map(compoundInstance -> new HashMap.SimpleEntry<>(compoundInstance.getType(),
                                                                                          compoundInstance.getAmount()
                                                                                            * recipeGraph.getEdgeWeight(recipeGraph.getEdge(inputNeighbor, node)))))
                                                            .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, Double::sum))
                                                            .entrySet())
            {
                CompoundInstance instance = new CompoundInstance(entry.getKey(), entry.getValue());
                summedCompoundInstances.add(instance);
            }

            final Set<IAnalysisGraphNode> nextNodes = new HashSet<>();

            for (ContainerWrapperGraphNode neighbor : resultNeighbors)
            {//As of now. We do not update once calculated.
                if (!neighbor.getCompoundInstances().isEmpty())
                    continue;

                final Set<CompoundInstance> compoundInstances = getLockedInformationInstances(neighbor.getWrapper());

                if (compoundInstances.isEmpty())
                {
                    Set<CompoundInstance> set = new HashSet<>();
                    for (CompoundInstance compoundInstance : summedCompoundInstances)
                    {
                        CompoundInstance simpleCompoundInstance = new CompoundInstance(compoundInstance.getType(),
                          Math.floor(compoundInstance.getAmount() / recipeGraph.getEdgeWeight(recipeGraph.getEdge(node, neighbor))));
                        if (simpleCompoundInstance.getAmount() > 0)
                        {
                            if (ValidCompoundTypeInformationProviderRegistry.getInstance(world.func_234923_W_())
                                  .isCompoundTypeValidForWrapper(neighbor.getWrapper(), simpleCompoundInstance.getType()))
                            {
                                if (ContributionInformationProviderRegistry.getInstance(world.func_234923_W_())
                                      .canCompoundTypeContributeAsOutput(neighbor.getWrapper(), recipeGraphNode.getRecipe(),
                                        simpleCompoundInstance.getType()))
                                {
                                    set.add(simpleCompoundInstance);
                                }
                            }
                        }
                    }
                    neighbor
                      .getCompoundInstances()
                      .addAll(
                        set
                      );
                }
                else
                {
                    neighbor
                      .getCompoundInstances()
                      .addAll(compoundInstances);
                }

                nextNodes.add(neighbor);
            }

            nextIterationNodes = nextNodes;
        }

        nextIterationNodes.removeIf(visitedNodes::contains);
        processingQueue.addAll(nextIterationNodes);
    }

    private ICompoundContainer<?> createUnitWrapper(@NotNull final ICompoundContainer<?> wrapper)
    {
        if (wrapper.getContentsCount() == 1d)
            return wrapper;

        return CompoundContainerFactoryManager.getInstance().wrapInContainer(wrapper.getContents(), 1d);
    }

    private Set<ContainerWrapperGraphNode> findRootNodes(@NotNull final Graph<IAnalysisGraphNode, AccessibleWeightEdge> graph)
    {
        Set<ContainerWrapperGraphNode> set = new HashSet<>();
        for (IAnalysisGraphNode v : findDanglingNodes(graph))
        {
            if (v instanceof ContainerWrapperGraphNode)
            {
                ContainerWrapperGraphNode containerWrapperGraphNode = (ContainerWrapperGraphNode) v;
                set.add(containerWrapperGraphNode);
            }
        }
        return set;
    }

    private Set<IAnalysisGraphNode> findDanglingNodes(@NotNull final Graph<IAnalysisGraphNode, AccessibleWeightEdge> graph)
    {
        Set<IAnalysisGraphNode> set = new HashSet<>();
        for (IAnalysisGraphNode v : graph
                                      .vertexSet())
        {
            if (graph.incomingEdgesOf(v).isEmpty())
            {
                set.add(v);
            }
        }
        return set;
    }

    private Set<CompoundInstance> getLockedInformationInstances(@NotNull final ICompoundContainer<?> wrapper)
    {
        final Set<CompoundInstance> lockedInstances = LockedCompoundInformationRegistry.getInstance(world.func_234923_W_())
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

    public Map<ICompoundContainer<?>, Set<CompoundInstance>> getResults()
    {
        return results;
    }
}
