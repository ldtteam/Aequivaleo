package com.ldtteam.aequivaleo.api.compound.container.registry;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class ICompoundContainerTypeManagerTest
{

    private ICompoundContainerFactoryManager manager = mock(ICompoundContainerFactoryManager.class);
    private MockedStatic<IAequivaleoAPI> apiMock;

    @Before
    public void setUp() throws Exception
    {
        apiMock = mockStatic(IAequivaleoAPI.class);
        final IAequivaleoAPI api = mock(IAequivaleoAPI.class);
        when(IAequivaleoAPI.getInstance()).thenReturn(api);
        when(api.getCompoundContainerFactoryManager()).thenReturn(manager);
    }

    @After
    public void tearDown() throws Exception
    {
        apiMock.close();
    }

    @Test
    public void getInstance()
    {
        Assert.assertEquals(manager, ICompoundContainerFactoryManager.getInstance());
    }
}