package com.ldtteam.aequivaleo.analyzer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.compound.information.locked.ILockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.information.locked.LockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.config.Configuration;
import com.ldtteam.aequivaleo.config.ServerConfiguration;
import com.ldtteam.aequivaleo.testing.compound.container.testing.StringCompoundContainer;
import com.ldtteam.aequivaleo.testing.recipe.equivalency.TestingEquivalencyRecipe;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.IForgeRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("net.minecraft.world.World")
@PowerMockIgnore({"jdk.internal.reflect.*", "org.apache.log4j.*", "org.apache.commons.logging.*", "javax.management.*"})
@PrepareForTest({Aequivaleo.class})
public class JGraphTBasedCompoundAnalyzerTest
{

    RegistryKey<World> key;
    World                                  world;
    JGraphTBasedCompoundAnalyzer           analyzer;


    ICompoundType      typeUnknownIsZero  = mock(ICompoundType.class);
    ICompoundTypeGroup groupUnknownIsZero = mock(ICompoundTypeGroup.class);

    ICompoundType      typeUnknownIsInvalid = mock(ICompoundType.class);
    ICompoundTypeGroup groupUnknownIsInvalid = mock(ICompoundTypeGroup.class);

    ILockedCompoundInformationRegistry input;

    @Rule
    public TestName currentTestName = new TestName();

    @SuppressWarnings("unchecked")
    @Before
    public void setUp()
    {
        key = mock(RegistryKey.class);
        when(key.getLocation()).thenReturn(new ResourceLocation(Constants.MOD_ID, currentTestName.getMethodName().toLowerCase()));
        world = mock(net.minecraft.world.World.class);
        when(world.getDimensionKey()).thenReturn(key);
        analyzer = new JGraphTBasedCompoundAnalyzer(world);

        input = LockedCompoundInformationRegistry.getInstance(key);

        mockStatic(Aequivaleo.class);
        Aequivaleo mod = mock(Aequivaleo.class);
        when(Aequivaleo.getInstance()).thenReturn(mod);


        Configuration config = mock(Configuration.class);
        ServerConfiguration serverConfig = mock(ServerConfiguration.class);
        ForgeConfigSpec.BooleanValue alwaysFalseConfig = mock(ForgeConfigSpec.BooleanValue.class);
        when(alwaysFalseConfig.get()).thenReturn(false);
        serverConfig.exportGraph = alwaysFalseConfig;
        serverConfig.writeResultsToLog = alwaysFalseConfig;
        when(config.getServer()).thenReturn(serverConfig);
        when(mod.getConfiguration()).thenReturn(config);

        List<ICompoundContainerFactory<?>> containerFactories = ImmutableList.of(new StringCompoundContainer.Factory());
        ModRegistries.CONTAINER_FACTORY = mock(IForgeRegistry.class);
        when(ModRegistries.CONTAINER_FACTORY.iterator()).thenReturn(containerFactories.iterator());
        CompoundContainerFactoryManager.getInstance().bake();

        when(typeUnknownIsZero.getGroup()).thenReturn(groupUnknownIsZero);
        when(typeUnknownIsZero.toString()).thenReturn("Type:Zero");
        when(typeUnknownIsZero.getRegistryName()).thenReturn(new ResourceLocation(Constants.MOD_ID, currentTestName.getMethodName().toLowerCase() + "_zero"));
        when(groupUnknownIsZero.canContributeToRecipeAsInput(any(), any())).thenReturn(true);
        when(groupUnknownIsZero.isValidFor(any(), any())).thenReturn(true);
        when(groupUnknownIsZero.canContributeToRecipeAsOutput(any(), any(), any())).thenReturn(true);
        when(groupUnknownIsZero.determineResult(any(), any()))
          .thenAnswer((Answer<Set<CompoundInstance>>) invocation -> {
            final Set<Set<CompoundInstance>> data = invocation.getArgumentAt(0, Set.class);

            return data
                     .stream()
                     .filter(i -> !i.isEmpty())
                     .min((left, right) -> (int) (left
                                                             .stream()
                                                             .mapToDouble(CompoundInstance::getAmount)
                                                             .sum() - right
                                                                        .stream()
                                                                        .mapToDouble(CompoundInstance::getAmount)
                                                                        .sum()))
                     .orElse(Sets.newHashSet());
        });
        when(groupUnknownIsZero.shouldIncompleteRecipeBeProcessed(any())).thenReturn(true);

        when(typeUnknownIsInvalid.getGroup()).thenReturn(groupUnknownIsInvalid);
        when(typeUnknownIsInvalid.toString()).thenReturn("Type:Invalid");
        when(typeUnknownIsZero.getRegistryName()).thenReturn(new ResourceLocation(Constants.MOD_ID, currentTestName.getMethodName().toLowerCase() + "_invalid"));
        when(groupUnknownIsInvalid.canContributeToRecipeAsInput(any(), any())).thenReturn(true);
        when(groupUnknownIsInvalid.isValidFor(any(), any())).thenReturn(true);
        when(groupUnknownIsInvalid.canContributeToRecipeAsOutput(any(), any(), any())).thenReturn(true);
        when(groupUnknownIsInvalid.determineResult(any(), any())).thenAnswer((Answer<Set<CompoundInstance>>) invocation -> {
            final Set<Set<CompoundInstance>> data = invocation.getArgumentAt(0, Set.class);
            final boolean isComplete = invocation.getArgumentAt(1, Boolean.class);

            if (!isComplete)
                return Collections.emptySet();

            return data
                     .stream()
                     .filter(i -> !i.isEmpty())
                     .min((left, right) -> (int) (left
                                                    .stream()
                                                    .mapToDouble(CompoundInstance::getAmount)
                                                    .sum() - right
                                                               .stream()
                                                               .mapToDouble(CompoundInstance::getAmount)
                                                               .sum()))
                     .orElse(Sets.newHashSet());
        });
        when(groupUnknownIsInvalid.shouldIncompleteRecipeBeProcessed(any())).thenReturn(false);
    }

    @After
    public void tearDown()
    {
        LockedCompoundInformationRegistry.getInstance(key).reset();
        EquivalencyRecipeRegistry.getInstance(key).reset();
    }

    @Test
    public void testSetSimpleValue()
    {
        input.registerLocking("A", ImmutableSet.of(new CompoundInstance(typeUnknownIsZero, 1.0)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();
        assertEquals(ImmutableSet.of(new CompoundInstance(typeUnknownIsZero, 1.0)), result.get(cc("A")));
    }

    @Test
    public void testSimpleCraftingBenchRecipe()
    {
        input.registerValue("log", ImmutableSet.of(new CompoundInstance(typeUnknownIsZero, 32.0)));

        registerRecipe("1x log to 4x plank", s(cc("log", 1)), s(cc("plank", 4)));
        registerRecipe("4x plank to 1x workbench", s(cc("plank", 4)), s(cc("workbench", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();
        assertEquals(s(cz(32)), result.get(cc("log")));
        assertEquals(s(cz(8)), result.get(cc("plank")));
        assertEquals(s(cz(32)), result.get(cc("workbench")));
    }

    @Test
    public void testGenerateValuesSimpleMultiRecipeWithEmptyAlternative()
    {
        input.registerValue("a1", ImmutableSet.of(new CompoundInstance(typeUnknownIsZero, 1)));

        registerRecipe("4x a1 to 1x c4", s(cc("a1", 4)), s(cc("c4", 1)));
        registerRecipe("nothing to 1x c4", s(), s(cc("c4", 1)));
        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();
        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(2)), result.get(cc("b2")));
        assertEquals(s(cz(4)), result.get(cc("c4")));
    }

    @Test
    public void testGenerateValuesSimpleFixedAfterInherit() {
        input.registerValue("a1", ImmutableSet.of(new CompoundInstance(typeUnknownIsZero, 1)));

        registerRecipe("4x a1 to 1x c4", s(cc("a1", 4)), s(cc("c4", 1)));
        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));

        input.registerLocking("b2", ImmutableSet.of(new CompoundInstance(typeUnknownIsZero, 20)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(20)), result.get(cc("b2")));
        assertEquals(s(cz(4)), result.get(cc("c4")));
    }

    @Test
    public void testGenerateValuesSimpleFixedDoNotInherit() {
        input.registerValue("a1", ImmutableSet.of(new CompoundInstance(typeUnknownIsZero, 1)));
        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));
        registerRecipe("2x b2 to 1x c4", s(cc("b2", 2)), s(cc("c4", 1)));
        input.registerValue("b2", ImmutableSet.of(new CompoundInstance(typeUnknownIsZero, 0)));
        input.registerLocking("b2", ImmutableSet.of(new CompoundInstance(typeUnknownIsZero, 20)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(20)), result.get(cc("b2")));
        assertEquals(s(cz(0)), result.get(cc("c4")));
    }

    @Test
    public void testGenerateValuesSimpleSelectMinValueWithDependency() {
        input.registerValue("a1", ImmutableSet.of(new CompoundInstance(typeUnknownIsZero, 1)));
        input.registerValue("b2", ImmutableSet.of(new CompoundInstance(typeUnknownIsZero, 2)));
        registerRecipe("2x a1 to 1x c", s(cc("a1", 2)), s(cc("c", 1)));
        registerRecipe("2x b2 to 1x c", s(cc("b2", 2)), s(cc("c", 1)));
        registerRecipe("1x c to 1x d", s(cc("c", 2)), s(cc("d", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(2)), result.get(cc("b2")));
        assertEquals(s(cz(2)), result.get(cc("c")));
        assertEquals(s(cz(4)), result.get(cc("d")));
    }

    @Test
    public void testGenerateValuesSimpleWoodToWorkBench() {
        input.registerValue("planks", ImmutableSet.of(new CompoundInstance(typeUnknownIsZero, 1)));
        registerRecipe("1x wood to 4x planks", s(cc("wood", 1)), s(cc("planks", 4)));
        registerRecipe("4x planks to 1x workbench", s(cc("planks", 4)), s(cc("workbench", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(), result.get(cc("wood")));
        assertEquals(s(cz(1)), result.get(cc("planks")));
        assertEquals(s(cz(4)), result.get(cc("workbench")));
    }

    @Test
    public void testGenerateValuesWood() {
        for (char i : "ABCD".toCharArray()) {
            input.registerValue("wood" + i, ImmutableSet.of(new CompoundInstance(typeUnknownIsZero, 32)));
            registerRecipe("1x wood" + i + " to 4x planks" + i, s(cc("wood" + i, 1)), s(cc("planks" + i, 4)));
        }

        for (char i : "ABCD".toCharArray()) {
            registerRecipe("1x wood" + " to 4x planks" + i, s(cc("wood", 1)), s(cc("planks" + i, 4)));
        }

        for (char i : "ABCD".toCharArray()) {
            for (char j : "ABCD".toCharArray()) {
                Set<ICompoundContainer<?>> inputs = s(cc("planks" + i, 1), cc("planks" + j, 1));
                if (i == j)
                    inputs = s(cc("planks" + i, 2));

                registerRecipe("(1x planks" + i + " + " + "1x planks" + j +") to 4x stick", inputs, s(cc("stick", 4)));
            }
        }

        registerRecipe("2x planksA to 1x crafting_table", s(cc("planksA", 4)), s(cc("crafting_table", 1)));
        for (char i : "ABCD".toCharArray()) {
            for (char j : "ABCD".toCharArray()) {
                Set<ICompoundContainer<?>> inputs = s(cc("stick", 2), cc("planks" + i, 1), cc("planks" + j, 1));
                if (i == j)
                    inputs = s(cc("stick", 2), cc("planks" + i, 2));

                registerRecipe("(2x stick + 1x planks" + i + " + 2x planks"+ j, inputs, s(cc("wooden_hoe", 1)));
            }
        }

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();
        assertEquals(s(), result.get(cc("wood")));
        for (char i : "ABCD".toCharArray()) {
            assertEquals(s(cz(32)), result.get(cc("wood" + i)));
        }
        for (char i : "ABCD".toCharArray()) {
            assertEquals(s(cz(8)), result.get(cc("planks" + i)));
        }

        assertEquals(s(cz(4)), result.get(cc("stick")));
        assertEquals(s(cz(32)), result.get(cc("crafting_table")));
        assertEquals(s(cz(24)), result.get(cc("wooden_hoe")));
    }

    @Test
    public void testGenerateValuesDeepConversions() {
        input.registerValue("a1", ImmutableSet.of(new CompoundInstance(typeUnknownIsZero, 1)));

        registerRecipe("1x a1 to 1x b1", s(cc("a1", 1)), s(cc("b1", 1)));
        registerRecipe("1x b1 to 1x c1", s(cc("b1", 1)), s(cc("c1", 1)));


        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(1)), result.get(cc("b1")));
        assertEquals(s(cz(1)), result.get(cc("c1")));
    }

    @Test
    public void testGenerateValuesDeepInvalidConversionWithZeroAssumption() {
        input.registerValue("a1", ImmutableSet.of(cz( 1)));

        registerRecipe("(1x a1 + 1x invalid1) to 1x b", s(cc("a1", 1), cc("invalid1", 1)), s(cc("b", 1)));
        registerRecipe("(1x a1 + 1x invalid2) to 1x invalid1", s(cc("a1", 1), cc("invalid2", 1)), s(cc("invalid1", 1)));
        registerRecipe("(1x a1 + 1x invalid3) to 1x invalid2", s(cc("a1", 1), cc("invalid3", 1)), s(cc("invalid2", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(3)), result.get(cc("b")));
        assertEquals(s(cz(2)), result.get(cc("invalid1")));
        assertEquals(s(cz(1)), result.get(cc("invalid2")));
        assertEquals(s(), result.get(cc("invalid3")));
    }

    @Test
    public void testGenerateValuesDeepInvalidConversionWithInvalidAssumption() {
        input.registerValue("a1", ImmutableSet.of(ci( 1)));

        registerRecipe("(1x a1 + 1x invalid1) to 1x b", s(cc("a1", 1), cc("invalid1", 1)), s(cc("b", 1)));
        registerRecipe("(1x a1 + 1x invalid2) to 1x invalid1", s(cc("a1", 1), cc("invalid2", 1)), s(cc("invalid1", 1)));
        registerRecipe("(1x a1 + 1x invalid3) to 1x invalid2", s(cc("a1", 1), cc("invalid3", 1)), s(cc("invalid2", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(ci(1)), result.get(cc("a1")));
        assertEquals(s(), result.get(cc("b")));
        assertEquals(s(), result.get(cc("invalid1")));
        assertEquals(s(), result.get(cc("invalid2")));
        assertEquals(s(), result.get(cc("invalid3")));
    }

    @Test
    public void testGenerateValuesDeepInvalidConversionWithJoinedAnalysis() {
        input.registerValue("a1", ImmutableSet.of(ci( 1), cz(1)));

        registerRecipe("(1x a1 + 1x invalid1) to 1x b", s(cc("a1", 1), cc("invalid1", 1)), s(cc("b", 1)));
        registerRecipe("(1x a1 + 1x invalid2) to 1x invalid1", s(cc("a1", 1), cc("invalid2", 1)), s(cc("invalid1", 1)));
        registerRecipe("(1x a1 + 1x invalid3) to 1x invalid2", s(cc("a1", 1), cc("invalid3", 1)), s(cc("invalid2", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(ci(1), cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(3)), result.get(cc("b")));
        assertEquals(s(cz(2)), result.get(cc("invalid1")));
        assertEquals(s(cz(1)), result.get(cc("invalid2")));
        assertEquals(s(), result.get(cc("invalid3")));
    }

    @Test
    public void testGenerateValuesMultiRecipeDeepInvalidWithZeroAssumption() {
        input.registerValue("a1", ImmutableSet.of(cz( 1)));

        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));
        registerRecipe("1x invalid1 to 1x b2", s(cc("invalid1", 1)), s(cc("b", 1)));
        registerRecipe("(1x a1 + 1x invalid2) to 1x invalid1", s(cc("a1", 1), cc("invalid2", 1)), s(cc("invalid1", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(2)), result.get(cc("b2")));
        assertEquals(s(cz(1)), result.get(cc("invalid1")));
        assertEquals(s(), result.get(cc("invalid2")));
    }
    
    @Test
    public void testGenerateValuesMultiRecipeDeepInvalidWithInvalidAssumption() {
        input.registerValue("a1", ImmutableSet.of(ci( 1)));

        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));
        registerRecipe("1x invalid1 to 1x b2", s(cc("invalid1", 1)), s(cc("b", 1)));
        registerRecipe("(1x a1 + 1x invalid2) to 1x invalid1", s(cc("a1", 1), cc("invalid2", 1)), s(cc("invalid1", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(ci(1)), result.get(cc("a1")));
        assertEquals(s(ci(2)), result.get(cc("b2")));
        assertEquals(s(), result.get(cc("invalid1")));
        assertEquals(s(), result.get(cc("invalid2")));
    }

    @Test
    public void testGenerateValuesMultiRecipeDeepInvalidWithJoinedAnalysis() {
        input.registerValue("a1", ImmutableSet.of(cz( 1), ci(1)));

        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));
        registerRecipe("1x invalid1 to 1x b2", s(cc("invalid1", 1)), s(cc("b", 1)));
        registerRecipe("(1x a1 + 1x invalid2) to 1x invalid1", s(cc("a1", 1), cc("invalid2", 1)), s(cc("invalid1", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1), ci(1)), result.get(cc("a1")));
        assertEquals(s(cz(2), ci(2)), result.get(cc("b2")));
        assertEquals(s(cz(1)), result.get(cc("invalid1")));
        assertEquals(s(), result.get(cc("invalid2")));
    }

    @Test
    public void testGenerateValuesMultiRecipesInvalidIngredientWithZeroAssumption() {
        input.registerValue("a1", ImmutableSet.of(cz( 1)));

        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));
        registerRecipe("1x invalid to 1x b2", s(cc("invalid", 1)), s(cc("b2", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(2)), result.get(cc("b2")));
        assertEquals(s(), result.get(cc("invalid")));
    }

    @Test
    public void testGenerateValuesMultiRecipesInvalidIngredientWithInvalidAssumption() {
        input.registerValue("a1", ImmutableSet.of(ci( 1)));

        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));
        registerRecipe("1x invalid to 1x b2", s(cc("invalid", 1)), s(cc("b2", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(ci(1)), result.get(cc("a1")));
        assertEquals(s(ci(2)), result.get(cc("b2")));
        assertEquals(s(), result.get(cc("invalid")));
    }

    @Test
    public void testGenerateValuesMultiRecipesInvalidIngredientWithJoinedAnalysis() {
        input.registerValue("a1", ImmutableSet.of(cz( 1), ci(1)));

        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));
        registerRecipe("1x invalid to 1x b2", s(cc("invalid", 1)), s(cc("b2", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1), ci(1)), result.get(cc("a1")));
        assertEquals(s(cz(2), ci(2)), result.get(cc("b2")));
        assertEquals(s(), result.get(cc("invalid")));
    }

    public void registerRecipe(final String name, Set<ICompoundContainer<?>> inputs, Set<ICompoundContainer<?>> outputs)
    {
        EquivalencyRecipeRegistry.getInstance(key).register(
          new TestingEquivalencyRecipe(
            name,
            inputs.stream().map(c -> new SimpleIngredientBuilder().from(c).createIngredient()).collect(Collectors.toSet()),
            Collections.emptySet(),
            outputs
          )
        );
    }

    public ICompoundContainer<?> cc(String s)
    {
        return cc(s, 1);
    }

    public ICompoundContainer<?> cc(String s, double count)
    {
        return new StringCompoundContainer(s, count);
    }

    @SafeVarargs
    public final <T> Set<T> s(T... args)
    {
        return ImmutableSet.copyOf(args);
    }

    public CompoundInstance cz(double amount)
    {
        return new CompoundInstance(typeUnknownIsZero, amount);
    }

    public CompoundInstance ci(double amount)
    {
        return new CompoundInstance(typeUnknownIsInvalid, amount);
    }
}