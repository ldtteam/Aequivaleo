package com.ldtteam.aequivaleo.analysis.jgrapht.cycles;

import com.google.common.collect.ImmutableList;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data.IRecipeIngredientType;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.config.CommonConfiguration;
import com.ldtteam.aequivaleo.config.Configuration;
import com.ldtteam.aequivaleo.config.ServerConfiguration;
import com.ldtteam.aequivaleo.recipe.equivalency.ingredient.SimpleIngredient;
import com.ldtteam.aequivaleo.recipe.equivalency.ingredient.TagIngredient;
import com.ldtteam.aequivaleo.testing.compound.container.testing.StringCompoundContainer;
import net.minecraft.core.Registry;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

public class CycleReductionTests {

    private static final class Node {
        private final List<?> contents;

        public Node(final List<?> contents) {
            this.contents = contents;
        }

        @Override
        public String toString() {
            if (contents.isEmpty()) {
                return "[]";
            }

            if (contents.size() == 1) {
                return contents.get(0).toString();
            }

            return "[%s]".formatted(contents.stream().map(Object::toString).reduce("%s, %s"::formatted).orElse(""));
        }
    }

    private static Node toCycle(Graph<Node, String> graph, List<Node> nodes) {
        return new Node(nodes);
    }


    MockedStatic<Aequivaleo> aequivaleoMock;
    MockedStatic<ModList> modListMock;

    Graph<Node, String> graph;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp()
    {
        graph = new DefaultDirectedWeightedGraph<>(null, new Supplier<>() {
            int innerClass = 0;

            @Override
            public String get() {
                return innerClass++ + "";
            }
        });

        aequivaleoMock = mockStatic(Aequivaleo.class);
        modListMock = mockStatic(ModList.class);
        Aequivaleo mod = mock(Aequivaleo.class);
        when(Aequivaleo.getInstance()).thenReturn(mod);

        Configuration config = mock(Configuration.class);
        ServerConfiguration serverConfig = mock(ServerConfiguration.class);
        ModConfigSpec.BooleanValue alwaysFalseConfig = mock(ModConfigSpec.BooleanValue.class);
        when(alwaysFalseConfig.get()).thenReturn(false);
        serverConfig.exportGraph = alwaysFalseConfig;
        serverConfig.writeResultsToLog = alwaysFalseConfig;
        when(config.getServer()).thenReturn(serverConfig);

        CommonConfiguration commonConfiguration = mock(CommonConfiguration.class);
        ModConfigSpec.BooleanValue alwaysTrueConfig = mock(ModConfigSpec.BooleanValue.class);
        when(alwaysTrueConfig.get()).thenReturn(true);
        commonConfiguration.debugAnalysisLog = alwaysTrueConfig;
        when(config.getCommon()).thenReturn(commonConfiguration);

        when(mod.getConfiguration()).thenReturn(config);

        List<ICompoundContainerType<?>> containerFactories = ImmutableList.of(new StringCompoundContainer.Type());
        ModRegistries.CONTAINER_FACTORY = (Registry<ICompoundContainerType<?>>) mock(Registry.class);
        when(ModRegistries.CONTAINER_FACTORY.iterator()).thenReturn(containerFactories.iterator());
        when(ModRegistries.CONTAINER_FACTORY.byNameCodec()).thenCallRealMethod();
        CompoundContainerFactoryManager.getInstance().bake();

        List<IRecipeIngredientType> ingredientTypes = ImmutableList.of(new SimpleIngredient.Type(), new TagIngredient.Type());
        ModRegistries.RECIPE_INGREDIENT_TYPE = (Registry<IRecipeIngredientType>) mock(Registry.class);
        when(ModRegistries.RECIPE_INGREDIENT_TYPE.iterator()).thenReturn(ingredientTypes.iterator());
        when(ModRegistries.RECIPE_INGREDIENT_TYPE.byNameCodec()).thenCallRealMethod();
    }

    @After
    public void close() {
        aequivaleoMock.close();
        modListMock.close();
    }


    @Test
    public void testSimpleCycle() {
        final ICyclesReducer<Graph<Node, String>, Node, String> cyclesReducer = new SzwarcfiterLauerCyclesReducer<>(
                CycleReductionTests::toCycle,
                (node, neighbor, replacement) -> {
                    System.out.printf("Replacing %s with %s on %s%n", neighbor, replacement, node);
                }
        );

        final Node a = new Node(List.of("a"));
        final Node b = new Node(List.of("b"));
        final Node c = new Node(List.of("c"));
        final Node s = new Node(List.of("s"));

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(s);

        graph.addEdge(a, b, "ab");
        graph.addEdge(b, c, "bc");
        graph.addEdge(c, a, "ca");

        graph.addEdge(s, a, "sa");

        cyclesReducer.reduce(graph, s);

        Assert.assertEquals("([s, [a, b, c]], [0=(s,[a, b, c])])", graph.toString());
    }

    @Test
    public void testSimpleCycleWithTail() {
        final ICyclesReducer<Graph<Node, String>, Node, String> cyclesReducer = new SzwarcfiterLauerCyclesReducer<>(
                CycleReductionTests::toCycle,
                (node, neighbor, replacement) -> {
                    System.out.printf("Replacing %s with %s on %s%n", neighbor, replacement, node);
                }
        );

        final Node s = new Node(List.of("s"));

        final Node a = new Node(List.of("a"));
        final Node b = new Node(List.of("b"));
        final Node c = new Node(List.of("c"));

        final Node m = new Node(List.of("m"));

        graph.addVertex(s);
        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(m);


        graph.addEdge(a, b, "ab");
        graph.addEdge(b, c, "bc");
        graph.addEdge(c, a, "ca");

        graph.addEdge(b, m, "mb");

        graph.addEdge(s, a, "sa");

        cyclesReducer.reduce(graph, s);

        Assert.assertEquals("([s, m, [a, b, c]], [0=(s,[a, b, c]), 1=([a, b, c],m)])", graph.toString());
    }

    @Test
    public void testDoubleCycle() {
        final ICyclesReducer<Graph<Node, String>, Node, String> cyclesReducer = new SzwarcfiterLauerCyclesReducer<>(
                CycleReductionTests::toCycle,
                (node, neighbor, replacement) -> {
                    System.out.printf("Replacing %s with %s on %s%n", neighbor, replacement, node);
                }
        );

        final Node s = new Node(List.of("s"));

        final Node a = new Node(List.of("a"));
        final Node b = new Node(List.of("b"));
        final Node c = new Node(List.of("c"));

        final Node m = new Node(List.of("m"));

        final Node x = new Node(List.of("x"));
        final Node y = new Node(List.of("y"));
        final Node z = new Node(List.of("z"));

        graph.addVertex(s);
        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(m);
        graph.addVertex(x);
        graph.addVertex(y);
        graph.addVertex(z);


        graph.addEdge(a, b, "ab");
        graph.addEdge(b, c, "bc");
        graph.addEdge(c, a, "ca");

        graph.addEdge(b, m, "mb");

        graph.addEdge(m, x, "mx");
        graph.addEdge(x, y, "xy");
        graph.addEdge(y, z, "yz");
        graph.addEdge(z, x, "zx");

        graph.addEdge(s, a, "sa");

        cyclesReducer.reduce(graph, s);

        Assert.assertEquals("([s, m, [x, y, z], [a, b, c]], [0=(m,[x, y, z]), 1=(s,[a, b, c]), 2=([a, b, c],m)])", graph.toString());
    }

    @Test
    public void testInnerCircle() {
        final ICyclesReducer<Graph<Node, String>, Node, String> cyclesReducer = new SzwarcfiterLauerCyclesReducer<>(
                CycleReductionTests::toCycle,
                (node, neighbor, replacement) -> {
                    System.out.printf("Replacing %s with %s on %s%n", neighbor, replacement, node);
                }
        );

        final Node s = new Node(List.of("s"));

        final Node a = new Node(List.of("a"));
        final Node b = new Node(List.of("b"));
        final Node c = new Node(List.of("c"));
        final Node d = new Node(List.of("d"));
        final Node e = new Node(List.of("e"));
        final Node f = new Node(List.of("f"));
        final Node g = new Node(List.of("g"));
        final Node h = new Node(List.of("h"));

        graph.addVertex(s);
        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);
        graph.addVertex(d);
        graph.addVertex(e);
        graph.addVertex(f);
        graph.addVertex(g);
        graph.addVertex(h);

        graph.addEdge(a, b, "ab");
        graph.addEdge(b, c, "bc");
        graph.addEdge(c, d, "cd");
        graph.addEdge(d, e, "de");
        graph.addEdge(e, f, "ef");
        graph.addEdge(f, g, "fg");
        graph.addEdge(g, h, "gh");
        graph.addEdge(h, a, "ha");

        graph.addEdge(c, f, "cf");
        graph.addEdge(g, b, "gb");

        graph.addEdge(s, a, "sa");

        cyclesReducer.reduce(graph, s);

        Assert.assertEquals("([s, [[[b, c, f, g], d, e], h, a]], [6=(s,[[[b, c, f, g], d, e], h, a])])", graph.toString());
    }
}
