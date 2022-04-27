package com.ldtteam.aequivaleo.analysis;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.analysis.debug.GraphIOHandler;
import com.ldtteam.aequivaleo.analysis.jgrapht.BuildRecipeGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.*;
import com.ldtteam.aequivaleo.analysis.jgrapht.cache.CacheKey;
import com.ldtteam.aequivaleo.analysis.jgrapht.clique.JGraphTCliqueReducer;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.JGraphTCyclesReducer;
import com.ldtteam.aequivaleo.analysis.jgrapht.graph.AequivaleoGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.iterator.AnalysisBFSGraphIterator;
import com.ldtteam.aequivaleo.analysis.jgrapht.node.*;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.util.AequivaleoLogger;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.information.CompoundInformationRegistry;
import com.ldtteam.aequivaleo.utils.AnalysisLogHandler;
import com.ldtteam.aequivaleo.utils.WorldCacheUtils;
import com.ldtteam.aequivaleo.utils.WorldUtils;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;

import java.util.*;
import java.util.stream.Collectors;

public class JGraphTBasedCompoundAnalyzer
{

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Object ANALYSIS_LOCK = new Object();

    private final List<IAnalysisOwner> owners;
    private final IAnalysisOwner       primaryOwner;
    private final boolean              forceReload;
    private final boolean              writeCachedData;

    private Map<ICompoundContainer<?>, Set<CompoundInstance>> results = new TreeMap<>();

    public JGraphTBasedCompoundAnalyzer(final List<? extends IAnalysisOwner> owners, final boolean forceReload, final boolean writeCachedData)
    {
        this.owners = new ArrayList<>(owners);
        this.primaryOwner = owners.get(0);
        this.forceReload = forceReload;
        this.writeCachedData = writeCachedData;

        if (this.primaryOwner == null)
        {
            throw new IllegalArgumentException("First passed world is null");
        }
    }

    public BuildRecipeGraph createGraph()
    {
        final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds = new TreeMap<>();

        final IGraph recipeGraph = new AequivaleoGraph();

        final Map<ICompoundContainer<?>, INode> compoundNodes = new HashMap<>();
        final Map<IRecipeIngredient, INode> ingredientNodes = new HashMap<>();

        for (IEquivalencyRecipe recipe : EquivalencyRecipeRegistry.getInstance(primaryOwner.getIdentifier())
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
                    AnalysisLogHandler.debug(LOGGER, String.format("Added new output node for: %s", output));
                }
                else
                {
                    AnalysisLogHandler.debug(LOGGER, String.format("Reused existing output node for: %s", output));
                }

                final INode outputWrapperGraphNode = compoundNodes.get(unitOutputWrapper);
                recipeGraph.addVertex(outputWrapperGraphNode);

                recipeGraph.addEdge(recipeGraphNode, outputWrapperGraphNode);
                recipeGraph.setEdgeWeight(recipeGraphNode, outputWrapperGraphNode, output.getContentsCount());
            }
        }

        for (ICompoundContainer<?> valueWrapper : CompoundInformationRegistry.getInstance(primaryOwner.getIdentifier()).getValueInformation().keySet())
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

            node.forceSetResult(CompoundInformationRegistry.getInstance(primaryOwner.getIdentifier()).getValueInformation().get(valueWrapper));
        }

        if (Aequivaleo.getInstance().getConfiguration().getServer().exportGraph.get())
        {
            GraphIOHandler.getInstance().export(primaryOwner.getIdentifier().location().toString().replace(":", "_").concat(".json"), recipeGraph);
        }

        final Set<ContainerNode> rootNodes = findRootNodes(recipeGraph);

        final Set<INode> notDefinedGraphNodes = new HashSet<>();
        for (ContainerNode n : rootNodes)
        {
            if (n.getWrapper().map(w -> getLockedInformationInstances(w).isEmpty()).orElse(false) && n.getWrapper()
              .map(w -> getValueInformationInstances(w).isEmpty())
              .orElse(false))
            {
                notDefinedGraphNodes.add(n);
            }
        }

        final SourceNode source = new SourceNode();
        recipeGraph.addVertex(source);

        for (ContainerNode rootNode : rootNodes)
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

    private IGraph reduceGraph(final IGraph recipeGraph, final SourceNode sourceNode)
    {

        LOGGER.warn("Starting clique reduction.");

        final JGraphTCliqueReducer<IGraph> cliqueReducer = new JGraphTCliqueReducer<>(
          (graph, iNodes, iRecipeNodes, iRecipeInputNodes) -> new CliqueNode(graph, iNodes),
          sets -> {
              if (sets.size() == 1)
              {
                  return Sets.newHashSet(); //Cover a weird etch case where a clique exists out of a single node........
              }

              //Short circuit if all of them have only one node.
              if (sets.stream().allMatch(s -> s.size() == 1))
              {
                  return sets.stream().flatMap(Set::stream).collect(Collectors.toSet());
              }

              final Set<Class<?>> recipeTypes = sets.get(0).stream().map(IRecipeNode::getRecipe).map(Object::getClass).collect(Collectors.toSet());
              final Optional<Class<?>> targetRecipeType =
                recipeTypes.stream()
                  .filter(type -> sets.stream().allMatch(nodes -> nodes.stream().anyMatch(node -> node.getRecipe().getClass().equals(type))))
                  .findAny();

              return targetRecipeType.map(type -> sets.stream()
                  .map(nodes -> {
                      for (IRecipeNode node : nodes)
                      {
                          if (node.getRecipe().getClass().equals(type))
                          {
                              return Optional.of(node).get();
                          }
                      }
                      return null;
                  })
                  .filter(Objects::nonNull)
                  .collect(Collectors.toSet()))
                .orElseGet(Sets::newHashSet);
          }, INode::onNeighborReplaced);

        cliqueReducer.reduce(recipeGraph);

        LOGGER.warn("Finished clique reduction.");

        LOGGER.warn("Starting cycle reduction.");

        final JGraphTCyclesReducer<IGraph, INode, IEdge> cyclesReducer = new JGraphTCyclesReducer<>(
          InnerNode::new,
          INode::onNeighborReplaced);

        cyclesReducer.reduce(recipeGraph);

        LOGGER.warn("Finished cycle reduction.");

        recipeGraph.removeVertex(sourceNode);

        final Set<INode> sourceNodeLinks = findDanglingNodes(recipeGraph);

        recipeGraph.addVertex(sourceNode);

        for (INode rootNode : sourceNodeLinks)
        {
            recipeGraph.addEdge(sourceNode, rootNode);
            recipeGraph.setEdgeWeight(sourceNode, rootNode, 1d);
        }

        return recipeGraph;
    }

    public void calculate()
    {
        if (this.primaryOwner == null)
        {
            throw new IllegalArgumentException("First passed world is null");
        }

        final BuildRecipeGraph buildRecipeGraph = createGraph();
        final IGraph noneReducedGraph = buildRecipeGraph.getRecipeGraph();
        final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds = buildRecipeGraph.getResultingCompounds();
        final Map<ICompoundContainer<?>, INode> compoundNodes = buildRecipeGraph.getCompoundNodes();
        final Set<INode> notDefinedGraphNodes = buildRecipeGraph.getNotDefinedGraphNodes();
        final SourceNode source = buildRecipeGraph.getSourceNode();

        final CacheKey key = new CacheKey(ModList.get(), noneReducedGraph);
        final int graphHash = key.hashCode();
        if (!forceReload)
        {
            //We are allowed to lookup cached values
            final Optional<Map<ICompoundContainer<?>, Set<CompoundInstance>>> cachedResults = WorldCacheUtils.loadCachedResults(primaryOwner, graphHash);
            if (cachedResults.isPresent())
            {
                LOGGER.warn(String.format("Using cached results for: %s", WorldUtils.formatWorldNames(getOwners())));
                this.results = cachedResults.get();
                LOGGER.warn(String.format("Cached results contained %d entries for: %s", this.results.size(), WorldUtils.formatWorldNames(getOwners())));
                return;
            }
        }

        final IGraph recipeGraph = reduceGraph(noneReducedGraph, source);

        final StatCollector statCollector = new StatCollector(WorldUtils.formatWorldNames(getOwners()), recipeGraph.vertexSet().size());
        final AnalysisBFSGraphIterator analysisBFSGraphIterator = new AnalysisBFSGraphIterator(recipeGraph, source);

        while (analysisBFSGraphIterator.hasNext())
        {
            analysisBFSGraphIterator.next().collectStats(statCollector);
        }

        statCollector.onCalculationComplete();

        for (ICompoundContainer<?> valueWrapper : CompoundInformationRegistry.getInstance(primaryOwner.getIdentifier()).getLockingInformation().keySet())
        {
            INode node;
            if (!recipeGraph.containsVertex(new ContainerNode(valueWrapper)))
            {
                LOGGER.debug(String.format("Adding missing locking node for container: %s", valueWrapper));
                compoundNodes.putIfAbsent(valueWrapper, new ContainerNode(valueWrapper));
                resultingCompounds.computeIfAbsent(valueWrapper, wrapper -> Sets.newHashSet())
                  .addAll(Objects.requireNonNull(CompoundInformationRegistry.getInstance(primaryOwner.getIdentifier())
                    .getLockingInformation()
                    .get(valueWrapper)));
            }
            node = compoundNodes.get(valueWrapper);

            if (node == null)
            {
                throw new IllegalStateException("Container node for locked information needs to be in the graph node map!");
            }

            node.forceSetResult(CompoundInformationRegistry.getInstance(primaryOwner.getIdentifier()).getLockingInformation().get(valueWrapper));
        }

        extractCompoundInstancesFromGraph(recipeGraph.vertexSet(), resultingCompounds, notDefinedGraphNodes);

        if (Aequivaleo.getInstance().getConfiguration().getServer().writeResultsToLog.get())
        {
            synchronized (ANALYSIS_LOCK)
            {
                AequivaleoLogger.startBigWarning(String.format("WARNING: Missing root equivalency data in world: %s", WorldUtils.formatWorldNames(getOwners())));
                for (INode node : notDefinedGraphNodes)
                {
                    if (node instanceof IContainerNode)
                    {
                        AequivaleoLogger.bigWarningMessage(String.format("Missing root information for: %s. Removing from recipe graph.",
                          ((IContainerNode) node).getWrapper().map(Object::toString).orElse("<UNKNOWN>")));
                    }
                    recipeGraph.removeVertex(node);
                }
                AequivaleoLogger.endBigWarning(String.format("WARNING: Missing root equivalency data in world: %s", WorldUtils.formatWorldNames(getOwners())));

                AequivaleoLogger.startBigWarning(String.format("RESULT: Compound analysis for world: %s", WorldUtils.formatWorldNames(getOwners())));
                for (Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>> entry : resultingCompounds.entrySet())
                {
                    ICompoundContainer<?> wrapper = entry.getKey();
                    Set<CompoundInstance> compounds = entry.getValue();
                    if (!compounds.isEmpty())
                    {
                        AequivaleoLogger.bigWarningMessage("{}: {}", wrapper, compounds);
                    }
                }
                AequivaleoLogger.endBigWarning(String.format("RESULT: Compound analysis for world: %s", WorldUtils.formatWorldNames(getOwners())));
            }
        }
        else
        {
            AequivaleoLogger.bigWarningSimple(String.format("Finished the analysis of: %s", WorldUtils.formatWorldNames(getOwners())));
        }

        if (writeCachedData)
        {
            LOGGER.warn(String.format("Writing results to cache for: %s", WorldUtils.formatWorldNames(getOwners())));
            WorldCacheUtils.writeCachedResults(primaryOwner, graphHash, resultingCompounds);
            LOGGER.warn(String.format("Written %d results to cache for: %s", resultingCompounds.size(), WorldUtils.formatWorldNames(getOwners())));
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
            if (v instanceof IInnerNode)
            {
                extractCompoundInstancesFromGraph(((IInnerNode) v).getInnerNodes(), resultingCompounds, notDefinedGraphNodes);
            }
            else if (v instanceof IContainerNode containerWrapperGraphNode)
            {
                //We could not find any information on this, possibly due to it being in a different set,
                //Or it is not producible. Register it as a not defined graph node.
                if (containerWrapperGraphNode.getResultingValue().orElse(Collections.emptySet()).size() == 0)
                {
                    notDefinedGraphNodes.add(containerWrapperGraphNode);
                }
                else
                {
                    if (!resultingCompounds.containsKey(containerWrapperGraphNode.getWrapper().orElse(null)))
                    {
                        resultingCompounds.putIfAbsent(containerWrapperGraphNode.getWrapper().orElse(null), new TreeSet<>());
                    }

                    resultingCompounds.get(containerWrapperGraphNode.getWrapper().orElse(null))
                      .addAll(containerWrapperGraphNode.getResultingValue().orElse(Collections.emptySet()));
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
            AnalysisLogHandler.debug(LOGGER, String.format("Added new input node for: %s", candidate));
        }
        else
        {
            AnalysisLogHandler.debug(LOGGER, String.format("Reused existing input node for: %s", candidate));
        }

        final INode candidateNode = nodes.get(unitWrapper);

        recipeGraph.addVertex(candidateNode);

        if (!recipeGraph.containsEdge(candidateNode, target))
        {
            recipeGraph.addEdge(candidateNode, target);
            recipeGraph.setEdgeWeight(candidateNode, target, candidate.getContentsCount());
        }
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
            if (v instanceof ContainerNode containerNode)
            {
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
        final Set<CompoundInstance> lockedInstances = CompoundInformationRegistry.getInstance(primaryOwner.getIdentifier())
          .getLockingInformation()
          .get(createUnitWrapper(wrapper));

        return Objects.requireNonNullElseGet(lockedInstances, HashSet::new);
    }

    private Set<CompoundInstance> getValueInformationInstances(@NotNull final ICompoundContainer<?> wrapper)
    {
        final Set<CompoundInstance> valueInstances = CompoundInformationRegistry.getInstance(primaryOwner.getIdentifier())
          .getValueInformation()
          .get(createUnitWrapper(wrapper));

        return Objects.requireNonNullElseGet(valueInstances, HashSet::new);
    }

    public List<IAnalysisOwner> getOwners()
    {
        return owners;
    }
}
