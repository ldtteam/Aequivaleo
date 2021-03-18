package com.ldtteam.aequivaleo.api.compound.container;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"jdk.internal.reflect.*", "org.apache.log4j.*", "org.apache.commons.logging.*", "javax.management.*"})
public class ICompoundContainerTest
{

    private ICompoundContainer<?> target = new DefaultTestContainer();

    @Test
    public void canBeLoadedFromDisk()
    {
        assertFalse(target.canBeLoadedFromDisk());
    }

    @Test
    public void getContentAsFileName()
    {
        assertThrows(UnsupportedOperationException.class, () -> target.getContentAsFileName());
    }

    private static final class DefaultTestContainer implements ICompoundContainer<DefaultTestContainer> {

        @Override
        public boolean isValid()
        {
            return true;
        }

        @Override
        public DefaultTestContainer getContents()
        {
            return this;
        }

        @Override
        public Double getContentsCount()
        {
            return 1d;
        }

        @Override
        public int compareTo(@NotNull final ICompoundContainer<?> o)
        {
            return 0;
        }
    }
}