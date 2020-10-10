package com.ldtteam.aequivaleo.analyzer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.compound.information.locked.ILockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.information.locked.LockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.config.Configuration;
import com.ldtteam.aequivaleo.config.ServerConfiguration;
import com.ldtteam.aequivaleo.testing.compound.container.testing.StringCompoundContainer;
import com.ldtteam.aequivaleo.testing.recipe.equivalency.TestingEquivalencyRecipe;
import junit.framework.TestCase;
import net.minecraft.util.RegistryKey;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.simple.SimpleLoggerContextFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("net.minecraft.world.World")
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({Aequivaleo.class})
public class JGraphTBasedCompoundAnalyzerTest extends TestCase {

    RegistryKey<net.minecraft.world.World> key;
    net.minecraft.world.World world;
    JGraphTBasedCompoundAnalyzer analyzer;

    ICompoundType type = mock(ICompoundType.class);
    ICompoundTypeGroup group = mock(ICompoundTypeGroup.class);
    @Before
    public void setUp() {
        //Prevent »Cannot run program "infocmp"« Error
        LogManager.setFactory(new SimpleLoggerContextFactory() );

        key =  mock(RegistryKey.class);
        world = mock(net.minecraft.world.World.class);
        when(world.getDimensionKey()).thenReturn(key);
        analyzer = new JGraphTBasedCompoundAnalyzer(world);

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

        when(type.getGroup()).thenReturn(group);
        when(group.canContributeToRecipeAsInput(any(), any())).thenReturn(true);
        when(group.isValidFor(any(), any())).thenReturn(true);
        when(group.canContributeToRecipeAsOutput(any(), any(), any())).thenReturn(true);
    }

    @After
    public void tearDown() {
        LockedCompoundInformationRegistry.getInstance(key).reset();
        EquivalencyRecipeRegistry.getInstance(key).reset();
    }

    @Test
    public void testSetSimpleValue() {
        ILockedCompoundInformationRegistry lockedCInfoRegistry = LockedCompoundInformationRegistry.getInstance(key);
        lockedCInfoRegistry.registerLocking("A", ImmutableSet.of(new CompoundInstance(type, 1.0)));
        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

        assertEquals(ImmutableSet.of(new CompoundInstance(type, 1.0)), result.get(cc("A")));
    }

    @Test
    public void simpleCraftingBenchRecipe() {
        ILockedCompoundInformationRegistry lockedCInfoRegistry = LockedCompoundInformationRegistry.getInstance(key);
        lockedCInfoRegistry.registerLocking("log", ImmutableSet.of(new CompoundInstance(type, 32.0)));

        registerRecipe(s(cc("log", 1)), s(cc("plank", 4)));
        registerRecipe(s(cc("plank", 4)), s(cc("workbench", 1)));

        final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();
        assertEquals(s(ci(32)), result.get(cc("log")));
        assertEquals(s(ci(8)), result.get(cc( "plank")));
        assertEquals(s(ci(32)), result.get(cc("workbench")));

    }

    public void registerRecipe(Set<ICompoundContainer<?>> inputs, Set<ICompoundContainer<?>> outputs) {
        EquivalencyRecipeRegistry.getInstance(key).register(
          new TestingEquivalencyRecipe(
            inputs.stream().map(c -> new SimpleIngredientBuilder().from(c).createIngredient()).collect(Collectors.toSet()),
            Collections.emptySet(),
            outputs
          )
        );
    }

    public ICompoundContainer<?> cc(String s) {
        return cc(s, 1);
    }

    public ICompoundContainer<?> cc(String s, double count) {
        return new StringCompoundContainer(s, count);
    }

    public <T> Set<T> s(T ... args) {
        return ImmutableSet.copyOf((T[])args);
    }

    public CompoundInstance ci(double amount) {
        return new CompoundInstance(type, amount);
    }
}