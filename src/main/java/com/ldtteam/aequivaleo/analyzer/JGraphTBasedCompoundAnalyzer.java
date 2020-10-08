package com.ldtteam.aequivaleo.analyzer;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.analyzer.debug.GraphIOHandler;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.node.*;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.util.AequivaleoLogger;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
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

        final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> recipeGraph = new DefaultDirectedWeightedGraph<>(AccessibleWeightEdge.class);

        final Map<ICompoundContainer<?>, IAnalysisGraphNode<Set<CompoundInstance>>> compoundNodes = new TreeMap<>();
        final Map<IRecipeIngredient, IAnalysisGraphNode<Set<CompoundInstance>>> ingredientNodes = new TreeMap<>();

        for (IEquivalencyRecipe recipe : EquivalencyRecipeRegistry.getInstance(world.getDimensionKey())
                                           .get())
        {
            final IAnalysisGraphNode<Set<CompoundInstance>> recipeGraphNode = new RecipeGraphNode(recipe);

            recipeGraph.addVertex(recipeGraphNode);

            //Process inputs
            for (IRecipeIngredient input : recipe.getInputs())
            {
                final IRecipeIngredient unitIngredient = new SimpleIngredientBuilder().from(input).withCount(1).createIngredient();
                ingredientNodes.putIfAbsent(unitIngredient, new IngredientCandidateGraphNode(unitIngredient));

                final IAnalysisGraphNode<Set<CompoundInstance>> inputNode = ingredientNodes.get(unitIngredient);
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

                final IAnalysisGraphNode<Set<CompoundInstance>> outputWrapperGraphNode = compoundNodes.get(unitOutputWrapper);

                resultingCompounds.putIfAbsent(unitOutputWrapper, new ConcurrentSkipListSet<>());
                recipeGraph.addVertex(outputWrapperGraphNode);

                recipeGraph.addEdge(recipeGraphNode, outputWrapperGraphNode);
                recipeGraph.setEdgeWeight(recipeGraphNode, outputWrapperGraphNode, output.getContentsCount());
            }
        }

        for (ICompoundContainer<?> lockedWrapper : LockedCompoundInformationRegistry.getInstance(world.getDimensionKey()).get().keySet())
        {
            if (!recipeGraph.containsVertex(new ContainerWrapperGraphNode(lockedWrapper)))
            {
                compoundNodes.putIfAbsent(lockedWrapper, new ContainerWrapperGraphNode(lockedWrapper));

                final IAnalysisGraphNode<Set<CompoundInstance>> inputWrapperGraphNode = compoundNodes.get(lockedWrapper);

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
            GraphIOHandler.getInstance().export(world.getDimensionKey().getLocation().toString().replace(":", "_").concat(".json"), recipeGraph);

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

        final StatCollector statCollector = new StatCollector(getWorld().getDimensionKey().getLocation(), recipeGraph.vertexSet().size());
        processRecipeGraphUsingBreathFirstSearch(recipeGraph, statCollector);

        statCollector.onCalculationComplete();

        for (IAnalysisGraphNode<Set<CompoundInstance>> v : recipeGraph
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
                AequivaleoLogger.startBigWarning(String.format("WARNING: Missing root equivalency data in world: %s", getWorld().getDimensionKey().getLocation()));
                for (ContainerWrapperGraphNode node : notDefinedGraphNodes)
                {
                    AequivaleoLogger.bigWarningMessage(String.format("Missing root information for: %s. Removing from recipe graph.", node.getWrapper()));
                    recipeGraph.removeVertex(node);
                }
                AequivaleoLogger.endBigWarning(String.format("WARNING: Missing root equivalency data in world: %s", getWorld().getDimensionKey().getLocation()));

                AequivaleoLogger.startBigWarning(String.format("RESULT: Compound analysis for world: %s", getWorld().getDimensionKey().getLocation()));
                for (Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>> entry : resultingCompounds.entrySet())
                {
                    ICompoundContainer<?> wrapper = entry.getKey();
                    Set<CompoundInstance> compounds = entry.getValue();
                    if (!compounds.isEmpty())
                        AequivaleoLogger.bigWarningMessage("{}: {}", wrapper, compounds);
                }
                AequivaleoLogger.endBigWarning(String.format("RESULT: Compound analysis for world: %s", getWorld().getDimensionKey().getLocation()));
            }
        } else {
            AequivaleoLogger.bigWarningSimple(String.format("Finished the analysis of: %s", getWorld().getDimensionKey().getLocation()));
        }

        this.results = resultingCompounds;
    }

    private void handleCompoundContainerAsInput(
      final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds,
      final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> recipeGraph,
      final Map<ICompoundContainer<?>, IAnalysisGraphNode<Set<CompoundInstance>>> nodes,
      final IAnalysisGraphNode<Set<CompoundInstance>> target,
      final ICompoundContainer<?> candidate
    )
    {
        final ICompoundContainer<?> unitWrapper = createUnitWrapper(candidate);
        nodes.putIfAbsent(unitWrapper, new ContainerWrapperGraphNode(unitWrapper));

        final IAnalysisGraphNode<Set<CompoundInstance>> candidateNode = nodes.get(unitWrapper);

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
    private void removeDanglingNodes(@NotNull final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> recipeGraph,
      @NotNull final Set<ContainerWrapperGraphNode> rootNodes)
    {
        @NotNull Set<IAnalysisGraphNode<Set<CompoundInstance>>> danglingNodesToDelete = new HashSet<>();
        for (IAnalysisGraphNode<Set<CompoundInstance>> iAnalysisGraphNode : findDanglingNodes(recipeGraph))
        {
            if (!rootNodes.contains(iAnalysisGraphNode))
            {
                danglingNodesToDelete.add(iAnalysisGraphNode);
            }
        }

        while(!danglingNodesToDelete.isEmpty())
        {
            for (IAnalysisGraphNode<Set<CompoundInstance>> iAnalysisGraphNode : danglingNodesToDelete)
            {
                recipeGraph.removeVertex(iAnalysisGraphNode);
            }

            Set<IAnalysisGraphNode<Set<CompoundInstance>>> set = new HashSet<>();
            for (IAnalysisGraphNode<Set<CompoundInstance>> n : findDanglingNodes(recipeGraph))
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
      @NotNull final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> recipeGraph,
      final StatCollector statCollector)
    {
        final LinkedHashSet<IAnalysisGraphNode<Set<CompoundInstance>>> processingQueue = new LinkedHashSet<>();
        processingQueue.add(new SourceGraphNode());
        final LinkedHashSet<IngredientCandidateGraphNode> noneCompleteIngredientNodes = new LinkedHashSet<>();

        processRecipeGraphFromNodeUsingBreathFirstSearch(recipeGraph, processingQueue, noneCompleteIngredientNodes, statCollector);

        final Set<IngredientCandidateGraphNode> alreadyProcessedIncompleteNodes = new HashSet<>();

        LinkedHashSet<IngredientCandidateGraphNode> workingSet = new LinkedHashSet<>(noneCompleteIngredientNodes);
        while(!workingSet.isEmpty()) {
            final IngredientCandidateGraphNode newTarget = workingSet.iterator().next();
            workingSet.remove(newTarget);
            alreadyProcessedIncompleteNodes.add(newTarget);

            handleIngredientNodeCompounds(
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

            workingSet.removeAll(alreadyProcessedIncompleteNodes);
        }
    }

    private void processRecipeGraphFromNodeUsingBreathFirstSearch(
      @NotNull final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> recipeGraph,
      final LinkedHashSet<IAnalysisGraphNode<Set<CompoundInstance>>> processingQueue,
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
        }
        if (clazz == ContainerWrapperGraphNode.class)
        {
            statCollector.onVisitCompoundNode();
        }
        if (clazz == IngredientCandidateGraphNode.class)
        {
            statCollector.onVisitIngredientNode();
        }
        if (clazz == RecipeGraphNode.class)
        {
            statCollector.onVisitRecipeNode();
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
        final Set<CompoundInstance> lockedInstances = LockedCompoundInformationRegistry.getInstance(world.getDimensionKey())
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
            final long now = System.currentTimeMillis();
            
            if (now >= lastReportingTime + (5*1000)) {
                lastReportingTime = now;
                logState();
            }
        }

        private void logState()
        {
            final int newPercentage = (int) Math.floorDiv(visitedNodes * 100, totalNodes);

            LOGGER.info(String.format("Visited: %d%% of nodes during analysis of recipe graph for world: %s. (%d/%d/%d/%d of %d)",
              newPercentage,
              worldName,
              sourceNodesVisited,
              compoundNodesVisited,
              ingredientNodesVisited,
              recipeNodesVisited,
              totalNodes));
        }

        private void onCalculationComplete() {
            logState();
        }
    }
}
