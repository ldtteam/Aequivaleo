package com.ldtteam.aequivaleo.api.compound.container.dummy;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.ldtteam.aequivaleo.api.compound.container.factory.ICompoundContainerType;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data.IRecipeIngredientType;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.recipe.equivalency.ingredient.SimpleIngredient;
import com.ldtteam.aequivaleo.recipe.equivalency.ingredient.TagIngredient;
import com.ldtteam.aequivaleo.testing.compound.container.testing.StringCompoundContainer;
import net.minecraft.core.Registry;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
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
        
        List<ICompoundContainerType<?>> containerFactories = ImmutableList.of(new StringCompoundContainer.Type());
        ModRegistries.CONTAINER_FACTORY = (Registry<ICompoundContainerType<?>>) mock(Registry.class);
        when(ModRegistries.CONTAINER_FACTORY.iterator()).thenReturn(containerFactories.iterator());
        when(ModRegistries.CONTAINER_FACTORY.byNameCodec()).thenCallRealMethod();
        CompoundContainerFactoryManager.getInstance().bake();
        
        List<IRecipeIngredientType> ingredientTypes = ImmutableList.of(new SimpleIngredient.Type(), new TagIngredient.Type());
        ModRegistries.RECIPE_INGREDIENT_TYPE = (Registry<IRecipeIngredientType>) mock(Registry.class);
        when(ModRegistries.RECIPE_INGREDIENT_TYPE.iterator()).thenReturn(ingredientTypes.iterator());
        when(ModRegistries.RECIPE_INGREDIENT_TYPE.byNameCodec()).thenCallRealMethod();
    }

    @Test
    public void getContents()
    {
        assertEquals(targetOne, targetOne.contents());
        assertEquals(targetTwo, targetTwo.contents());
    }

    @Test
    public void getContentsCount()
    {
        assertEquals(0D, targetOne.contentsCount(), 0d);
        assertEquals(0D, targetTwo.contentsCount(), 0d);
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
        assertEquals(one, targetOne.originalData());
        assertEquals(two, targetTwo.originalData());
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