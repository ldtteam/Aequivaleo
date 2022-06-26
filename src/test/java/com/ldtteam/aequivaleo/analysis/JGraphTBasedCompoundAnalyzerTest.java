package com.ldtteam.aequivaleo.analysis;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.compound.information.ICompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.mediation.IMediationCandidate;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.registry.ISyncedRegistry;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.information.CompoundInformationRegistry;
import com.ldtteam.aequivaleo.config.CommonConfiguration;
import com.ldtteam.aequivaleo.config.Configuration;
import com.ldtteam.aequivaleo.config.ServerConfiguration;
import com.ldtteam.aequivaleo.testing.compound.container.testing.StringCompoundContainer;
import com.ldtteam.aequivaleo.testing.recipe.equivalency.TestingEquivalencyRecipe;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor({"net.minecraft.world.level.Level"})
@PowerMockIgnore({"jdk.internal.reflect.*", "org.apache.log4j.*", "org.apache.commons.logging.*", "javax.management.*", "org.apache.logging.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({Aequivaleo.class, ModList.class})
public class JGraphTBasedCompoundAnalyzerTest
{
    ResourceKey<Level>           key;
    JGraphTBasedCompoundAnalyzer analyzer;


    ICompoundType      typeUnknownIsZero  = mock(ICompoundType.class);
    ICompoundTypeGroup groupUnknownIsZero = mock(ICompoundTypeGroup.class);

    ICompoundType      typeUnknownIsInvalid = mock(ICompoundType.class);
    ICompoundTypeGroup groupUnknownIsInvalid = mock(ICompoundTypeGroup.class);

    ICompoundInformationRegistry input;

    @Rule
    public TestName currentTestName = new TestName();

    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    @Before
    public void setUp()
    {
        key = mock(ResourceKey.class);
        when(key.location()).thenReturn(new ResourceLocation(Constants.MOD_ID, currentTestName.getMethodName().toLowerCase()));
        analyzer = new JGraphTBasedCompoundAnalyzer(Lists.newArrayList(new TestAnalysisOwner(currentTestName, key)), true, false);

        input = CompoundInformationRegistry.getInstance(key);

        mockStatic(Aequivaleo.class, ModList.class);
        Aequivaleo mod = mock(Aequivaleo.class);
        when(Aequivaleo.getInstance()).thenReturn(mod);

        Configuration config = mock(Configuration.class);
        ServerConfiguration serverConfig = mock(ServerConfiguration.class);
        ForgeConfigSpec.BooleanValue alwaysFalseConfig = mock(ForgeConfigSpec.BooleanValue.class);
        when(alwaysFalseConfig.get()).thenReturn(false);
        serverConfig.exportGraph = alwaysFalseConfig;
        serverConfig.writeResultsToLog = alwaysFalseConfig;
        when(config.getServer()).thenReturn(serverConfig);

        CommonConfiguration commonConfiguration = mock(CommonConfiguration.class);
        ForgeConfigSpec.BooleanValue alwaysTrueConfig = mock(ForgeConfigSpec.BooleanValue.class);
        when(alwaysTrueConfig.get()).thenReturn(true);
        commonConfiguration.debugAnalysisLog = alwaysTrueConfig;
        when(config.getCommon()).thenReturn(commonConfiguration);

        when(mod.getConfiguration()).thenReturn(config);

        List<ICompoundContainerFactory<?>> containerFactories = ImmutableList.of(new StringCompoundContainer.Factory());
        ModRegistries.CONTAINER_FACTORY = Suppliers.memoize(() -> mock(IForgeRegistry.class));
        when(ModRegistries.CONTAINER_FACTORY.get().iterator()).thenReturn(containerFactories.iterator());
        CompoundContainerFactoryManager.getInstance().bake();

        when(typeUnknownIsZero.getGroup()).thenReturn(groupUnknownIsZero);
        when(typeUnknownIsZero.toString()).thenReturn("Type:Zero");
        when(typeUnknownIsZero.getRegistryName()).thenReturn(new ResourceLocation(Constants.MOD_ID, currentTestName.getMethodName().toLowerCase() + "_zero"));
        when(groupUnknownIsZero.canContributeToRecipeAsInput(any(), any())).thenReturn(true);
        when(groupUnknownIsZero.isValidFor(any(), any())).thenReturn(true);
        when(groupUnknownIsZero.canContributeToRecipeAsOutput(any(), any())).thenReturn(true);
        when(groupUnknownIsZero.getMediationEngine()).thenReturn(context -> Optional.of(context.getCandidates()
                             .stream()
                             .map(IMediationCandidate::getValues)
                             .filter(i -> !i.isEmpty())
                             .min((left, right) -> (int) (left
                                                            .stream()
                                                            .mapToDouble(CompoundInstance::getAmount)
                                                            .sum() - right
                                                                       .stream()
                                                                       .mapToDouble(CompoundInstance::getAmount)
                                                                       .sum()))
                             .orElse(Sets.newHashSet())
        ));
        when(groupUnknownIsZero.shouldIncompleteRecipeBeProcessed(any())).thenReturn(true);

        when(typeUnknownIsInvalid.getGroup()).thenReturn(groupUnknownIsInvalid);
        when(typeUnknownIsInvalid.toString()).thenReturn("Type:Invalid");
        when(typeUnknownIsZero.getRegistryName()).thenReturn(new ResourceLocation(Constants.MOD_ID, currentTestName.getMethodName().toLowerCase() + "_invalid"));
        when(groupUnknownIsInvalid.canContributeToRecipeAsInput(any(), any())).thenReturn(true);
        when(groupUnknownIsInvalid.isValidFor(any(), any())).thenReturn(true);
        when(groupUnknownIsInvalid.canContributeToRecipeAsOutput(any(), any())).thenReturn(true);
        when(groupUnknownIsInvalid.getMediationEngine()).thenReturn(context -> {
            if (!context.areTargetParentsAnalyzed())
                return Optional.of(Collections.emptySet());

            return Optional.of(context
                                 .getCandidates()
                                 .stream()
                                 .min((o1, o2) -> {
                                     if (o1.isSourceIncomplete() && !o2.isSourceIncomplete())
                                         return 1;

                                     if (!o1.isSourceIncomplete() && o2.isSourceIncomplete())
                                         return -1;

                                     if (o1.getValues().isEmpty() && !o2.getValues().isEmpty())
                                         return 1;

                                     if (!o1.getValues().isEmpty() && o2.getValues().isEmpty())
                                         return -1;

                                     return (int) (o1.getValues().stream().mapToDouble(CompoundInstance::getAmount).sum() -
                                                                                           o2.getValues().stream().mapToDouble(CompoundInstance::getAmount).sum());
                                 })
              .map(IMediationCandidate::getValues)
              .orElse(Sets.newHashSet()));
        });

        when(groupUnknownIsInvalid.shouldIncompleteRecipeBeProcessed(any())).thenReturn(false);

        ISyncedRegistry<ICompoundType> typeReg = mock(ISyncedRegistry.class);
        when(typeReg.getSynchronizationIdOf(any(ICompoundType.class))).thenAnswer((Answer<Integer>) invocation -> Lists.newArrayList(typeUnknownIsZero, typeUnknownIsInvalid).indexOf(invocation.getArgument(0)));
        when(typeReg.getAllKnownRegistryNames()).thenReturn(Sets.newHashSet(new ResourceLocation("zero"), new ResourceLocation("invalid")));
        when(typeReg.iterator()).thenAnswer((Answer<Iterator<ICompoundType>>) invocation -> Sets.newHashSet(typeUnknownIsZero, typeUnknownIsInvalid).iterator());
        ModRegistries.COMPOUND_TYPE = () -> typeReg;

        final ModList modList = mock(ModList.class);
        when(modList.getMods()).thenReturn(Collections.emptyList());
        when(ModList.get()).thenReturn(modList);
    }

    @After
    public void tearDown()
    {
        CompoundInformationRegistry.getInstance(key).reset();
        EquivalencyRecipeRegistry.getInstance(key).reset();
    }

    @Test
    public void testSetSimpleValue()
    {
        input.registerLocking("A", s(cz( 1.0)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz( 1.0)), result.get(cc("A")));
    }

    @Test
    public void testSetBaseValueWithoutInputValueShouldNotProduceAnything()
    {
        input.registerBase("A", s(cz( 1.0)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(), result.getOrDefault(cc("A"), s()));
    }

    @Test
    public void testSetBaseValueWithLockingInputShouldTakeLocking()
    {
        input.registerLocking("A", s(cz( 1.0)));
        input.registerBase("A", s(ci(1.0)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.getOrDefault(cc("A"), s()));
    }

    @Test
    public void testSetBaseValueWithValueShouldCombine()
    {
        input.registerValue("A", s(cz( 1.0)));
        input.registerBase("A", s(ci(1.0)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1), ci(1)), result.getOrDefault(cc("A"), s()));
    }

    @Test
    public void testSetBaseValueWithValueShouldAdd()
    {
        input.registerValue("A", s(cz( 1.0)));
        input.registerBase("A", s(cz(1.0)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(2)), result.getOrDefault(cc("A"), s()));
    }

    @Test
    public void testSimpleCraftingBenchRecipe()
    {
        input.registerValue("log", s(cz( 32.0)));

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
        input.registerValue("a1", s(cz( 1)));

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
        input.registerValue("a1", s(cz( 1)));

        registerRecipe("4x a1 to 1x c4", s(cc("a1", 4)), s(cc("c4", 1)));
        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));

        input.registerLocking("b2", s(cz( 20)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(20)), result.get(cc("b2")));
        assertEquals(s(cz(4)), result.get(cc("c4")));
    }

    @Test
    public void testGenerateValuesSimpleFixedDoNotInherit() {
        input.registerValue("a1", s(cz( 1)));
        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));
        registerRecipe("2x b2 to 1x c4", s(cc("b2", 2)), s(cc("c4", 1)));
        input.registerValue("b2", s(cz( 0)));
        input.registerLocking("b2", s(cz( 20)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(20)), result.get(cc("b2")));
        assertEquals(s(cz(0)), result.get(cc("c4")));
    }

    @Test
    public void testGenerateValuesSimpleSelectMinValueWithDependency() {
        input.registerValue("a1", s(cz( 1)));
        input.registerValue("b2", s(cz( 2)));
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
        input.registerValue("planks", s(cz( 1)));
        registerRecipe("1x wood to 4x planks", s(cc("wood", 1)), s(cc("planks", 4)));
        registerRecipe("4x planks to 1x workbench", s(cc("planks", 4)), s(cc("workbench", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertNull( result.get(cc("wood")));
        assertEquals(s(cz(1)), result.get(cc("planks")));
        assertEquals(s(cz(4)), result.get(cc("workbench")));
    }

    @Test
    public void testGenerateValuesWood() {
        for (char i : "ABCD".toCharArray()) {
            input.registerValue("wood" + i, s(cz( 32)));
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
        assertNull( result.get(cc("wood")));
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
        input.registerValue("a1", s(cz( 1)));

        registerRecipe("1x a1 to 1x b1", s(cc("a1", 1)), s(cc("b1", 1)));
        registerRecipe("1x b1 to 1x c1", s(cc("b1", 1)), s(cc("c1", 1)));


        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(1)), result.get(cc("b1")));
        assertEquals(s(cz(1)), result.get(cc("c1")));
    }

    @Test
    public void testGenerateValuesDeepInvalidConversionWithZeroAssumption() {
        input.registerValue("a1", s(cz( 1)));

        registerRecipe("(1x a1 + 1x invalid1) to 1x b", s(cc("a1", 1), cc("invalid1", 1)), s(cc("b", 1)));
        registerRecipe("(1x a1 + 1x invalid2) to 1x invalid1", s(cc("a1", 1), cc("invalid2", 1)), s(cc("invalid1", 1)));
        registerRecipe("(1x a1 + 1x invalid3) to 1x invalid2", s(cc("a1", 1), cc("invalid3", 1)), s(cc("invalid2", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(3)), result.get(cc("b")));
        assertEquals(s(cz(2)), result.get(cc("invalid1")));
        assertEquals(s(cz(1)), result.get(cc("invalid2")));
        assertNull( result.get(cc("invalid3")));
    }

    @Test
    public void testGenerateValuesDeepInvalidConversionWithInvalidAssumption() {
        input.registerValue("a1", s(ci( 1)));

        registerRecipe("(1x a1 + 1x invalid1) to 1x b", s(cc("a1", 1), cc("invalid1", 1)), s(cc("b", 1)));
        registerRecipe("(1x a1 + 1x invalid2) to 1x invalid1", s(cc("a1", 1), cc("invalid2", 1)), s(cc("invalid1", 1)));
        registerRecipe("(1x a1 + 1x invalid3) to 1x invalid2", s(cc("a1", 1), cc("invalid3", 1)), s(cc("invalid2", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(ci(1)), result.get(cc("a1")));
        assertNull( result.get(cc("b")));
        assertNull( result.get(cc("invalid1")));
        assertNull( result.get(cc("invalid2")));
        assertNull( result.get(cc("invalid3")));
    }

    @Test
    public void testGenerateValuesDeepInvalidConversionWithJoinedAnalysis() {
        input.registerValue("a1", s(ci( 1), cz(1)));

        registerRecipe("(1x a1 + 1x invalid1) to 1x b", s(cc("a1", 1), cc("invalid1", 1)), s(cc("b", 1)));
        registerRecipe("(1x a1 + 1x invalid2) to 1x invalid1", s(cc("a1", 1), cc("invalid2", 1)), s(cc("invalid1", 1)));
        registerRecipe("(1x a1 + 1x invalid3) to 1x invalid2", s(cc("a1", 1), cc("invalid3", 1)), s(cc("invalid2", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(ci(1), cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(3)), result.get(cc("b")));
        assertEquals(s(cz(2)), result.get(cc("invalid1")));
        assertEquals(s(cz(1)), result.get(cc("invalid2")));
        assertNull( result.get(cc("invalid3")));
    }

    @Test
    public void testGenerateValuesMultiRecipeDeepInvalidWithZeroAssumption() {
        input.registerValue("a1", s(cz( 1)));

        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));
        registerRecipe("1x invalid1 to 1x b2", s(cc("invalid1", 1)), s(cc("b", 1)));
        registerRecipe("(1x a1 + 1x invalid2) to 1x invalid1", s(cc("a1", 1), cc("invalid2", 1)), s(cc("invalid1", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(2)), result.get(cc("b2")));
        assertEquals(s(cz(1)), result.get(cc("invalid1")));
        assertNull( result.get(cc("invalid2")));
    }
    
    @Test
    public void testGenerateValuesMultiRecipeDeepInvalidWithInvalidAssumption() {
        input.registerValue("a1", s(ci( 1)));

        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));
        registerRecipe("1x invalid1 to 1x b2", s(cc("invalid1", 1)), s(cc("b", 1)));
        registerRecipe("(1x a1 + 1x invalid2) to 1x invalid1", s(cc("a1", 1), cc("invalid2", 1)), s(cc("invalid1", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(ci(1)), result.get(cc("a1")));
        assertEquals(s(ci(2)), result.get(cc("b2")));
        assertNull( result.get(cc("invalid1")));
        assertNull( result.get(cc("invalid2")));
    }

    @Test
    public void testGenerateValuesMultiRecipeDeepInvalidWithJoinedAnalysis() {
        input.registerValue("a1", s(cz( 1), ci(1)));

        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));
        registerRecipe("1x invalid1 to 1x b2", s(cc("invalid1", 1)), s(cc("b", 1)));
        registerRecipe("(1x a1 + 1x invalid2) to 1x invalid1", s(cc("a1", 1), cc("invalid2", 1)), s(cc("invalid1", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1), ci(1)), result.get(cc("a1")));
        assertEquals(s(cz(2), ci(2)), result.get(cc("b2")));
        assertEquals(s(cz(1)), result.get(cc("invalid1")));
        assertNull( result.get(cc("invalid2")));
    }

    @Test
    public void testGenerateValuesMultiRecipesInvalidIngredientWithZeroAssumption() {
        input.registerValue("a1", s(cz( 1)));

        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));
        registerRecipe("1x invalid to 1x b2", s(cc("invalid", 1)), s(cc("b2", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(2)), result.get(cc("b2")));
        assertNull( result.get(cc("invalid")));
    }

    @Test
    public void testGenerateValuesMultiRecipesInvalidIngredientWithInvalidAssumption() {
        input.registerValue("a1", s(ci( 1)));

        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));
        registerRecipe("1x invalid to 1x b2", s(cc("invalid", 1)), s(cc("b2", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(ci(1)), result.get(cc("a1")));
        assertEquals(s(ci(2)), result.get(cc("b2")));
        assertNull( result.get(cc("invalid")));
    }

    @Test
    public void testGenerateValuesMultiRecipesInvalidIngredientWithJoinedAnalysis() {
        input.registerValue("a1", s(cz( 1), ci(1)));

        registerRecipe("2x a1 to 1x b2", s(cc("a1", 2)), s(cc("b2", 1)));
        registerRecipe("1x invalid to 1x b2", s(cc("invalid", 1)), s(cc("b2", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1), ci(1)), result.get(cc("a1")));
        assertEquals(s(cz(2), ci(2)), result.get(cc("b2")));
        assertNull(result.get(cc("invalid")));
    }

    @Test
    public void testGenerateValuesMultiRecipeSourcesInvalidIngredientWithZeroAssumption() {
        input.registerValue("a1", s(cz( 2)));
        input.registerValue("b2", s(cz( 1)));

        registerRecipe("(1x b2 + 1x invalid2) to 1x d2", s(cc("b2"), cc("invalid2")), s(cc("d2")));
        registerRecipe("(1x a1 + 1x invalid1) to 1x c1", s(cc("a1"), cc("invalid1")), s(cc("c1")));
        registerRecipe("1x d2 to 1x c1", s(cc("d2")), s(cc("c1")));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(2)), result.get(cc("a1")));
        assertEquals(s(cz(1)), result.get(cc("b2")));
        assertEquals(s(cz(1)), result.get(cc("d2")));
        assertNull( result.get(cc("invalid2")));
        assertNull( result.get(cc("invalid1")));
        assertEquals(s(cz(1)), result.get(cc("c1")));
    }


    @Test
    public void testGenerateValuesMultiRecipeSourcesInvalidIngredientWithInvalidAssumption() {
        input.registerValue("a1", s(ci( 2)));
        input.registerValue("b2", s(ci( 1)));

        registerRecipe("(1x b2 + 1x invalid2) to 1x d2", s(cc("b2"), cc("invalid2")), s(cc("d2")));
        registerRecipe("(1x a1 + 1x invalid1) to 1x c1", s(cc("a1"), cc("invalid1")), s(cc("c1")));
        registerRecipe("1x d2 to 1x c1", s(cc("d2")), s(cc("c1")));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(ci(2)), result.get(cc("a1")));
        assertEquals(s(ci(1)), result.get(cc("b2")));
        assertNull( result.get(cc("d2")));
        assertNull( result.get(cc("invalid2")));
        assertNull( result.get(cc("invalid1")));
        assertNull( result.get(cc("c1")));
    }


    @Test
    public void testGenerateValuesMultiRecipeSourcesInvalidIngredientWithJoinedAnalysis() {
        input.registerValue("a1", s(cz( 2), ci(2)));
        input.registerValue("b2", s(cz( 1), ci(1)));

        registerRecipe("(1x b2 + 1x invalid2) to 1x d2", s(cc("b2"), cc("invalid2")), s(cc("d2")));
        registerRecipe("(1x a1 + 1x invalid1) to 1x c1", s(cc("a1"), cc("invalid1")), s(cc("c1")));
        registerRecipe("1x d2 to 1x c1", s(cc("d2")), s(cc("c1")));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(2), ci(2)), result.get(cc("a1")));
        assertEquals(s(cz(1), ci(1)), result.get(cc("b2")));
        assertEquals(s(cz(1)), result.get(cc("d2")));
        assertNull( result.get(cc("invalid2")));
        assertNull( result.get(cc("invalid1")));
        assertEquals(s(cz(1)), result.get(cc("c1")));
    }

    @Test
    public void testGenerateValuesCycleRecipe() {
        input.registerValue("a1", s(cz( 1)));

        registerRecipe("1x a1 to 1x cycle-1", s(cc("a1")), s(cc("cycle-1")));
        registerRecipe("1x cycle-1 to 1x cycle-2", s(cc("cycle-1")), s(cc("cycle-2")));
        registerRecipe("1x cycle-2 to 1x cycle-1", s(cc("cycle-2")), s(cc("cycle-1")));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(1)), result.get(cc("cycle-1")));
        assertEquals(s(cz(1)), result.get(cc("cycle-2")));
    }

    @Test
    public void testGenerateValuesBigCycleRecipe() {
        input.registerValue("a1", s(cz( 1)));

        registerRecipe("1x a1 to 1x cycle-1", s(cc("a1")), s(cc("cycle-1")));
        registerRecipe("1x cycle-1 to 1x cycle-2", s(cc("cycle-1")), s(cc("cycle-2")));
        registerRecipe("1x cycle-2 to 1x cycle-3", s(cc("cycle-2")), s(cc("cycle-3")));
        registerRecipe("1x cycle-3 to 1x cycle-4", s(cc("cycle-3")), s(cc("cycle-4")));
        registerRecipe("1x cycle-4 to 1x cycle-5", s(cc("cycle-4")), s(cc("cycle-5")));
        registerRecipe("1x cycle-5 to 1x cycle-1", s(cc("cycle-5")), s(cc("cycle-1")));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(cz(1)), result.get(cc("a1")));
        assertEquals(s(cz(1)), result.get(cc("cycle-1")));
        assertEquals(s(cz(1)), result.get(cc("cycle-2")));
        assertEquals(s(cz(1)), result.get(cc("cycle-3")));
        assertEquals(s(cz(1)), result.get(cc("cycle-4")));
        assertEquals(s(cz(1)), result.get(cc("cycle-5")));
    }

    @Test
    public void testGenerateValuesFuelAndMatter() {
        final String coal = "coal";
        final String aCoal = "alchemicalCoal";
        final String aCoalBlock = "alchemicalCoalBlock";
        final String mFuel = "mobiusFuel";
        final String mFuelBlock = "mobiusFuelBlock";
        final String aFuel = "aeternalisFuel";
        final String aFuelBlock = "aeternalisFuelBlock";
        String repeat;
        
        input.registerValue(coal, s(ci( 128)));
        addConversion(1, aCoal, Arrays.asList(coal, coal, coal, coal));
        addConversion(4, aCoal, Collections.singletonList(mFuel));
        addConversion(9, aCoal, Collections.singletonList(aCoalBlock));
        repeat = aCoal;
        addConversion(1, aCoalBlock, Arrays.asList(repeat, repeat, repeat, repeat, repeat, repeat, repeat, repeat, repeat));

        addConversion(1, mFuel, Arrays.asList(aCoal, aCoal, aCoal, aCoal));
        addConversion(4, mFuel, Collections.singletonList(aFuel));
        addConversion(9, mFuel, Collections.singletonList(mFuelBlock));
        repeat = mFuel;
        addConversion(1, mFuelBlock, Arrays.asList(repeat, repeat, repeat, repeat, repeat, repeat, repeat, repeat, repeat));

        addConversion(1, aFuel, Arrays.asList(mFuel, mFuel, mFuel, mFuel));
        addConversion(9, aFuel, Collections.singletonList(aFuelBlock));
        repeat = aFuel;
        addConversion(1, aFuelBlock, Arrays.asList(repeat, repeat, repeat, repeat, repeat, repeat, repeat, repeat, repeat));

        input.registerValue("diamondBlock", s(ci( 73728L)));
        final String dMatter = "darkMatter";
        final String dMatterBlock = "darkMatterBlock";

        addConversion(1, dMatter, Arrays.asList(aFuel, aFuel, aFuel, aFuel, aFuel, aFuel, aFuel, aFuel, "diamondBlock"));
        addConversion(1, dMatter, Collections.singletonList(dMatterBlock));
        addConversion(4, dMatterBlock, Arrays.asList(dMatter, dMatter, dMatter, dMatter));

        final String rMatter = "redMatter";
        final String rMatterBlock = "redMatterBlock";
        addConversion(1, rMatter, Arrays.asList(aFuel, aFuel, aFuel, dMatter, dMatter, dMatter, aFuel, aFuel, aFuel));
        addConversion(1, rMatter, Collections.singletonList(rMatterBlock));
        addConversion(4, rMatterBlock, Arrays.asList(rMatter, rMatter, rMatter, rMatter));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(s(ci(128)), result.get(cc(coal)));
        assertEquals(s(ci(512)), result.get(cc(aCoal)));
        assertEquals(s(ci(4608)), result.get(cc(aCoalBlock)));
        assertEquals(s(ci(2048)), result.get(cc(mFuel)));
        assertEquals(s(ci(18432)), result.get(cc(mFuelBlock)));
        assertEquals(s(ci(8192)), result.get(cc(aFuel)));
        assertEquals(s(ci(73728)), result.get(cc(aFuelBlock)));
        assertEquals(s(ci(73728)), result.get(cc("diamondBlock")));
        assertEquals(s(ci(139264)), result.get(cc(dMatter)));
        assertEquals(s(ci(139264)), result.get(cc(dMatterBlock)));
        assertEquals(s(ci(466944)), result.get(cc(rMatter)));
        assertEquals(s(ci(466944)), result.get(cc(rMatterBlock)));
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

    public void registerRecipe(final String name, Set<ICompoundContainer<?>> inputs, Set<ICompoundContainer<?>> containers, Set<ICompoundContainer<?>> outputs)
    {
        EquivalencyRecipeRegistry.getInstance(key).register(
          new TestingEquivalencyRecipe(
            name,
            inputs.stream().map(c -> new SimpleIngredientBuilder().from(c).createIngredient()).collect(Collectors.toSet()),
            containers,
            outputs
          )
        );
    }
    
    public void addConversion(int count, String result, List<String> inputs) {
        final Collection<Collection<String>> groupedInputs = GroupingUtils.groupByUsingList(inputs, Function.identity());
        final Set<ICompoundContainer<?>> inputContainers = groupedInputs
          .stream()
          .map(cs -> cc(cs.iterator().next(), cs.size()))
          .collect(Collectors.toSet());
        
        final String name = groupedInputs
          .stream().map(cs -> String.format("%dx %s", cs.size(), cs.iterator().next()))
          .reduce("", (s, s2) -> {
              final String n = s + " " + s2;
              return n.trim();
          }) + " to " + count + "x " + result;
        
        registerRecipe(name, inputContainers, s(cc(result, count)));
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