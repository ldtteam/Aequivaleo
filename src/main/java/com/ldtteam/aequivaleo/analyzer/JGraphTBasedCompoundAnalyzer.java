package com.ldtteam.aequivaleo.analyzer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.analyzer.debug.GraphIOHandler;
import com.ldtteam.aequivaleo.analyzer.jgrapht.*;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.util.AequivaleoLogger;
import com.ldtteam.aequivaleo.compound.information.locked.LockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class JGraphTBasedCompoundAnalyzer
{

    private static final Object ANALYSIS_LOCK = new Object();

    private final World world;

    private Map<ICompoundContainer<?>, Set<CompoundInstance>> results = new TreeMap<>();

    public JGraphTBasedCompoundAnalyzer(final World world) {this.world = world;}

    public void calculate()
    {
        final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds = new TreeMap<>();

        final Graph<IAnalysisGraphNode, AccessibleWeightEdge> recipeGraph = new DefaultDirectedWeightedGraph<>(AccessibleWeightEdge.class);

        final Map<ICompoundContainer<?>, IAnalysisGraphNode> compoundNodes = new TreeMap<>();
        final Map<IRecipeIngredient, IAnalysisGraphNode> ingredientNodes = new TreeMap<>();

        for (IEquivalencyRecipe recipe : EquivalencyRecipeRegistry.getInstance(world.func_234923_W_())
                                           .get())
        {
            final IAnalysisGraphNode recipeGraphNode = new RecipeGraphNode(recipe);

            recipeGraph.addVertex(recipeGraphNode);

            //Process inputs
            for (IRecipeIngredient input : recipe.getInputs())
            {
                final IRecipeIngredient unitIngredient = new SimpleIngredientBuilder().from(input).withCount(1).createIngredient();
                ingredientNodes.putIfAbsent(unitIngredient, new IngredientCandidateGraphNode(unitIngredient));

                final IAnalysisGraphNode inputNode = ingredientNodes.get(unitIngredient);
                recipeGraph.addVertex(inputNode);

                recipeGraph.addEdge(inputNode, recipeGraphNode);
                recipeGraph.setEdgeWeight(inputNode, recipeGraphNode, input.getRequiredCount());

                for (final ICompoundContainer<?> candidate : input.getCandidates())
                {
                    handleCompoundContainerAsInput(resultingCompounds, recipeGraph, compoundNodes, inputNode, candidate);
                }
            }

            //Process outputs
            for (ICompoundContainer<?> output : recipe.getRequiredKnownOutputs())
            {
                handleCompoundContainerAsInput(resultingCompounds, recipeGraph, compoundNodes, recipeGraphNode, output);
            }

            //Process outputs
            for (ICompoundContainer<?> output : recipe.getOutputs())
            {
                final ICompoundContainer<?> unitOutputWrapper = createUnitWrapper(output);
                compoundNodes.putIfAbsent(unitOutputWrapper, new ContainerWrapperGraphNode(unitOutputWrapper));

                final IAnalysisGraphNode outputWrapperGraphNode = compoundNodes.get(unitOutputWrapper);

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
                compoundNodes.putIfAbsent(lockedWrapper, new ContainerWrapperGraphNode(lockedWrapper));

                final IAnalysisGraphNode inputWrapperGraphNode = compoundNodes.get(lockedWrapper);

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

        final StatCollector statCollector = new StatCollector(getWorld().func_234923_W_().func_240901_a_(), recipeGraph.vertexSet().size());
        processRecipeGraphUsingBreathFirstSearch(recipeGraph, statCollector);

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

    private void handleCompoundContainerAsInput(
      final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds,
      final Graph<IAnalysisGraphNode, AccessibleWeightEdge> recipeGraph,
      final Map<ICompoundContainer<?>, IAnalysisGraphNode> nodes,
      final IAnalysisGraphNode target,
      final ICompoundContainer<?> candidate
    )
    {
        final ICompoundContainer<?> unitWrapper = createUnitWrapper(candidate);
        nodes.putIfAbsent(unitWrapper, new ContainerWrapperGraphNode(unitWrapper));

        final IAnalysisGraphNode candidateNode = nodes.get(unitWrapper);

        resultingCompounds.putIfAbsent(unitWrapper, new ConcurrentSkipListSet<>());
        recipeGraph.addVertex(candidateNode);

        recipeGraph.addEdge(candidateNode, target);
        recipeGraph.setEdgeWeight(candidateNode, target, candidate.getContentsCount());
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

    private void processRecipeGraphUsingBreathFirstSearch(
      @NotNull final Graph<IAnalysisGraphNode, AccessibleWeightEdge> recipeGraph,
      final StatCollector statCollector)
    {
        final LinkedHashSet<IAnalysisGraphNode> processingQueue = new LinkedHashSet<>();
        processingQueue.add(new SourceGraphNode());
        final LinkedHashSet<IngredientCandidateGraphNode> noneCompleteIngredientNodes = new LinkedHashSet<>();

        processRecipeGraphFromNodeUsingBreathFirstSearch(recipeGraph, processingQueue, noneCompleteIngredientNodes, statCollector);

        final Set<Set<IngredientCandidateGraphNode>> previousIterations = new HashSet<>();

        LinkedHashSet<IngredientCandidateGraphNode> workingSet = new LinkedHashSet<>(noneCompleteIngredientNodes);
        while(!previousIterations.contains(workingSet) && !workingSet.isEmpty()) {
            previousIterations.add(workingSet);
            final IngredientCandidateGraphNode newTarget = workingSet.iterator().next();
            workingSet.remove(newTarget);

            handleIngredientNodeCompounds(
              recipeGraph,
              newTarget,
              true
            );

            if (newTarget.getCompoundInstances().isEmpty())
                continue;

            processingQueue.add(
              newTarget
            );

            processRecipeGraphFromNodeUsingBreathFirstSearch(
              recipeGraph,
              processingQueue,
              workingSet,
              statCollector
            );
        }
    }

    private void processRecipeGraphFromNodeUsingBreathFirstSearch(
      @NotNull final Graph<IAnalysisGraphNode, AccessibleWeightEdge> recipeGraph,
      final LinkedHashSet<IAnalysisGraphNode> processingQueue,
      final Set<IngredientCandidateGraphNode> noneCompleteIngredientNodes,
      final StatCollector statCollector)
    {
        final Set<IAnalysisGraphNode> visitedNodes = new LinkedHashSet<>();

        while(!processingQueue.isEmpty())
        {
            final Iterator<IAnalysisGraphNode> nodeIterator = processingQueue.iterator();
            final IAnalysisGraphNode node = nodeIterator.next();
            nodeIterator.remove();

            processRecipeGraphForNodeWithBFS(recipeGraph, processingQueue, visitedNodes, noneCompleteIngredientNodes, node, statCollector);
        }
    }

    private void processRecipeGraphForNodeWithBFS(
      @NotNull final Graph<IAnalysisGraphNode, AccessibleWeightEdge> recipeGraph,
      final LinkedHashSet<IAnalysisGraphNode> processingQueue,
      final Set<IAnalysisGraphNode> visitedNodes,
      final Set<IngredientCandidateGraphNode> noneCompleteIngredients,
      final IAnalysisGraphNode node,
      final StatCollector statCollector)
    {
        visitedNodes.add(node);
        final Class<? extends IAnalysisGraphNode> clazz = node.getClass();

        Set<IAnalysisGraphNode> nextIterationNodes = Sets.newHashSet();

        AequivaleoLogger.fine(String.format("Processing node: %s", node));

        if(clazz == SourceGraphNode.class)
        {
            statCollector.onVisitSourceNode();
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
            statCollector.onVisitCompoundNode();
            final Set<IngredientCandidateGraphNode> ingredientNeighbors = new HashSet<>();
            final Set<RecipeGraphNode> recipeNeighbors = new HashSet<>();
            for (AccessibleWeightEdge accessibleWeightEdge : recipeGraph
                                                               .outgoingEdgesOf(node))
            {
                IAnalysisGraphNode v = recipeGraph.getEdgeTarget(accessibleWeightEdge);
                if (v instanceof RecipeGraphNode)
                {
                    RecipeGraphNode graphNode = (RecipeGraphNode) v;
                    recipeNeighbors.add(graphNode);
                }
                else if (v instanceof IngredientCandidateGraphNode)
                {
                    IngredientCandidateGraphNode graphNode = (IngredientCandidateGraphNode) v;
                    ingredientNeighbors.add(graphNode);
                }
            }

            for (RecipeGraphNode neighbor : recipeNeighbors)
            {
                neighbor.getAnalyzedInputNodes().add(node);
            }

            for (IngredientCandidateGraphNode neighbor : ingredientNeighbors)
            {
                neighbor.getAnalyzedInputNodes().add(node);
            }

            Set<IAnalysisGraphNode> set = new HashSet<>();
            for (RecipeGraphNode recipeGraphNode : recipeNeighbors)
            {
                if (recipeGraphNode.getAnalyzedInputNodes().size() == recipeGraph.incomingEdgesOf(recipeGraphNode).size())
                {
                    set.add(recipeGraphNode);
                }
            }
            for (IngredientCandidateGraphNode ingredientGraphNode : ingredientNeighbors)
            {
                if (ingredientGraphNode.getAnalyzedInputNodes().size() == recipeGraph.incomingEdgesOf(ingredientGraphNode).size())
                {
                    noneCompleteIngredients.remove(ingredientGraphNode);
                    set.add(ingredientGraphNode);
                }
                else
                {
                    noneCompleteIngredients.add(ingredientGraphNode);
                }
            }

            nextIterationNodes = set;
        }
        if (clazz == IngredientCandidateGraphNode.class)
        {
            statCollector.onVisitIngredientNode();
            final IngredientCandidateGraphNode ingredientGraphNode = (IngredientCandidateGraphNode) node;
            noneCompleteIngredients.remove(ingredientGraphNode);
            handleIngredientNodeCompounds(recipeGraph, ingredientGraphNode, false);

            if (!ingredientGraphNode.getCompoundInstances().isEmpty()) {
                final Set<RecipeGraphNode> recipeNeighbors = new HashSet<>();
                for (AccessibleWeightEdge accessibleWeightEdge : recipeGraph
                                                                   .outgoingEdgesOf(node))
                {
                    IAnalysisGraphNode v = recipeGraph.getEdgeTarget(accessibleWeightEdge);
                    if (v instanceof RecipeGraphNode)
                    {
                        RecipeGraphNode graphNode = (RecipeGraphNode) v;
                        recipeNeighbors.add(graphNode);
                    }
                }

                for (RecipeGraphNode neighbor : recipeNeighbors)
                {
                    neighbor.getAnalyzedInputNodes().add(node);
                }

                Set<IAnalysisGraphNode> set = new HashSet<>();
                for (RecipeGraphNode recipeGraphNode : recipeNeighbors)
                {
                    if (recipeGraphNode.getAnalyzedInputNodes().size() == recipeGraph.incomingEdgesOf(recipeGraphNode).size())
                    {
                        set.add(recipeGraphNode);
                    }
                }

                nextIterationNodes = set;
            }
        }
        if (clazz == RecipeGraphNode.class)
        {
            statCollector.onVisitRecipeNode();
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

            final Set<ContainerWrapperGraphNode> requiredKnownOutputs = new HashSet<>();
            for (AccessibleWeightEdge accessibleWeightEdge : recipeGraph
                                                               .incomingEdgesOf(node))
            {
                IAnalysisGraphNode v = recipeGraph.getEdgeSource(accessibleWeightEdge);
                if (v instanceof ContainerWrapperGraphNode)
                {
                    ContainerWrapperGraphNode containerWrapperGraphNode = (ContainerWrapperGraphNode) v;
                    requiredKnownOutputs.add(containerWrapperGraphNode);
                }
            }

            final Set<IngredientCandidateGraphNode> inputNeightbors = new HashSet<>();
            for (AccessibleWeightEdge accessibleWeightEdge : recipeGraph
                                                               .incomingEdgesOf(node))
            {
                IAnalysisGraphNode v = recipeGraph.getEdgeSource(accessibleWeightEdge);
                if (v instanceof IngredientCandidateGraphNode)
                {
                    IngredientCandidateGraphNode ingredientGraphNode = (IngredientCandidateGraphNode) v;
                    inputNeightbors.add(ingredientGraphNode);
                }
            }

            final Set<CompoundInstance> summedCompoundInstances = new HashSet<>();
            for (Map.Entry<ICompoundType, Double> entry : inputNeightbors
                                                            .stream()
                                                            .flatMap(inputNeighbor -> inputNeighbor
                                                                                        .getCompoundInstances()
                                                                                        .stream()
                                                                                        .filter(compoundInstance -> compoundInstance.getType().getGroup().canContributeToRecipeAsInput(compoundInstance, recipeGraphNode.getRecipe()))
                                                                                        .map(compoundInstance -> new HashMap.SimpleEntry<>(compoundInstance.getType(),
                                                                                          compoundInstance.getAmount()
                                                                                            * recipeGraph.getEdgeWeight(recipeGraph.getEdge(inputNeighbor, node)))))
                                                            .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, Double::sum))
                                                            .entrySet())
            {
                double amount = entry.getValue();
                for (final ContainerWrapperGraphNode requiredKnownOutput : requiredKnownOutputs)
                {
                    for (final CompoundInstance compoundInstance : requiredKnownOutput.getCompoundInstances())
                    {
                        if (compoundInstance.getType().equals(entry.getKey()))
                            amount = Math.max(0, amount - compoundInstance.getAmount());
                    }
                }

                if (amount > 0) {
                    CompoundInstance instance = new CompoundInstance(entry.getKey(), amount);
                    summedCompoundInstances.add(instance);
                }
            }

            final Set<IAnalysisGraphNode> nextNodes = new HashSet<>();

            final double totalOutgoingEdgeWeight = recipeGraph.outgoingEdgesOf(recipeGraphNode)
              .stream()
              .mapToDouble(recipeGraph::getEdgeWeight)
              .sum();

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
                        final Double unitAmount = Math.floor(
                          compoundInstance.getAmount() / totalOutgoingEdgeWeight
                        );

                        CompoundInstance simpleCompoundInstance = new CompoundInstance(compoundInstance.getType(), unitAmount);
                        if (simpleCompoundInstance.getAmount() > 0)
                        {
                            if (compoundInstance.getType().getGroup().isValidFor(neighbor.getWrapper(), simpleCompoundInstance))
                            {
                                if (compoundInstance.getType().getGroup().canContributeToRecipeAsOutput(neighbor.getWrapper(), recipeGraphNode.getRecipe(), simpleCompoundInstance))
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

    private static void handleIngredientNodeCompounds(
      @NotNull final Graph<IAnalysisGraphNode, AccessibleWeightEdge> recipeGraph,
      final IngredientCandidateGraphNode ingredientGraphNode, final boolean incomplete)
    {
        Map<ICompoundTypeGroup, List<Triple<ICompoundTypeGroup, ICompoundContainer<?>, Set<CompoundInstance>>>> map = new HashMap<>();
        for (AccessibleWeightEdge accessibleWeightEdge : recipeGraph.incomingEdgesOf(ingredientGraphNode))
        {
            IAnalysisGraphNode edgeSource = recipeGraph.getEdgeSource(accessibleWeightEdge);
            if (edgeSource instanceof ContainerWrapperGraphNode)
            {
                ContainerWrapperGraphNode n = (ContainerWrapperGraphNode) edgeSource;
                Pair<? extends ICompoundContainer<?>, Set<CompoundInstance>> of = Pair.of(n.getWrapper(), n.getCompoundInstances());
                Pair<? extends ICompoundContainer<?>, Collection<List<CompoundInstance>>> collectionPair =
                  Pair.of(of.getLeft(), of.getRight().stream().collect(Collectors.groupingBy(i -> i.getType().getGroup())).values());
                for (List<CompoundInstance> l : collectionPair.getRight())
                {
                    Triple<ICompoundTypeGroup, ICompoundContainer<?>, Set<CompoundInstance>> iCompoundTypeGroupSetTriple =
                      Triple.of(l.get(0).getType().getGroup(), collectionPair.getLeft(), new HashSet<>(l));
                    map.computeIfAbsent(iCompoundTypeGroupSetTriple.getLeft(), k -> new ArrayList<>()).add(iCompoundTypeGroupSetTriple);
                }
            }
        }


        map
          .entrySet()
          .stream()
          .map(e -> Pair.of(e.getKey(), e.getValue()
            .stream()
            .map(t -> Pair.of(t.getMiddle(), t.getRight()))
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight))))
          .forEach(p -> {
              final ICompoundTypeGroup group = p.getKey();
              final Map<? extends ICompoundContainer<?>, Set<CompoundInstance>> data = p.getRight();

              if (data.size() != ingredientGraphNode.getAnalyzedInputNodes().size()) {
                  System.out.println("Next");
              }

              if (data.size() == 1 && !incomplete) {
                  data.values().iterator().next().forEach(ingredientGraphNode::addCompound);
              }
              else if (data.size() > 1 || incomplete) {
                  final Set<CompoundInstance> handledData = group.handleIngredient(data, incomplete);
                  handledData.forEach(ingredientGraphNode::addCompound);
              }
          });
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

    private static final class StatCollector {

        private static final Logger LOGGER = LogManager.getLogger();

        private final ResourceLocation worldName;
        private final long totalNodes;
        private long lastReportingTime = 0;
        private long visitedNodes = 0;
        private long sourceNodesVisited;
        private long compoundNodesVisited;
        private long ingredientNodesVisited;
        private long recipeNodesVisited;

        private StatCollector(final ResourceLocation worldName, final int totalNodes) {
            this.worldName = worldName;
            this.totalNodes = totalNodes;}

        public void onVisitSourceNode() {
            sourceNodesVisited++;
            onVisitNode();
        }

        public void onVisitCompoundNode() {
            compoundNodesVisited++;
            onVisitNode();
        }

        public void onVisitIngredientNode() {
            ingredientNodesVisited++;
            onVisitNode();
        }

        public void onVisitRecipeNode() {
            recipeNodesVisited++;
            onVisitNode();
        }

        private void onVisitNode() {
            visitedNodes++;
            final int newPercentage = (int) Math.floorDiv(visitedNodes * 100, totalNodes);
            final long now = System.currentTimeMillis();
            if (now >= lastReportingTime + (5*1000)) {
                lastReportingTime = now;

                LOGGER.info(String.format("Visited: %d%% of nodes during analysis of recipe graph for world: %s. (%d/%d/%d/%d of %d)",
                  newPercentage,
                  worldName,
                  sourceNodesVisited,
                  compoundNodesVisited,
                  ingredientNodesVisited,
                  recipeNodesVisited,
                  totalNodes));
            }
        }
    }
}
