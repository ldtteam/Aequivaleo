package com.ldtteam.aequivaleo.analyzer;

import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.analyzer.debug.GraphIOHandler;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.AccessibleWeightEdge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.iterator.AnalysisBFSGraphIterator;
import com.ldtteam.aequivaleo.analyzer.jgrapht.node.*;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.util.AequivaleoLogger;
import com.ldtteam.aequivaleo.compound.information.locked.LockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

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

        for (ICompoundContainer<?> valueWrapper : LockedCompoundInformationRegistry.getInstance(world.getDimensionKey()).getValueInformation().keySet())
        {
            IAnalysisGraphNode<Set<CompoundInstance>> node;
            if (!recipeGraph.containsVertex(new ContainerWrapperGraphNode(valueWrapper)))
            {
                compoundNodes.putIfAbsent(valueWrapper, new ContainerWrapperGraphNode(valueWrapper));

                final IAnalysisGraphNode<Set<CompoundInstance>> inputWrapperGraphNode = compoundNodes.get(valueWrapper);
                node = inputWrapperGraphNode;

                resultingCompounds.putIfAbsent(valueWrapper, new ConcurrentSkipListSet<>());
                recipeGraph.addVertex(inputWrapperGraphNode);
            }
            else
            {
                final Set<AccessibleWeightEdge> incomingEdgesToRemove = new HashSet<>(recipeGraph.incomingEdgesOf(compoundNodes.get(valueWrapper)));
                recipeGraph.removeAllEdges(incomingEdgesToRemove);
                node = compoundNodes.get(valueWrapper);
            }

            if (node == null)
                throw new IllegalStateException("Container node for locked information needs to be in the graph node map!");

            node.forceSetResult(LockedCompoundInformationRegistry.getInstance(world.getDimensionKey()).getValueInformation().get(valueWrapper));
        }

        if (Aequivaleo.getInstance().getConfiguration().getServer().exportGraph.get())
            GraphIOHandler.getInstance().export(world.getDimensionKey().getLocation().toString().replace(":", "_").concat(".json"), recipeGraph);

        final Set<ContainerWrapperGraphNode> rootNodes = findRootNodes(recipeGraph);

        final Set<ContainerWrapperGraphNode> notDefinedGraphNodes = new HashSet<>();
        for (ContainerWrapperGraphNode n : rootNodes)
        {
            if (getLockedInformationInstances(n.getWrapper()).isEmpty() && getValueInformationInstances(n.getWrapper()).isEmpty())
            {
                notDefinedGraphNodes.add(n);
            }
        }

        notDefinedGraphNodes.forEach(IAnalysisGraphNode::setIncomplete);

        final SourceGraphNode source = new SourceGraphNode();
        recipeGraph.addVertex(source);

        for (ContainerWrapperGraphNode rootNode : rootNodes)
        {
            recipeGraph.addEdge(source, rootNode);
            recipeGraph.setEdgeWeight(source, rootNode, 1d);
        }

        final StatCollector statCollector = new StatCollector(getWorld().getDimensionKey().getLocation(), recipeGraph.vertexSet().size());
        final AnalysisBFSGraphIterator<Set<CompoundInstance>> analysisBFSGraphIterator = new AnalysisBFSGraphIterator<>(recipeGraph, source);

        while(analysisBFSGraphIterator.hasNext()) {
            analysisBFSGraphIterator.next().collectStats(statCollector);
        }

        statCollector.onCalculationComplete();

        for (ICompoundContainer<?> valueWrapper : LockedCompoundInformationRegistry.getInstance(world.getDimensionKey()).getLockingInformation().keySet())
        {
            IAnalysisGraphNode<Set<CompoundInstance>> node;
            if (!recipeGraph.containsVertex(new ContainerWrapperGraphNode(valueWrapper)))
            {
                compoundNodes.putIfAbsent(valueWrapper, new ContainerWrapperGraphNode(valueWrapper));

                final IAnalysisGraphNode<Set<CompoundInstance>> inputWrapperGraphNode = compoundNodes.get(valueWrapper);
                node = inputWrapperGraphNode;

                resultingCompounds.putIfAbsent(valueWrapper, new ConcurrentSkipListSet<>());
                recipeGraph.addVertex(inputWrapperGraphNode);
            }
            else
            {
                final Set<AccessibleWeightEdge> incomingEdgesToRemove = new HashSet<>(recipeGraph.incomingEdgesOf(compoundNodes.get(valueWrapper)));
                recipeGraph.removeAllEdges(incomingEdgesToRemove);
                node = compoundNodes.get(valueWrapper);
            }

            if (node == null)
                throw new IllegalStateException("Container node for locked information needs to be in the graph node map!");

            node.forceSetResult(LockedCompoundInformationRegistry.getInstance(world.getDimensionKey()).getLockingInformation().get(valueWrapper));
        }

        for (IAnalysisGraphNode<Set<CompoundInstance>> v : recipeGraph
                                      .vertexSet())
        {
            if (v instanceof ContainerWrapperGraphNode)
            {
                ContainerWrapperGraphNode containerWrapperGraphNode = (ContainerWrapperGraphNode) v;
                //We could not find any information on this, possibly due to it being in a different set,
                //Or it is not producible. Register it as a not defined graph node.
                if (containerWrapperGraphNode.getResultingValue().orElse(Collections.emptySet()).size() == 0)
                {
                    notDefinedGraphNodes.add(containerWrapperGraphNode);
                }
                else
                {
                    if (!resultingCompounds.containsKey(containerWrapperGraphNode.getWrapper()))
                        resultingCompounds.putIfAbsent(containerWrapperGraphNode.getWrapper(), new ConcurrentSkipListSet<>());

                    resultingCompounds.get(containerWrapperGraphNode.getWrapper()).addAll(containerWrapperGraphNode.getResultingValue().orElse(Collections.emptySet()));
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

    private ICompoundContainer<?> createUnitWrapper(@NotNull final ICompoundContainer<?> wrapper)
    {
        if (wrapper.getContentsCount() == 1d)
            return wrapper;

        return CompoundContainerFactoryManager.getInstance().wrapInContainer(wrapper.getContents(), 1d);
    }

    private Set<ContainerWrapperGraphNode> findRootNodes(@NotNull final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> graph)
    {
        Set<ContainerWrapperGraphNode> set = new HashSet<>();
        for (IAnalysisGraphNode<Set<CompoundInstance>> v : findDanglingNodes(graph))
        {
            if (v instanceof ContainerWrapperGraphNode)
            {
                ContainerWrapperGraphNode containerWrapperGraphNode = (ContainerWrapperGraphNode) v;
                set.add(containerWrapperGraphNode);
            }
        }
        return set;
    }

    private Set<IAnalysisGraphNode<Set<CompoundInstance>>> findDanglingNodes(@NotNull final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, AccessibleWeightEdge> graph)
    {
        Set<IAnalysisGraphNode<Set<CompoundInstance>>> set = new HashSet<>();
        for (IAnalysisGraphNode<Set<CompoundInstance>> v : graph
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
                                                         .getLockingInformation()
                                                         .get(createUnitWrapper(wrapper));

        if (lockedInstances != null)
            return lockedInstances;

        return new HashSet<>();
    }

    private Set<CompoundInstance> getValueInformationInstances(@NotNull final ICompoundContainer<?> wrapper)
    {
        final Set<CompoundInstance> valueInstances = LockedCompoundInformationRegistry.getInstance(world.getDimensionKey())
                                                        .getValueInformation()
                                                        .get(createUnitWrapper(wrapper));

        if (valueInstances != null)
            return valueInstances;

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
