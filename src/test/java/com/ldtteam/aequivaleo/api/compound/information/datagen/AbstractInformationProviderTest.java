package com.ldtteam.aequivaleo.api.compound.information.datagen;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerFactory;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.compound.information.datagen.data.CompoundInstanceData;
import com.ldtteam.aequivaleo.api.compound.information.datagen.data.CompoundInstanceRef;
import com.ldtteam.aequivaleo.api.util.Constants;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.testing.compound.container.testing.LoadableStringCompoundContainer;
import com.ldtteam.aequivaleo.testing.compound.container.testing.StringCompoundContainer;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.IForgeRegistry;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@SuppressWarnings({"unchecked"})
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"jdk.internal.reflect.*", "org.apache.log4j.*", "org.apache.commons.logging.*", "javax.management.*"})
@PrepareForTest({IAequivaleoAPI.class, DataProvider.class, ICompoundContainerFactoryManager.class})
public class AbstractInformationProviderTest {

    AbstractInformationProvider saveTestTarget;

    @Before
    public void setUp() {
        mockStatic(IAequivaleoAPI.class, ICompoundContainerFactoryManager.class);
        final IAequivaleoAPI api = mock(IAequivaleoAPI.class);
        when(IAequivaleoAPI.getInstance()).thenReturn(api);
        when(api.getGson(ICondition.IContext.EMPTY)).thenReturn(new Gson());

        when(ICompoundContainerFactoryManager.getInstance()).thenReturn(CompoundContainerFactoryManager.getInstance());

        final ICompoundContainerFactory<?> factory = new LoadableStringCompoundContainer.Factory();
        List<ICompoundContainerFactory<?>> containerFactories = ImmutableList.of(factory);
        ModRegistries.CONTAINER_FACTORY = Suppliers.memoize(() -> mock(IForgeRegistry.class));
        when(ModRegistries.CONTAINER_FACTORY.get().iterator()).thenReturn(containerFactories.iterator());
        when(ModRegistries.CONTAINER_FACTORY.get().getValue(any())).thenAnswer((Answer<ICompoundContainerFactory<?>>) invocation -> factory);
        CompoundContainerFactoryManager.getInstance().bake();

        saveTestTarget = mock(AbstractInformationProvider.class);
    }

    @Test
    public void assureActCallsCalculate() throws IOException {
        final CachedOutput cache = mock(CachedOutput.class);
        final AbstractInformationProvider target = mock(AbstractInformationProvider.class);
        final AbstractInformationProvider.WorldData generalData = mock(AbstractInformationProvider.WorldData.class);

        when(generalData.getPath()).thenReturn("test");
        when(generalData.getDataToWrite()).thenReturn(Collections.emptyMap());
        when(generalData.getWorldId()).thenReturn(new ResourceLocation(Constants.MOD_ID, "test"));

        when(target.getWorldDataMap()).thenReturn(Collections.emptyMap());
        when(target.getGeneralData()).thenReturn(generalData);

        doNothing().when(target).calculateDataToSave();
        doCallRealMethod().when(target).run(any());

        target.run(cache);
        verify(target, times(1)).calculateDataToSave();
    }

    @Test
    public void assureWriteDataCallsGetPaths() throws IOException {
        final CachedOutput cache = mock(CachedOutput.class);
        final AbstractInformationProvider target = mock(AbstractInformationProvider.class);
        final AbstractInformationProvider.WorldData generalData = mock(AbstractInformationProvider.WorldData.class);

        when(generalData.getPath()).thenReturn("test");
        when(generalData.getDataToWrite()).thenReturn(Collections.emptyMap());
        when(generalData.getWorldId()).thenReturn(new ResourceLocation(Constants.MOD_ID, "test"));

        when(target.getWorldDataMap()).thenReturn(Collections.emptyMap());
        when(target.getGeneralData()).thenReturn(generalData);

        doCallRealMethod().when(target).writeData(any(), any(), any());

        target.writeData(cache, new Gson(), generalData);
        verify(target, times(1)).getPathsToWrite(anyString());
    }

    @Test
    public void assureWorldDataIsCalledForOneAdditionalWorlds() throws IOException {
        final CachedOutput cache = mock(CachedOutput.class);
        final AbstractInformationProvider target = mock(AbstractInformationProvider.class);

        final AbstractInformationProvider.WorldData generalData = mock(AbstractInformationProvider.WorldData.class);
        when(generalData.getPath()).thenReturn("test");
        when(generalData.getDataToWrite()).thenReturn(Collections.emptyMap());
        when(generalData.getWorldId()).thenReturn(new ResourceLocation(Constants.MOD_ID, "test"));

        final Map<ResourceLocation, AbstractInformationProvider.WorldData> mockWorldDataMap = Maps.newHashMap();
        final AbstractInformationProvider.WorldData testWorldData = mock(AbstractInformationProvider.WorldData.class);
        when(testWorldData.getPath()).thenReturn("test");
        when(testWorldData.getDataToWrite()).thenReturn(Collections.emptyMap());
        when(testWorldData.getWorldId()).thenReturn(new ResourceLocation(Constants.MOD_ID, "test"));
        mockWorldDataMap.put(new ResourceLocation(Constants.MOD_ID, "test"), testWorldData);

        when(target.getWorldDataMap()).thenReturn(mockWorldDataMap);
        when(target.getGeneralData()).thenReturn(generalData);

        doNothing().when(target).calculateDataToSave();

        doCallRealMethod().when(target).run(any());

        target.run(cache);
        verify(target, times(2)).writeData(any(), any(), any());
    }

    @Test
    public void assureWorldDataIsCalledForMultipleAdditionalWorlds() throws IOException {
        final CachedOutput cache = mock(CachedOutput.class);
        final AbstractInformationProvider target = mock(AbstractInformationProvider.class);

        final AbstractInformationProvider.WorldData generalData = mock(AbstractInformationProvider.WorldData.class);
        when(generalData.getPath()).thenReturn("test");
        when(generalData.getDataToWrite()).thenReturn(Collections.emptyMap());
        when(generalData.getWorldId()).thenReturn(new ResourceLocation(Constants.MOD_ID, "test"));

        final Map<ResourceLocation, AbstractInformationProvider.WorldData> mockWorldDataMap = Maps.newHashMap();
        final AbstractInformationProvider.WorldData testWorldData = mock(AbstractInformationProvider.WorldData.class);
        when(testWorldData.getPath()).thenReturn("test");
        when(testWorldData.getDataToWrite()).thenReturn(Collections.emptyMap());
        when(testWorldData.getWorldId()).thenReturn(new ResourceLocation(Constants.MOD_ID, "test"));
        final AbstractInformationProvider.WorldData testWorldData2 = mock(AbstractInformationProvider.WorldData.class);
        when(testWorldData2.getPath()).thenReturn("test2");
        when(testWorldData2.getDataToWrite()).thenReturn(Collections.emptyMap());
        when(testWorldData2.getWorldId()).thenReturn(new ResourceLocation(Constants.MOD_ID, "test2"));
        mockWorldDataMap.put(new ResourceLocation(Constants.MOD_ID, "test"), testWorldData);
        mockWorldDataMap.put(new ResourceLocation(Constants.MOD_ID, "test2"), testWorldData2);

        when(target.getWorldDataMap()).thenReturn(mockWorldDataMap);
        when(target.getGeneralData()).thenReturn(generalData);

        doNothing().when(target).calculateDataToSave();

        doCallRealMethod().when(target).run(any());

        target.run(cache);
        verify(target, times(3)).writeData(any(), any(), any());
    }

    @Test
    public void assureWriteDataRetrievesDataFromWorldData() throws IOException {
        final CachedOutput cache = mock(CachedOutput.class);
        final AbstractInformationProvider target = mock(AbstractInformationProvider.class);
        final AbstractInformationProvider.WorldData generalData = mock(AbstractInformationProvider.WorldData.class);

        when(generalData.getPath()).thenReturn("test");
        when(generalData.getDataToWrite()).thenReturn(Collections.emptyMap());
        when(generalData.getWorldId()).thenReturn(new ResourceLocation(Constants.MOD_ID, "test"));

        when(target.getWorldDataMap()).thenReturn(Collections.emptyMap());
        when(target.getGeneralData()).thenReturn(generalData);
        when(target.getPathsToWrite(anyString())).thenReturn(ImmutableSet.<Path>builder().add(new File("test").toPath()).build());

        doCallRealMethod().when(target).writeData(any(), any(), any());

        target.writeData(cache, new Gson(), generalData);
        verify(generalData, times(1)).getDataToWrite();
    }

    @Test
    public void assureDataProviderSaveIsCalled() throws Exception {
        mockStatic(DataProvider.class);
        doNothing().when(DataProvider.class, "saveStable", any(), any(), any());

        final CachedOutput cache = mock(CachedOutput.class);
        final AbstractInformationProvider target = mock(AbstractInformationProvider.class);
        final AbstractInformationProvider.WorldData generalData = mock(AbstractInformationProvider.WorldData.class);

        final Map<Set<ICompoundContainer<?>>, AbstractInformationProvider.DataSpec> mockedDataToWrite = Maps.newHashMap();
        mockedDataToWrite.put(
                Sets.newLinkedHashSet(new LoadableStringCompoundContainer("test", 1d)),
                new AbstractInformationProvider.DataSpec(
                        CompoundInstanceData.Mode.REPLACING,
                        Sets.newLinkedHashSet(
                                new CompoundInstanceRef(new ResourceLocation(Constants.MOD_ID, "test"), 1d)
                        ),
                        Sets.newHashSet()
                )
        );

        when(generalData.getPath()).thenReturn("test");
        when(generalData.getDataToWrite()).thenReturn(mockedDataToWrite);
        when(generalData.getWorldId()).thenReturn(new ResourceLocation(Constants.MOD_ID, "test"));

        when(target.getWorldDataMap()).thenReturn(Collections.emptyMap());
        when(target.getGeneralData()).thenReturn(generalData);
        when(target.getPathsToWrite(anyString())).thenReturn(ImmutableSet.<Path>builder().add(new File("test").toPath()).build());

        doCallRealMethod().when(target).writeData(any(), any(), any());

        target.writeData(cache, new Gson(), generalData);
        verifyStatic(DataProvider.class, times(1));
        DataProvider.saveStable(any(), any(), any());
    }

    @Test
    public void assureDataProviderSaveIsNotCalled() throws Exception {
        mockStatic(DataProvider.class);
        doNothing().when(DataProvider.class, "saveStable", any(), any(), any());

        final CachedOutput cache = mock(CachedOutput.class);
        final AbstractInformationProvider target = mock(AbstractInformationProvider.class);
        final AbstractInformationProvider.WorldData generalData = mock(AbstractInformationProvider.WorldData.class);

        final Map<Set<ICompoundContainer<?>>, AbstractInformationProvider.DataSpec> mockedDataToWrite = Maps.newHashMap();
        mockedDataToWrite.put(
                Sets.newLinkedHashSet(new StringCompoundContainer("test", 1d)),
                new AbstractInformationProvider.DataSpec(
                        CompoundInstanceData.Mode.REPLACING,
                        Sets.newLinkedHashSet(
                                new CompoundInstanceRef(new ResourceLocation(Constants.MOD_ID, "test"), 1d)
                        ),
                        Sets.newHashSet()
                )
        );

        when(generalData.getPath()).thenReturn("test");
        when(generalData.getDataToWrite()).thenReturn(mockedDataToWrite);
        when(generalData.getWorldId()).thenReturn(new ResourceLocation(Constants.MOD_ID, "test"));

        when(target.getWorldDataMap()).thenReturn(Collections.emptyMap());
        when(target.getGeneralData()).thenReturn(generalData);
        when(target.getPathsToWrite(anyString())).thenReturn(ImmutableSet.<Path>builder().add(new File("test").toPath()).build());

        doCallRealMethod().when(target).writeData(any(), any(), any());

        target.writeData(cache, new Gson(), generalData);
        verifyStatic(DataProvider.class, times(0));
        DataProvider.saveStable(any(), any(), any());
    }

    @Test
    public void assureDataProviderSaveIsCalledForEachPath() throws Exception {
        mockStatic(DataProvider.class);
        doNothing().when(DataProvider.class, "saveStable", any(), any(), any());

        final CachedOutput cache = mock(CachedOutput.class);
        final AbstractInformationProvider target = mock(AbstractInformationProvider.class);
        final AbstractInformationProvider.WorldData generalData = mock(AbstractInformationProvider.WorldData.class);

        final Map<Set<ICompoundContainer<?>>, AbstractInformationProvider.DataSpec> mockedDataToWrite = Maps.newHashMap();
        mockedDataToWrite.put(
                Sets.newLinkedHashSet(new LoadableStringCompoundContainer("test", 1d)),
                new AbstractInformationProvider.DataSpec(
                        CompoundInstanceData.Mode.REPLACING,
                        Sets.newLinkedHashSet(
                                new CompoundInstanceRef(new ResourceLocation(Constants.MOD_ID, "test"), 1d)
                        ),
                        Sets.newHashSet()
                )
        );

        when(generalData.getPath()).thenReturn("test");
        when(generalData.getDataToWrite()).thenReturn(mockedDataToWrite);
        when(generalData.getWorldId()).thenReturn(new ResourceLocation(Constants.MOD_ID, "test"));

        when(target.getWorldDataMap()).thenReturn(Collections.emptyMap());
        when(target.getGeneralData()).thenReturn(generalData);
        when(target.getPathsToWrite(anyString())).thenReturn(ImmutableSet.<Path>builder().add(new File("test").toPath()).add(new File("test2").toPath()).build());

        doCallRealMethod().when(target).writeData(any(), any(), any());

        target.writeData(cache, new Gson(), generalData);
        verifyStatic(DataProvider.class, times(2));
        DataProvider.saveStable(any(), any(), any());
    }

    @Test
    public void assureDataProviderSaveIsCalledForEachEntry() throws Exception {
        mockStatic(DataProvider.class);
        doNothing().when(DataProvider.class, "saveStable", any(), any(), any());

        final CachedOutput cache = mock(CachedOutput.class);
        final AbstractInformationProvider target = mock(AbstractInformationProvider.class);
        final AbstractInformationProvider.WorldData generalData = mock(AbstractInformationProvider.WorldData.class);

        final Map<Set<ICompoundContainer<?>>, AbstractInformationProvider.DataSpec> mockedDataToWrite = Maps.newHashMap();
        mockedDataToWrite.put(
                Sets.newLinkedHashSet(new LoadableStringCompoundContainer("test", 1d)),
                new AbstractInformationProvider.DataSpec(
                        CompoundInstanceData.Mode.REPLACING,
                        Sets.newLinkedHashSet(
                                new CompoundInstanceRef(new ResourceLocation(Constants.MOD_ID, "test"), 1d)
                        ),
                        Sets.newHashSet()
                )
        );
        mockedDataToWrite.put(
                Sets.newLinkedHashSet(new LoadableStringCompoundContainer("test2", 1d)),
                new AbstractInformationProvider.DataSpec(
                        CompoundInstanceData.Mode.REPLACING,
                        Sets.newLinkedHashSet(
                                new CompoundInstanceRef(new ResourceLocation(Constants.MOD_ID, "test2"), 1d)
                        ),
                        Sets.newHashSet()
                )
        );

        when(generalData.getPath()).thenReturn("test");
        when(generalData.getDataToWrite()).thenReturn(mockedDataToWrite);
        when(generalData.getWorldId()).thenReturn(new ResourceLocation(Constants.MOD_ID, "test"));

        when(target.getWorldDataMap()).thenReturn(Collections.emptyMap());
        when(target.getGeneralData()).thenReturn(generalData);
        when(target.getPathsToWrite(anyString())).thenReturn(ImmutableSet.<Path>builder().add(new File("test").toPath()).build());

        doCallRealMethod().when(target).writeData(any(), any(), any());

        target.writeData(cache, new Gson(), generalData);
        verifyStatic(DataProvider.class, times(2));
        DataProvider.saveStable(any(), any(), any());
    }

    @Test
    public void assureDataProviderSaveIsCalledForEachPathAndEachEntry() throws Exception {
        mockStatic(DataProvider.class);
        doNothing().when(DataProvider.class, "saveStable", any(), any(), any());

        final CachedOutput cache = mock(CachedOutput.class);
        final AbstractInformationProvider target = mock(AbstractInformationProvider.class);
        final AbstractInformationProvider.WorldData generalData = mock(AbstractInformationProvider.WorldData.class);

        final Map<Set<ICompoundContainer<?>>, AbstractInformationProvider.DataSpec> mockedDataToWrite = Maps.newHashMap();
        mockedDataToWrite.put(
                Sets.newLinkedHashSet(new LoadableStringCompoundContainer("test", 1d)),
                new AbstractInformationProvider.DataSpec(
                        CompoundInstanceData.Mode.REPLACING,
                        Sets.newLinkedHashSet(
                                new CompoundInstanceRef(new ResourceLocation(Constants.MOD_ID, "test"), 1d)
                        ),
                        Sets.newHashSet()
                )
        );
        mockedDataToWrite.put(
                Sets.newLinkedHashSet(new LoadableStringCompoundContainer("test2", 1d)),
                new AbstractInformationProvider.DataSpec(
                        CompoundInstanceData.Mode.REPLACING,
                        Sets.newLinkedHashSet(
                                new CompoundInstanceRef(new ResourceLocation(Constants.MOD_ID, "test2"), 1d)
                        ),
                        Sets.newHashSet()
                )
        );

        when(generalData.getPath()).thenReturn("test");
        when(generalData.getDataToWrite()).thenReturn(mockedDataToWrite);
        when(generalData.getWorldId()).thenReturn(new ResourceLocation(Constants.MOD_ID, "test"));

        when(target.getWorldDataMap()).thenReturn(Collections.emptyMap());
        when(target.getGeneralData()).thenReturn(generalData);
        when(target.getPathsToWrite(anyString())).thenReturn(ImmutableSet.<Path>builder().add(new File("test").toPath()).add(new File("test2").toPath()).build());

        doCallRealMethod().when(target).writeData(any(), any(), any());

        target.writeData(cache, new Gson(), generalData);
        verifyStatic(DataProvider.class, times(4));
        DataProvider.saveStable(any(), any(), any());
    }


    @Test
    public void assureDataProviderSaveIsNotCalledWithEmptyContainerSet() throws Exception {
        mockStatic(DataProvider.class);
        doNothing().when(DataProvider.class, "saveStable", any(), any(), any());

        final CachedOutput cache = mock(CachedOutput.class);
        final AbstractInformationProvider target = mock(AbstractInformationProvider.class);
        final AbstractInformationProvider.WorldData generalData = mock(AbstractInformationProvider.WorldData.class);

        final Map<Set<ICompoundContainer<?>>, AbstractInformationProvider.DataSpec> mockedDataToWrite = Maps.newHashMap();
        mockedDataToWrite.put(
                Collections.emptySet(),
                new AbstractInformationProvider.DataSpec(
                        CompoundInstanceData.Mode.REPLACING,
                        Sets.newLinkedHashSet(
                                new CompoundInstanceRef(new ResourceLocation(Constants.MOD_ID, "test"), 1d)
                        ),
                        Sets.newHashSet()
                )
        );

        when(generalData.getPath()).thenReturn("test");
        when(generalData.getDataToWrite()).thenReturn(mockedDataToWrite);
        when(generalData.getWorldId()).thenReturn(new ResourceLocation(Constants.MOD_ID, "test"));

        when(target.getWorldDataMap()).thenReturn(Collections.emptyMap());
        when(target.getGeneralData()).thenReturn(generalData);
        when(target.getPathsToWrite(anyString())).thenReturn(ImmutableSet.<Path>builder().add(new File("test").toPath()).build());

        doCallRealMethod().when(target).writeData(any(), any(), any());

        target.writeData(cache, new Gson(), generalData);
        verifyStatic(DataProvider.class, times(0));
        DataProvider.saveStable(any(), any(), any());
    }
}