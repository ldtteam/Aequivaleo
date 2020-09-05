package com.ldtteam.aequivaleo.analyzer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.compound.information.locked.ILockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.information.locked.LockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.config.Configuration;
import com.ldtteam.aequivaleo.config.ServerConfiguration;
import junit.framework.TestCase;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.simple.SimpleLoggerContextFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	@Before
	public void setUp() {
		//Prevent »Cannot run program "infocmp"« Error
		LogManager.setFactory(new SimpleLoggerContextFactory() );

		key =  mock(RegistryKey.class);
		world = mock(net.minecraft.world.World.class);
		when(world.func_234923_W_()).thenReturn(key);
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

		List<ICompoundContainerFactory<?>> containerFactories = ImmutableList.of(new StringCompoundContainerFactory());
		ModRegistries.CONTAINER_FACTORY = mock(IForgeRegistry.class);
		when(ModRegistries.CONTAINER_FACTORY.iterator()).thenReturn(containerFactories.iterator());
		CompoundContainerFactoryManager.getInstance().bake();

	}

	@After
	public void tearDown() {
		LockedCompoundInformationRegistry.getInstance(key).reset();
	}

	@Test
	public void testSetSimpleValue() {
		ICompoundType type = mock(ICompoundType.class);

		ILockedCompoundInformationRegistry lockedCInfoRegistry = LockedCompoundInformationRegistry.getInstance(key);
		lockedCInfoRegistry.registerLocking("A", ImmutableSet.of(new CompoundInstance(type, 1.0)));
		final Map<ICompoundContainer<?>, Set<CompoundInstance>> result = analyzer.calculateAndGet();

		System.out.println(result);
		assertEquals(ImmutableSet.of(new CompoundInstance(type, 1.0)), result.getOrDefault(new StringCompoundContainer("A", 1), null));
	}

	static class StringCompoundContainerFactory implements ICompoundContainerFactory<String> {

		@NotNull
		@Override
		public Class<String> getContainedType() {
			return String.class;
		}

		@NotNull
		@Override
		public ICompoundContainer<String> create(@NotNull String inputInstance, double count) {
			return new StringCompoundContainer(inputInstance, count);
		}

		@Override
		public ICompoundContainer<String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return null;
		}

		@Override
		public JsonElement serialize(ICompoundContainer<String> src, Type typeOfSrc, JsonSerializationContext context) {
			return null;
		}

		@Override
		public void write(ICompoundContainer<String> object, PacketBuffer buffer) {

		}

		@Override
		public ICompoundContainer<String> read(PacketBuffer buffer) {
			return null;
		}

		@Override
		public ICompoundContainerFactory<?> setRegistryName(ResourceLocation resourceLocation) {
			return null;
		}

		@Nullable
		@Override
		public ResourceLocation getRegistryName() {
			return null;
		}

		@Override
		public Class<ICompoundContainerFactory<?>> getRegistryType() {
			return null;
		}
	}
	private static class StringCompoundContainer implements ICompoundContainer<String> {

		String content;
		double count;
		public StringCompoundContainer(String content, double count) {
			this.content = content;
			this.count = count;
		}
		@Override
		public String getContents() {
			return content;
		}

		@Override
		public Double getContentsCount() {
			return count;
		}

		@Override
		public int compareTo(@NotNull ICompoundContainer<?> o) {
			if (o == this) {
				return 0;
			}
			if (o instanceof StringCompoundContainer) {
				return content.compareTo(((StringCompoundContainer) o).content);
			}
			return -1;
		}

		public String toString() {
			return String.format("[s=\"%s\",c=%f]", content, count);
		}

		@Override
		public int hashCode() {
			return content.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof StringCompoundContainer) {
				return compareTo((StringCompoundContainer) obj) == 0;
			}
			return false;
		}
	}
}
