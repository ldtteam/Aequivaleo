package com.ldtteam.aequivaleo.analyzer;

import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.analyzer.debug.GraphIOHandler;
import com.ldtteam.aequivaleo.analyzer.jgrapht.BuildRecipeGraph;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisGraphNode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisNodeWithContainer;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisNodeWithSubNodes;
import com.ldtteam.aequivaleo.analyzer.jgrapht.cycles.JGraphTCyclesReducer;
import com.ldtteam.aequivaleo.analyzer.jgrapht.edge.Edge;
import com.ldtteam.aequivaleo.analyzer.jgrapht.iterator.AnalysisBFSGraphIterator;
import com.ldtteam.aequivaleo.analyzer.jgrapht.node.*;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.util.AequivaleoLogger;
import com.ldtteam.aequivaleo.compound.information.CompoundInformationRegistry;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import java.util.*;

public class JGraphTBasedCompoundAnalyzer
{

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Object ANALYSIS_LOCK = new Object();

    private final World world;

    private Map<ICompoundContainer<?>, Set<CompoundInstance>> results = new TreeMap<>();

    public JGraphTBasedCompoundAnalyzer(final World world) {this.world = world;}

    public BuildRecipeGraph createGraph() {
        final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds = new TreeMap<>();

        final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, Edge> recipeGraph = new DefaultDirectedWeightedGraph<>(Edge.class);

        final Map<ICompoundContainer<?>, IAnalysisGraphNode<Set<CompoundInstance>>> compoundNodes = new HashMap<>();
        final Map<IRecipeIngredient, IAnalysisGraphNode<Set<CompoundInstance>>> ingredientNodes = new HashMap<>();

        for (IEquivalencyRecipe recipe : EquivalencyRecipeRegistry.getInstance(world.getDimensionKey())
                                           .get())
        {
            final IAnalysisGraphNode<Set<CompoundInstance>> recipeGraphNode = new RecipeNode(recipe);

            recipeGraph.addVertex(recipeGraphNode);

            //Process inputs
            for (IRecipeIngredient input : recipe.getInputs())
            {
                final IRecipeIngredient unitIngredient = new SimpleIngredientBuilder().from(input).withCount(1).createIngredient();
                ingredientNodes.putIfAbsent(unitIngredient, new IngredientNode(unitIngredient));

                final IAnalysisGraphNode<Set<CompoundInstance>> inputNode = ingredientNodes.get(unitIngredient);
                recipeGraph.addVertex(inputNode);

                recipeGraph.addEdge(inputNode, recipeGraphNode);
                recipeGraph.setEdgeWeight(inputNode, recipeGraphNode, input.getRequiredCount());

                for (final ICompoundContainer<?> candidate : input.getCandidates())
                {
                    handleCompoundContainerAsInput(recipeGraph, compoundNodes, inputNode, candidate);
                }
            }

            //Process outputs
            for (ICompoundContainer<?> output : recipe.getRequiredKnownOutputs())
            {
                handleCompoundContainerAsInput(recipeGraph, compoundNodes, recipeGraphNode, output);
            }

            //Process outputs
            for (ICompoundContainer<?> output : recipe.getOutputs())
            {
                final ICompoundContainer<?> unitOutputWrapper = createUnitWrapper(output);
                if (compoundNodes.putIfAbsent(unitOutputWrapper, new ContainerNode(unitOutputWrapper)) == null)
                {
                    LOGGER.debug(String.format("Added new output node for: %s", output));
                }
                else
                {
                    LOGGER.debug(String.format("Reused existing output node for: %s", output));
                }

                final IAnalysisGraphNode<Set<CompoundInstance>> outputWrapperGraphNode = compoundNodes.get(unitOutputWrapper);
                recipeGraph.addVertex(outputWrapperGraphNode);

                recipeGraph.addEdge(recipeGraphNode, outputWrapperGraphNode);
                recipeGraph.setEdgeWeight(recipeGraphNode, outputWrapperGraphNode, output.getContentsCount());
            }
        }

        for (ICompoundContainer<?> valueWrapper : CompoundInformationRegistry.getInstance(world.getDimensionKey()).getValueInformation().keySet())
        {
            IAnalysisGraphNode<Set<CompoundInstance>> node;
            if (!recipeGraph.containsVertex(new ContainerNode(valueWrapper)))
            {
                compoundNodes.putIfAbsent(valueWrapper, new ContainerNode(valueWrapper));

                final IAnalysisGraphNode<Set<CompoundInstance>> inputWrapperGraphNode = compoundNodes.get(valueWrapper);
                node = inputWrapperGraphNode;
                recipeGraph.addVertex(inputWrapperGraphNode);
            }
            else
            {
                final Set<Edge> incomingEdgesToRemove = new HashSet<>(recipeGraph.incomingEdgesOf(compoundNodes.get(valueWrapper)));
                recipeGraph.removeAllEdges(incomingEdgesToRemove);
                node = compoundNodes.get(valueWrapper);
            }

            if (node == null)
            {
                throw new IllegalStateException("Container node for locked information needs to be in the graph node map!");
            }

            node.forceSetResult(CompoundInformationRegistry.getInstance(world.getDimensionKey()).getValueInformation().get(valueWrapper));
        }

        if (Aequivaleo.getInstance().getConfiguration().getServer().exportGraph.get())
        {
            GraphIOHandler.getInstance().export(world.getDimensionKey().getLocation().toString().replace(":", "_").concat(".json"), recipeGraph);
        }

        final Set<ContainerNode> rootNodes = findRootNodes(recipeGraph);

        final Set<IAnalysisNodeWithContainer<Set<CompoundInstance>>> notDefinedGraphNodes = new HashSet<>();
        for (ContainerNode n : rootNodes)
        {
            if (n.getWrapper().map(w -> getLockedInformationInstances(w).isEmpty()).orElse(false) && n.getWrapper().map(w -> getValueInformationInstances(w).isEmpty()).orElse(false))
            {
                notDefinedGraphNodes.add(n);
            }
        }

        notDefinedGraphNodes.forEach(IAnalysisGraphNode::setIncomplete);

        final SourceNode source = new SourceNode();
        recipeGraph.addVertex(source);

        for (ContainerNode rootNode : rootNodes)
        {
            recipeGraph.addEdge(source, rootNode);
            recipeGraph.setEdgeWeight(source, rootNode, 1d);
        }

        final JGraphTCyclesReducer<IAnalysisGraphNode<Set<CompoundInstance>>, Edge> cyclesReducer = new JGraphTCyclesReducer<>(
          InnerGraphNode::new,
          (graph, oldEdge, newEdge) -> graph.setEdgeWeight(newEdge, oldEdge.getWeight()),
          IAnalysisGraphNode::onNeighborReplaced);

        cyclesReducer.reduce(recipeGraph);
        return new BuildRecipeGraph(
          recipeGraph,
          resultingCompounds,
          compoundNodes,
          ingredientNodes,
          notDefinedGraphNodes,
          source);
    }

    public void calculate()
    {
        final BuildRecipeGraph buildRecipeGraph = createGraph();
        final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, Edge> recipeGraph = buildRecipeGraph.getRecipeGraph();
        final Map<ICompoundContainer<?>, Set<CompoundInstance>>                      resultingCompounds = buildRecipeGraph.getResultingCompounds();
        final Map<ICompoundContainer<?>, IAnalysisGraphNode<Set<CompoundInstance>>>  compoundNodes = buildRecipeGraph.getCompoundNodes();
        final Set<IAnalysisNodeWithContainer<Set<CompoundInstance>>> notDefinedGraphNodes = buildRecipeGraph.getNotDefinedGraphNodes();
        final SourceNode source = buildRecipeGraph.getSourceGraphNode();

        final StatCollector statCollector = new StatCollector(getWorld().getDimensionKey().getLocation().toString(), recipeGraph.vertexSet().size());
        final AnalysisBFSGraphIterator<Set<CompoundInstance>> analysisBFSGraphIterator = new AnalysisBFSGraphIterator<>(recipeGraph, source);

        while (analysisBFSGraphIterator.hasNext())
        {
            analysisBFSGraphIterator.next().collectStats(statCollector);
        }

        statCollector.onCalculationComplete();

        for (ICompoundContainer<?> valueWrapper : CompoundInformationRegistry.getInstance(world.getDimensionKey()).getLockingInformation().keySet())
        {
            IAnalysisGraphNode<Set<CompoundInstance>> node;
            if (!recipeGraph.containsVertex(new ContainerNode(valueWrapper)))
            {
                compoundNodes.putIfAbsent(valueWrapper, new ContainerNode(valueWrapper));

                final IAnalysisGraphNode<Set<CompoundInstance>> inputWrapperGraphNode = compoundNodes.get(valueWrapper);
                node = inputWrapperGraphNode;
                recipeGraph.addVertex(inputWrapperGraphNode);
            }
            else
            {
                final Set<Edge> incomingEdgesToRemove = new HashSet<>(recipeGraph.incomingEdgesOf(compoundNodes.get(valueWrapper)));
                recipeGraph.removeAllEdges(incomingEdgesToRemove);
                node = compoundNodes.get(valueWrapper);
            }

            if (node == null)
            {
                throw new IllegalStateException("Container node for locked information needs to be in the graph node map!");
            }

            node.forceSetResult(CompoundInformationRegistry.getInstance(world.getDimensionKey()).getLockingInformation().get(valueWrapper));
        }

        extractCompoundInstancesFromGraph(recipeGraph.vertexSet(), resultingCompounds, notDefinedGraphNodes);

        if (Aequivaleo.getInstance().getConfiguration().getServer().writeResultsToLog.get())
        {
            synchronized (ANALYSIS_LOCK)
            {
                AequivaleoLogger.startBigWarning(String.format("WARNING: Missing root equivalency data in world: %s", getWorld().getDimensionKey().getLocation()));
                for (IAnalysisNodeWithContainer<Set<CompoundInstance>> node : notDefinedGraphNodes)
                {
                    AequivaleoLogger.bigWarningMessage(String.format("Missing root information for: %s. Removing from recipe graph.", node.getWrapper().map(Object::toString).orElse("<UNKNOWN>")));
                    recipeGraph.removeVertex(node);
                }
                AequivaleoLogger.endBigWarning(String.format("WARNING: Missing root equivalency data in world: %s", getWorld().getDimensionKey().getLocation()));

                AequivaleoLogger.startBigWarning(String.format("RESULT: Compound analysis for world: %s", getWorld().getDimensionKey().getLocation()));
                for (Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>> entry : resultingCompounds.entrySet())
                {
                    ICompoundContainer<?> wrapper = entry.getKey();
                    Set<CompoundInstance> compounds = entry.getValue();
                    if (!compounds.isEmpty())
                    {
                        AequivaleoLogger.bigWarningMessage("{}: {}", wrapper, compounds);
                    }
                }
                AequivaleoLogger.endBigWarning(String.format("RESULT: Compound analysis for world: %s", getWorld().getDimensionKey().getLocation()));
            }
        }
        else
        {
            AequivaleoLogger.bigWarningSimple(String.format("Finished the analysis of: %s", getWorld().getDimensionKey().getLocation()));
        }

        this.results = resultingCompounds;
    }

    private void extractCompoundInstancesFromGraph(
      final Set<IAnalysisGraphNode<Set<CompoundInstance>>> vertices,
      final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds,
      final Set<IAnalysisNodeWithContainer<Set<CompoundInstance>>> notDefinedGraphNodes)
    {
        for (IAnalysisGraphNode<Set<CompoundInstance>> v : vertices)
        {
            if (v instanceof IAnalysisNodeWithSubNodes) {
                extractCompoundInstancesFromGraph(((IAnalysisNodeWithSubNodes<Set<CompoundInstance>>) v).getInnerNodes(), resultingCompounds, notDefinedGraphNodes);
            }
            else if (v instanceof IAnalysisNodeWithContainer)
            {
                IAnalysisNodeWithContainer<Set<CompoundInstance>> containerWrapperGraphNode = (IAnalysisNodeWithContainer<Set<CompoundInstance>>) v;
                //We could not find any information on this, possibly due to it being in a different set,
                //Or it is not producible. Register it as a not defined graph node.
                if (containerWrapperGraphNode.getResultingValue().orElse(Collections.emptySet()).size() == 0)
                {
                    notDefinedGraphNodes.add(containerWrapperGraphNode);
                }
                else
                {
                    if (!resultingCompounds.containsKey(containerWrapperGraphNode.getWrapper().orElse(null)))
                        resultingCompounds.putIfAbsent(containerWrapperGraphNode.getWrapper().orElse(null), new TreeSet<>());

                    resultingCompounds.get(containerWrapperGraphNode.getWrapper().orElse(null)).addAll(containerWrapperGraphNode.getResultingValue().orElse(Collections.emptySet()));
                }
            }
        }
    }

    private void handleCompoundContainerAsInput(
      final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, Edge> recipeGraph,
      final Map<ICompoundContainer<?>, IAnalysisGraphNode<Set<CompoundInstance>>> nodes,
      final IAnalysisGraphNode<Set<CompoundInstance>> target,
      final ICompoundContainer<?> candidate
    )
    {
        final ICompoundContainer<?> unitWrapper = createUnitWrapper(candidate);
        if (nodes.putIfAbsent(unitWrapper, new ContainerNode(unitWrapper)) == null)
        {
            LOGGER.debug(String.format("Added new input node for: %s", candidate));
        }
        else
        {
            LOGGER.debug(String.format("Reused existing input node for: %s", candidate));
        }

        final IAnalysisGraphNode<Set<CompoundInstance>> candidateNode = nodes.get(unitWrapper);

        recipeGraph.addVertex(candidateNode);

        recipeGraph.addEdge(candidateNode, target);
        recipeGraph.setEdgeWeight(candidateNode, target, candidate.getContentsCount());
    }

    public Map<ICompoundContainer<?>, Set<CompoundInstance>> calculateAndGet()
    {
        calculate();
        return results;
    }

    private ICompoundContainer<?> createUnitWrapper(@NotNull final ICompoundContainer<?> wrapper)
    {
        if (wrapper.getContentsCount() == 1d)
        {
            return wrapper;
        }

        return CompoundContainerFactoryManager.getInstance().wrapInContainer(wrapper.getContents(), 1d);
    }

    private Set<ContainerNode> findRootNodes(@NotNull final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, Edge> graph)
    {
        Set<ContainerNode> set = new HashSet<>();
        for (IAnalysisGraphNode<Set<CompoundInstance>> v : findDanglingNodes(graph))
        {
            if (v instanceof ContainerNode)
            {
                ContainerNode containerNode = (ContainerNode) v;
                set.add(containerNode);
            }
        }
        return set;
    }

    private Set<IAnalysisGraphNode<Set<CompoundInstance>>> findDanglingNodes(@NotNull final Graph<IAnalysisGraphNode<Set<CompoundInstance>>, Edge> graph)
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
        final Set<CompoundInstance> lockedInstances = CompoundInformationRegistry.getInstance(world.getDimensionKey())
                                                        .getLockingInformation()
                                                        .get(createUnitWrapper(wrapper));

        if (lockedInstances != null)
        {
            return lockedInstances;
        }

        return new HashSet<>();
    }

    private Set<CompoundInstance> getValueInformationInstances(@NotNull final ICompoundContainer<?> wrapper)
    {
        final Set<CompoundInstance> valueInstances = CompoundInformationRegistry.getInstance(world.getDimensionKey())
                                                       .getValueInformation()
                                                       .get(createUnitWrapper(wrapper));

        if (valueInstances != null)
        {
            return valueInstances;
        }

        return new HashSet<>();
    }

    public World getWorld()
    {
        return world;
    }
}
