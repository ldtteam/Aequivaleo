package com.ldtteam.aequivaleo.analysis;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.analysis.debug.GraphIOHandler;
import com.ldtteam.aequivaleo.analysis.jgrapht.BuildRecipeGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.*;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.impl.*;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.results.CompoundInstanceSet;
import com.ldtteam.aequivaleo.analysis.jgrapht.aequivaleo.utils.NodeUtils;
import com.ldtteam.aequivaleo.analysis.jgrapht.builder.analysis.BFSAnalysisBuilder;
import com.ldtteam.aequivaleo.analysis.jgrapht.builder.analysis.IAnalysisBuilder;
import com.ldtteam.aequivaleo.analysis.jgrapht.cache.CacheKey;
import com.ldtteam.aequivaleo.analysis.jgrapht.clique.JGraphTCliqueReducer;
import com.ldtteam.aequivaleo.analysis.jgrapht.connection.IConnectionFinder;
import com.ldtteam.aequivaleo.analysis.jgrapht.connection.QueueBasedConnectionFinder;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.ICyclesReducer;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.DFSDirectCycleReducer;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.search.ISearchAction;
import com.ldtteam.aequivaleo.analysis.jgrapht.cycles.direct.trace.ICycleReducingTracer;
import com.ldtteam.aequivaleo.analysis.jgrapht.graph.AequivaleoGraph;
import com.ldtteam.aequivaleo.analysis.jgrapht.graph.DuplicateEdgeException;
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
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;

import java.util.*;

public class JGraphTBasedCompoundAnalyzer {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Object ANALYSIS_LOCK = new Object();

    private final List<IAnalysisOwner> owners;
    private final IAnalysisOwner primaryOwner;
    private final boolean forceReload;
    private final boolean writeCachedData;

    private Map<ICompoundContainer<?>, Set<CompoundInstance>> results = new TreeMap<>();

    public JGraphTBasedCompoundAnalyzer(final List<? extends IAnalysisOwner> owners, final boolean forceReload, final boolean writeCachedData) {
        this.owners = new ArrayList<>(owners);
        this.primaryOwner = owners.get(0);
        this.forceReload = forceReload;
        this.writeCachedData = writeCachedData;

        if (this.primaryOwner == null) {
            throw new IllegalArgumentException("First passed world is null");
        }
    }

    public BuildRecipeGraph createGraph() {
        final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds = new TreeMap<>();

        final IGraph recipeGraph = new AequivaleoGraph();

        final Map<ICompoundContainer<?>, IContainerNode> compoundNodes = new HashMap<>();
        final Map<IRecipeIngredient, IIngredientNode> ingredientNodes = new HashMap<>();

        for (IEquivalencyRecipe recipe : EquivalencyRecipeRegistry.getInstance(primaryOwner.getIdentifier())
                .get()) {
            if (!recipe.isValid()) {
                LOGGER.debug("Skipping invalid recipe: {}", recipe);
                continue;
            }

            if (recipe.getInputs().isEmpty()) {
                LOGGER.warn(String.format("Skipping recipe with no ingredients: %s", recipe));
                continue;
            }

            final RecipeNode recipeGraphNode = new RecipeNode(recipe);

            recipeGraph.addVertex(recipeGraphNode);

            //Process inputs
            for (IRecipeIngredient input : recipe.getInputs()) {
                handleRecipeInput(input, ingredientNodes, recipeGraph, recipeGraphNode, compoundNodes, 1);
            }

            //Process outputs
            for (IRecipeIngredient output : recipe.getRequiredKnownOutputs()) {
                handleRecipeInput(output, ingredientNodes, recipeGraph, recipeGraphNode, compoundNodes, -1);
            }

            //Process outputs
            for (ICompoundContainer<?> output : recipe.getOutputs()) {
                final ICompoundContainer<?> unitOutputWrapper = createUnitWrapper(output);
                if (compoundNodes.putIfAbsent(unitOutputWrapper, new ContainerNode(unitOutputWrapper)) == null) {
                    AnalysisLogHandler.debug(LOGGER, String.format("Added new output node for: %s", output));
                } else {
                    AnalysisLogHandler.debug(LOGGER, String.format("Reused existing output node for: %s", output));
                }

                final ICoreNode outputWrapperGraphNode = compoundNodes.get(unitOutputWrapper);
                recipeGraph.addVertex(outputWrapperGraphNode);

                recipeGraph.addEdge(recipeGraphNode, outputWrapperGraphNode);
                recipeGraph.setEdgeWeight(recipeGraphNode, outputWrapperGraphNode, output.getContentsCount());

                recipeGraphNode.addOutput(outputWrapperGraphNode, output.getContentsCount());
                outputWrapperGraphNode.addInput(recipeGraphNode, output.getContentsCount());
            }
        }

        for (ICompoundContainer<?> valueWrapper : CompoundInformationRegistry.getInstance(primaryOwner.getIdentifier()).getValueInformation().keySet()) {
            IResultsOwningNode node = constructContainerNode(valueWrapper, recipeGraph, compoundNodes);
            recipeGraph.clearIncomingEdgesOf(node);
            node.results().force(CompoundInstanceSet.of(CompoundInformationRegistry.getInstance(primaryOwner.getIdentifier()).getValueInformation().get(valueWrapper)));
        }

        for (ICompoundContainer<?> valueWrapper : CompoundInformationRegistry.getInstance(primaryOwner.getIdentifier()).getBaseInformation().keySet()) {
            IResultsOwningNode node = constructContainerNode(valueWrapper, recipeGraph, compoundNodes);
            recipeGraph.clearIncomingEdgesOf(node);
            node.results().base(CompoundInstanceSet.of(CompoundInformationRegistry.getInstance(primaryOwner.getIdentifier()).getBaseInformation().get(valueWrapper)));
        }

        if (Aequivaleo.getInstance().getConfiguration().getServer().exportGraph.get()) {
            GraphIOHandler.getInstance().export(primaryOwner.getIdentifier().location().toString().replace(":", "_").concat(".json"), recipeGraph);
        }

        final Set<IContainerNode> rootNodes = findRootNodes(recipeGraph);

        final Set<INode> notDefinedGraphNodes = new HashSet<>();
        for (IContainerNode n : rootNodes) {
            if (getLockedInformationInstances(n.contents()).isEmpty() && getValueInformationInstances(n.contents()).isEmpty()) {
                notDefinedGraphNodes.add(n);
            }
        }

        final SourceNode source = new SourceNode();
        recipeGraph.addVertex(source);

        for (IContainerNode rootNode : rootNodes) {
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

    private void handleRecipeInput(IRecipeIngredient input, Map<IRecipeIngredient, IIngredientNode> ingredientNodes, IGraph recipeGraph, RecipeNode recipeGraphNode, Map<ICompoundContainer<?>, IContainerNode> compoundNodes, int factor) {
        final IRecipeIngredient unitIngredient = new SimpleIngredientBuilder().from(input).withCount(1).createIngredient();
        ingredientNodes.putIfAbsent(unitIngredient, new IngredientNode(unitIngredient));

        final ICoreNode inputNode = ingredientNodes.get(unitIngredient);
        if (!recipeGraph.containsVertex(inputNode)) {
            recipeGraph.addVertex(inputNode);
        }

        try {
            recipeGraph.addEdge(inputNode, recipeGraphNode);
            recipeGraph.setEdgeWeight(inputNode, recipeGraphNode, factor * input.getRequiredCount());

            inputNode.addOutput(recipeGraphNode, factor * input.getRequiredCount());
            recipeGraphNode.addInput(inputNode, factor * input.getRequiredCount());
        } catch (DuplicateEdgeException e) {
            final IEdge existingEdge = recipeGraph.getEdge(inputNode, recipeGraphNode);
            final double currentWeight = recipeGraph.getEdgeWeight(existingEdge);
            final double newWeight = currentWeight + (factor * input.getRequiredCount());

            recipeGraph.setEdgeWeight(inputNode, recipeGraphNode, newWeight);

            inputNode.addOutput(recipeGraphNode, newWeight);
            recipeGraphNode.addInput(inputNode, newWeight);
        }

        for (final ICompoundContainer<?> candidate : input.getCandidates()) {
            handleCompoundContainerAsInput(recipeGraph, compoundNodes, inputNode, candidate);
        }
    }

    private static @NotNull IResultsOwningNode constructContainerNode(ICompoundContainer<?> valueWrapper, IGraph recipeGraph, Map<ICompoundContainer<?>, IContainerNode> compoundNodes) {
        IContainerNode node;
        if (!recipeGraph.containsVertex(new ContainerNode(valueWrapper))) {
            node = new ContainerNode(valueWrapper);
            compoundNodes.putIfAbsent(valueWrapper, node);
            recipeGraph.addVertex(node);
        } else {
            node = compoundNodes.get(valueWrapper);
        }

        if (node == null) {
            throw new IllegalStateException("Container node for base information needs to be in the graph node map!");
        }
        return node;
    }

    private IGraph reduceGraph(final IGraph recipeGraph, final SourceNode sourceNode) {

        LOGGER.warn("Starting component detection");

        final IConnectionFinder<IGraph, INode, IEdge> connectivityInspector = new QueueBasedConnectionFinder<>();
        final Set<INode> connectedNodes = connectivityInspector.reachableNodes(recipeGraph, sourceNode);

        LOGGER.warn("Connected nodes: {} out of {}", connectedNodes.size(), recipeGraph.vertexSet().size());

        final Set<INode> nodesToRemove = new HashSet<>(Sets.difference(recipeGraph.vertexSet(), connectedNodes));
        nodesToRemove.forEach(v -> {
            recipeGraph.removeVertex(v);
            LOGGER.debug("Removed node: {}", v);
        });

        LOGGER.info("Removed {} nodes from the graph", nodesToRemove.size());

        LOGGER.warn("Finished component detection");

        LOGGER.warn("Starting clique reduction.");

        final JGraphTCliqueReducer cliqueReducer = new JGraphTCliqueReducer(CliqueNode::new);

        cliqueReducer.reduce(recipeGraph);

        LOGGER.warn("Finished clique reduction.");

        LOGGER.warn("Starting cycle reduction.");

        final IJGraphTBasedCompoundCycleTracer tracer = createTracer();
        final ICyclesReducer<IGraph, INode, IEdge> cyclesReducer = new DFSDirectCycleReducer<>(
                CycleNode::new,
                tracer);

        tracer.start();

        cyclesReducer.reduce(recipeGraph, sourceNode);

        tracer.end();

        LOGGER.warn("Finished cycle reduction.");

        recipeGraph.removeVertex(sourceNode);

        final Set<INode> sourceNodeLinks = findDanglingNodes(recipeGraph);

        recipeGraph.addVertex(sourceNode);

        for (INode rootNode : sourceNodeLinks) {
            recipeGraph.addEdge(sourceNode, rootNode);
            recipeGraph.setEdgeWeight(sourceNode, rootNode, 1d);
        }

        return recipeGraph;
    }

    public void calculate() {
        if (this.primaryOwner == null) {
            throw new IllegalArgumentException("First passed world is null");
        }

        final BuildRecipeGraph buildRecipeGraph = createGraph();
        final IGraph noneReducedGraph = buildRecipeGraph.recipeGraph();
        final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds = buildRecipeGraph.resultingCompounds();
        final Set<INode> notDefinedGraphNodes = buildRecipeGraph.notDefinedGraphNodes();
        final SourceNode source = buildRecipeGraph.sourceNode();

        final CacheKey key = new CacheKey(ModList.get(), noneReducedGraph);
        final int graphHash = key.hashCode();
        if (!forceReload) {
            //We are allowed to lookup cached values
            final Optional<Map<ICompoundContainer<?>, Set<CompoundInstance>>> cachedResults = WorldCacheUtils.loadCachedResults(primaryOwner, graphHash);
            if (cachedResults.isPresent()) {
                LOGGER.warn(String.format("Using cached results for: %s", WorldUtils.formatWorldNames(getOwners())));
                this.results = cachedResults.get();
                LOGGER.warn(String.format("Cached results contained %d entries for: %s", this.results.size(), WorldUtils.formatWorldNames(getOwners())));
                return;
            }
        }

        final IGraph recipeGraph = reduceGraph(noneReducedGraph, source);

        final StatCollector statCollector = new StatCollector(
                WorldUtils.formatWorldNames(getOwners()),
                NodeUtils.nodeCount(recipeGraph.vertexSet())
        );
        final IAnalysisBuilder analysisBuilder = new BFSAnalysisBuilder(recipeGraph, source);
        analysisBuilder.analyse(statCollector);
        statCollector.onCalculationComplete();

        extractCompoundInstancesFromGraph(recipeGraph.vertexSet(), resultingCompounds, notDefinedGraphNodes);

        for (ICompoundContainer<?> valueWrapper : CompoundInformationRegistry.getInstance(primaryOwner.getIdentifier()).getLockingInformation().keySet()) {
            resultingCompounds.compute(valueWrapper, (w, v) -> Objects.requireNonNull(CompoundInformationRegistry.getInstance(primaryOwner.getIdentifier())
                    .getLockingInformation()
                    .get(valueWrapper)));
        }

        if (Aequivaleo.getInstance().getConfiguration().getServer().writeResultsToLog.get()) {
            synchronized (ANALYSIS_LOCK) {
                AequivaleoLogger.startBigWarning(String.format("WARNING: Missing root equivalency data in world: %s", WorldUtils.formatWorldNames(getOwners())));
                for (INode node : notDefinedGraphNodes) {
                    if (node instanceof IContainerNode containerNode) {
                        AequivaleoLogger.bigWarningMessage(String.format("Missing root information for: %s. Removing from recipe graph.",
                                containerNode.contents()));
                    }
                    recipeGraph.removeVertex(node);
                }
                AequivaleoLogger.endBigWarning(String.format("WARNING: Missing root equivalency data in world: %s", WorldUtils.formatWorldNames(getOwners())));

                AequivaleoLogger.startBigWarning(String.format("RESULT: Compound analysis for world: %s", WorldUtils.formatWorldNames(getOwners())));
                for (Map.Entry<ICompoundContainer<?>, Set<CompoundInstance>> entry : resultingCompounds.entrySet()) {
                    ICompoundContainer<?> wrapper = entry.getKey();
                    Set<CompoundInstance> compounds = entry.getValue();
                    if (!compounds.isEmpty()) {
                        AequivaleoLogger.bigWarningMessage("{}: {}", wrapper, compounds);
                    }
                }
                AequivaleoLogger.endBigWarning(String.format("RESULT: Compound analysis for world: %s", WorldUtils.formatWorldNames(getOwners())));
            }
        } else {
            AequivaleoLogger.bigWarningSimple(String.format("Finished the analysis of: %s", WorldUtils.formatWorldNames(getOwners())));
        }

        if (writeCachedData) {
            LOGGER.warn(String.format("Writing results to cache for: %s", WorldUtils.formatWorldNames(getOwners())));
            WorldCacheUtils.writeCachedResults(primaryOwner, graphHash, resultingCompounds);
            LOGGER.warn(String.format("Written %d results to cache for: %s", resultingCompounds.size(), WorldUtils.formatWorldNames(getOwners())));
        }
        this.results = resultingCompounds;
    }

    private void extractCompoundInstancesFromGraph(
            final Collection<? extends INode> vertices,
            final Map<ICompoundContainer<?>, Set<CompoundInstance>> resultingCompounds,
            final Set<INode> notDefinedGraphNodes) {
        for (INode v : vertices) {
            if (v instanceof IInnerNode innerNode) {
                extractCompoundInstancesFromGraph(Lists.newArrayList(innerNode.flatten()), resultingCompounds, notDefinedGraphNodes);
            } else if (v instanceof IContainerNode containerWrapperGraphNode) {
                //We could not find any information on this, possibly due to it being in a different set,
                //Or it is not producible. Register it as a not defined graph node.
                if (containerWrapperGraphNode.results().results().isEmpty()) {
                    notDefinedGraphNodes.add(containerWrapperGraphNode);
                } else {
                    if (!resultingCompounds.containsKey(containerWrapperGraphNode.contents())) {
                        resultingCompounds.putIfAbsent(containerWrapperGraphNode.contents(), new TreeSet<>());
                    }

                    resultingCompounds.get(containerWrapperGraphNode.contents())
                            .addAll(containerWrapperGraphNode.results().results());
                }
            }
        }
    }

    private void handleCompoundContainerAsInput(
            final Graph<INode, IEdge> recipeGraph,
            final Map<ICompoundContainer<?>, IContainerNode> nodes,
            final ICoreNode target,
            final ICompoundContainer<?> candidate) {
        final ICompoundContainer<?> unitWrapper = createUnitWrapper(candidate);
        if (nodes.putIfAbsent(unitWrapper, new ContainerNode(unitWrapper)) == null) {
            AnalysisLogHandler.debug(LOGGER, String.format("Added new input node for: %s", candidate));
        } else {
            AnalysisLogHandler.debug(LOGGER, String.format("Reused existing input node for: %s", candidate));
        }

        final IContainerNode candidateNode = nodes.get(unitWrapper);

        recipeGraph.addVertex(candidateNode);

        if (!recipeGraph.containsEdge(candidateNode, target)) {
            recipeGraph.addEdge(candidateNode, target);
            recipeGraph.setEdgeWeight(candidateNode, target, 1);
            candidateNode.addOutput(target, 1);
            target.addInput(candidateNode, 1);
        }
    }

    public Map<ICompoundContainer<?>, Set<CompoundInstance>> calculateAndGet() {
        calculate();
        return results;
    }

    private ICompoundContainer<?> createUnitWrapper(@NotNull final ICompoundContainer<?> wrapper) {
        if (wrapper.getContentsCount() == 1d) {
            return wrapper;
        }

        return CompoundContainerFactoryManager.getInstance().wrapInContainer(wrapper.getContents(), 1d);
    }

    private Set<IContainerNode> findRootNodes(@NotNull final Graph<INode, IEdge> graph) {
        Set<IContainerNode> set = new HashSet<>();
        for (INode v : findDanglingNodes(graph)) {
            if (v instanceof IContainerNode containerNode) {
                set.add(containerNode);
            }
        }
        return set;
    }

    private Set<INode> findDanglingNodes(@NotNull final Graph<INode, IEdge> graph) {
        Set<INode> set = new HashSet<>();
        for (INode v : graph
                .vertexSet()) {
            if (!(v instanceof IFreeNode) && graph.incomingEdgesOf(v).isEmpty()) {
                set.add(v);
            }
        }
        return set;
    }

    private Set<CompoundInstance> getLockedInformationInstances(@NotNull final ICompoundContainer<?> wrapper) {
        final Set<CompoundInstance> lockedInstances = CompoundInformationRegistry.getInstance(primaryOwner.getIdentifier())
                .getLockingInformation()
                .get(createUnitWrapper(wrapper));

        return Objects.requireNonNullElseGet(lockedInstances, HashSet::new);
    }

    private Set<CompoundInstance> getValueInformationInstances(@NotNull final ICompoundContainer<?> wrapper) {
        final Set<CompoundInstance> valueInstances = CompoundInformationRegistry.getInstance(primaryOwner.getIdentifier())
                .getValueInformation()
                .get(createUnitWrapper(wrapper));

        return Objects.requireNonNullElseGet(valueInstances, HashSet::new);
    }

    public List<IAnalysisOwner> getOwners() {
        return owners;
    }

    private IJGraphTBasedCompoundCycleTracer createTracer() {
        if (Aequivaleo.getInstance().getConfiguration().getCommon().traceCycleLog.get()) {
            return new FullTracer();
        }

        if (Aequivaleo.getInstance().getConfiguration().getCommon().debugCycleLog.get()) {
            return new CycleOnlyTracer();
        }

        if (Aequivaleo.getInstance().getConfiguration().getCommon().outputCycleCount.get()) {
            return new CycleCountingTracer();
        }

        return IJGraphTBasedCompoundCycleTracer.noop();
    }

    private interface IJGraphTBasedCompoundCycleTracer extends ICycleReducingTracer<IGraph, INode, IEdge> {
        default void start() {
            AequivaleoLogger.startBigWarning("Cycles");
        }

        default void end() {
            AequivaleoLogger.endBigWarning("Cycles");
        }

        static IJGraphTBasedCompoundCycleTracer noop() {
            return new IJGraphTBasedCompoundCycleTracer() {

                @Override
                public void start() {

                }

                @Override
                public void end() {

                }

                @Override
                public void onEncounterVertex(INode vertex) {

                }

                @Override
                public void onExitVertex(INode vertex) {

                }

                @Override
                public void onCycleFound(Collection<INode> cycle) {

                }

                @Override
                public void onActionAdded(ISearchAction<IGraph, INode, IEdge> action) {

                }

                @Override
                public void onActionRemoved(ISearchAction<IGraph, INode, IEdge> action) {

                }
            };
        }
    }

    private static final class FullTracer implements IJGraphTBasedCompoundCycleTracer {

        @Override
        public void onEncounterVertex(INode vertex) {
            AequivaleoLogger.bigWarningMessage("Encounter: " + vertex);
        }

        @Override
        public void onExitVertex(INode vertex) {
            AequivaleoLogger.bigWarningMessage("Exit: " + vertex);
        }

        @Override
        public void onCycleFound(Collection<INode> cycle) {
            AequivaleoLogger.bigWarningMessage("Cycle: " + cycle);
        }

        @Override
        public void onActionAdded(ISearchAction<IGraph, INode, IEdge> action) {
            AequivaleoLogger.bigWarningMessage("Action added: " + action);
        }

        @Override
        public void onActionRemoved(ISearchAction<IGraph, INode, IEdge> action) {
            AequivaleoLogger.bigWarningMessage("Action removed: " + action);
        }
    }

    private static final class CycleOnlyTracer implements IJGraphTBasedCompoundCycleTracer {

        @Override
        public void onEncounterVertex(INode vertex) {

        }

        @Override
        public void onExitVertex(INode vertex) {

        }

        @Override
        public void onCycleFound(Collection<INode> cycle) {
            AequivaleoLogger.bigWarningMessage("Cycle: " + cycle);
        }

        @Override
        public void onActionAdded(ISearchAction<IGraph, INode, IEdge> action) {

        }

        @Override
        public void onActionRemoved(ISearchAction<IGraph, INode, IEdge> action) {

        }
    }

    private static final class CycleCountingTracer implements IJGraphTBasedCompoundCycleTracer {

        private int count = 0;

        @Override
        public void onEncounterVertex(INode vertex) {

        }

        @Override
        public void onExitVertex(INode vertex) {

        }

        @Override
        public void onCycleFound(Collection<INode> cycle) {
            count++;
            if (count % 1000 == 0) {
                AequivaleoLogger.bigWarningMessage("Cycle count: " + count);
            }
        }

        @Override
        public void onActionAdded(ISearchAction<IGraph, INode, IEdge> action) {

        }

        @Override
        public void onActionRemoved(ISearchAction<IGraph, INode, IEdge> action) {

        }
    }
}
