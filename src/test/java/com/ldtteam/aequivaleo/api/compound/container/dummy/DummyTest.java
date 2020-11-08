package com.ldtteam.aequivaleo.api.compound.container.dummy;

import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DummyTest
{

    private JsonObject one;
    private JsonObject two;

    private Dummy targetOne;
    private Dummy targetTwo;

    @Before
    public void setUp() throws Exception
    {
        one = new JsonObject();
        one.addProperty("something", "one");

        two = new JsonObject();
        two.addProperty("something", "two");

        targetOne = new Dummy(one);
        targetTwo = new Dummy(two);
    }

    @Test
    public void getContents()
    {
        assertEquals(targetOne, targetOne.getContents());
        assertEquals(targetTwo, targetTwo.getContents());
    }

    @Test
    public void getContentsCount()
    {
        assertEquals(0D, targetOne.getContentsCount(), 0d);
        assertEquals(0D, targetTwo.getContentsCount(), 0d);
    }

    @Test
    public void canBeLoadedFromDisk()
    {
        assertFalse(targetOne.canBeLoadedFromDisk());
        assertFalse(targetTwo.canBeLoadedFromDisk());
    }

    @Test
    public void getContentAsFileName()
    {
        assertThrows(IllegalStateException.class, () -> targetOne.getContentAsFileName());
        assertThrows(IllegalStateException.class, () -> targetTwo.getContentAsFileName());
    }

    @Test
    public void getOriginalData()
    {
        assertEquals(one, targetOne.getOriginalData());
        assertEquals(two, targetTwo.getOriginalData());
    }

    @Test
    public void compareTo()
    {
        assertEquals(
          one.toString().compareTo(two.toString()),
          targetOne.compareTo(targetTwo)
        );
        assertEquals(
          two.toString().compareTo(one.toString()),
          targetTwo.compareTo(targetOne)
        );
    }

    @Test
    public void testEquals()
    {
        assertEquals(one, one);
        assertEquals(two, two);
        assertNotEquals(one, two);
        assertNotEquals(two, one);
    }

    @Test
    public void testHashCode()
    {
        assertEquals(one.toString().hashCode(), targetOne.hashCode());
        assertEquals(two.toString().hashCode(), targetTwo.hashCode());
    }
}