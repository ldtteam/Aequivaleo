package com.ldtteam.aequivaleo.analyzer;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.analyzer.debug.GraphIOHandler;
import com.ldtteam.aequivaleo.analyzer.jgrapht.BuildRecipeGraph;
import com.ldtteam.aequivaleo.analyzer.jgrapht.aequivaleo.*;
import com.ldtteam.aequivaleo.analyzer.jgrapht.core.IAnalysisGraphNode;
import com.ldtteam.aequivaleo.analyzer.jgrapht.cycles.JGraphTCyclesReducer;
import com.ldtteam.aequivaleo.analyzer.jgrapht.graph.AequivaleoGraph;
import com.ldtteam.aequivaleo.analyzer.jgrapht.iterator.AnalysisBFSGraphIterator;
import com.ldtteam.aequivaleo.analyzer.jgrapht.node.*;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.util.AequivaleoLogger;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.information.CompoundInformationRegistry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;

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

        final IGraph recipeGraph = new AequivaleoGraph();

        final Map<ICompoundContainer<?>, INode> compoundNodes = new HashMap<>();
        final Map<IRecipeIngredient, INode> ingredientNodes = new HashMap<>();

        for (IEquivalencyRecipe recipe : EquivalencyRecipeRegistry.getInstance(world.getDimensionKey())
                                           .get())
        {
            if (recipe.getInputs().isEmpty())
            {
                LOGGER.warn(String.format("Skipping recipe with no ingredients: %s", recipe));
                continue;
            }

            final INode recipeGraphNode = new RecipeNode(recipe);

            recipeGraph.addVertex(recipeGraphNode);

            //Process inputs
            for (IRecipeIngredient input : recipe.getInputs())
            {
                final IRecipeIngredient unitIngredient = new SimpleIngredientBuilder().from(input).withCount(1).createIngredient();
                ingredientNodes.putIfAbsent(unitIngredient, new IngredientNode(unitIngredient));

                final INode inputNode = ingredientNodes.get(unitIngredient);
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

                final INode outputWrapperGraphNode = compoundNodes.get(unitOutputWrapper);
                recipeGraph.addVertex(outputWrapperGraphNode);

                recipeGraph.addEdge(recipeGraphNode, outputWrapperGraphNode);
                recipeGraph.setEdgeWeight(recipeGraphNode, outputWrapperGraphNode, output.getContentsCount());
            }
        }

        for (ICompoundContainer<?> valueWrapper : CompoundInformationRegistry.getInstance(world.getDimensionKey()).getValueInformation().keySet())
        {
            INode node;
            if (!recipeGraph.containsVertex(new ContainerNode(valueWrapper)))
            {
                compoundNodes.putIfAbsent(valueWrapper, new ContainerNode(valueWrapper));

                final INode inputWrapperGraphNode = compoundNodes.get(valueWrapper);
                node = inputWrapperGraphNode;
                recipeGraph.addVertex(inputWrapperGraphNode);
            }
            else
            {
                final Set<IEdge> incomingEdgesToRemove = new HashSet<>(recipeGraph.incomingEdgesOf(compoundNodes.get(valueWrapper)));
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

        final Set<INode> notDefinedGraphNodes = new HashSet<>();
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

        LOGGER.warn("Starting cycle reduction.");

        final JGraphTCyclesReducer<IGraph, INode, IEdge> cyclesReducer = new JGraphTCyclesReducer<>(
          InnerNode::new,
          INode::onNeighborReplaced);

        cyclesReducer.reduce(recipeGraph);

        LOGGER.warn("Finished cycle reduction.");

        recipeGraph.removeVertex(source);

        final Set<INode> sourceNodeLinks = findDanglingNodes(recipeGraph);

        recipeGraph.addVertex(source);

        for (INode rootNode : sourceNodeLinks)
        {
            recipeGraph.addEdge(source, rootNode);
            recipeGraph.setEdgeWeight(source, rootNode, 1d);
        }

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
        final IGraph recipeGraph = buildRecipeGraph.getRecipeGraph();
        final Map<ICompoundContainer<?>, Set<CompoundInstance>>                      resultingCompounds = buildRecipeGraph.getResultingCompounds();
        final Map<ICompoundContainer<?>, INode>  compoundNodes = buildRecipeGraph.getCompoundNodes();
        final Set<INode> notDefinedGraphNodes = buildRecipeGraph.getNotDefinedGraphNodes();
        final SourceNode source = buildRecipeGraph.getSourceGraphNode();

        final StatCollector statCollector = new StatCollector(getWorld().getDimensionKey().getLocation().toString(), recipeGraph.vertexSet().size());
        final AnalysisBFSGraphIterator analysisBFSGraphIterator = new AnalysisBFSGraphIterator(recipeGraph, source);

        while (analysisBFSGraphIterator.hasNext())
        {
            analysisBFSGraphIterator.next().collectStats(statCollector);
        }

        statCollector.onCalculationComplete();

        for (ICompoundContainer<?> valueWrapper : CompoundInformationRegistry.getInstance(world.getDimensionKey()).getLockingInformation().keySet())
        {
            INode node;
            if (!recipeGraph.containsVertex(new ContainerNode(valueWrapper)))
            {
                compoundNodes.putIfAbsent(valueWrapper, new ContainerNode(valueWrapper));
                resultingCompounds.computeIfAbsent(valueWrapper, wrapper -> Sets.newHashSet()).addAll(CompoundInformationRegistry.getInstance(world.getDimensionKey()).getLockingInformation().get(valueWrapper));
            }
            node = compoundNodes.get(valueWrapper);

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
                for (INode node : notDefinedGraphNodes)
                {
                    if (node instanceof IContainerNode)
                    {
                        AequivaleoLogger.bigWarningMessage(String.format("Missing root information for: %s. Removing from recipe graph.", ((IContainerNode) node).getWrapper().map(Object::toString).orElse("<UNKNOWN>")));
                    }
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
      final Set<INode> vertices,
      final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds,
      final Set<INode> notDefinedGraphNodes)
    {
        for (INode v : vertices)
        {
            if (v instanceof IInnerNode) {
                extractCompoundInstancesFromGraph(((IInnerNode) v).getInnerNodes(), resultingCompounds, notDefinedGraphNodes);
            }
            else if (v instanceof IContainerNode)
            {
                IContainerNode containerWrapperGraphNode = (IContainerNode) v;
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
      final Graph<INode, IEdge> recipeGraph,
      final Map<ICompoundContainer<?>, INode> nodes,
      final INode target,
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

        final INode candidateNode = nodes.get(unitWrapper);

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

    private Set<ContainerNode> findRootNodes(@NotNull final Graph<INode, IEdge> graph)
    {
        Set<ContainerNode> set = new HashSet<>();
        for (INode v : findDanglingNodes(graph))
        {
            if (v instanceof ContainerNode)
            {
                ContainerNode containerNode = (ContainerNode) v;
                set.add(containerNode);
            }
        }
        return set;
    }

    private Set<INode> findDanglingNodes(@NotNull final Graph<INode, IEdge> graph)
    {
        Set<INode> set = new HashSet<>();
        for (INode v : graph
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
