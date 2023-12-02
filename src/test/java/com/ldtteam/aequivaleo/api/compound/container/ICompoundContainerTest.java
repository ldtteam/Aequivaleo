package com.ldtteam.aequivaleo.api.compound.container;

import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

public class ICompoundContainerTest
{
    private final ICompoundContainer<?> target = new DefaultTestContainer();

    @Test
    public void canBeLoadedFromDisk()
    {
        assertFalse(target.canBeLoadedFromDisk());
    }

    @Test
    public void getContentAsFileName()
    {
        assertThrows(UnsupportedOperationException.class, target::getContentAsFileName);
    }

    private static final class DefaultTestContainer implements ICompoundContainer<DefaultTestContainer> {

        @Override
        public boolean isValid()
        {
            return true;
        }

        @Override
        public DefaultTestContainer contents()
        {
            return this;
        }

        @Override
        public Double contentsCount()
        {
            return 1d;
        }
        
        @Override
        public ICompoundContainerType<DefaultTestContainer> type() {
            return null;
        }
        
        @Override
        public int compareTo(@NotNull final ICompoundContainer<?> o)
        {
            return 0;
        }
    }
}