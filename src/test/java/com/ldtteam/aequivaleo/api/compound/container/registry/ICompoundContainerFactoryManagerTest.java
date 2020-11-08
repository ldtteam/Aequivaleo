package com.ldtteam.aequivaleo.api.compound.container.registry;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"jdk.internal.reflect.*", "org.apache.log4j.*", "org.apache.commons.logging.*", "javax.management.*"})
@PrepareForTest({IAequivaleoAPI.class})
public class ICompoundContainerFactoryManagerTest
{

    private ICompoundContainerFactoryManager manager = mock(ICompoundContainerFactoryManager.class);

    @Before
    public void setUp() throws Exception
    {
        mockStatic(IAequivaleoAPI.class);
        final IAequivaleoAPI api = mock(IAequivaleoAPI.class);
        when(IAequivaleoAPI.getInstance()).thenReturn(api);
        when(api.getCompoundContainerFactoryManager()).thenReturn(manager);
    }

    @Test
    public void getInstance()
    {
        Assert.assertEquals(manager, ICompoundContainerFactoryManager.getInstance());
    }
}