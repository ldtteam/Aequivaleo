package com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import net.minecraft.tags.TagKey;

import java.util.function.Consumer;

/**
 * Defines the default recipe ingredients that aequivaleo provides.
 * This is a convenience class to make it easier to create recipe ingredients.
 * <br/>
 * You can register your own types if need be.
 */
public interface IDefaultRecipeIngredients {
    
    /**
     * Gets the instance of the default recipe ingredients.
     *
     * @return The instance of the default recipe ingredients.
     */
    public static IDefaultRecipeIngredients getInstance() {
        return IAequivaleoAPI.getInstance().getDefaultRecipeIngredients();
    }
    
    /**
     * Creates a new ingredient from a single source container.
     *
     * @param source The source to create a recipe ingredient of.
     * @return The ingredient which represents the given source object.
     */
    IRecipeIngredient from(ICompoundContainer<?> source);
    
    /**
     * Creates a new ingredient from a single source object.
     * The passed in source object needs to have an innate count that its container factory is aware of,
     * if this is not the case then the count will be 1.
     *
     * @param source The source to create a recipe ingredient of.
     * @return The ingredient which represents the given source object.
     */
    default IRecipeIngredient from(Object source) {
        return from(source, ICompoundContainerFactoryManager.getInstance().getInnateCount(source).orElse(1d));
    }
    
    /**
     * Creates a new ingredient from a single source object.
     *
     * @param source The source to create a recipe ingredient of.
     * @param count The number of source instances that this ingredient represents.
     * @return The ingredient which represents the given source object.
     */
     default IRecipeIngredient from(Object source, int count) {
        return from(source, (double) count);
     }
    
    /**
     * Creates a new ingredient from a single source object.
     *
     * @param source The source to create a recipe ingredient of.
     * @param count The number of source instances that this ingredient represents.
     * @return The ingredient which represents the given source object.
     */
    IRecipeIngredient from(Object source, double count);
    
    /**
     * Creates a new ingredient from the given tag.
     *
     * @param key The key of the tag to create an ingredient of.
     * @return The ingredient which represents the given tag.
     */
    default IRecipeIngredient tagged(TagKey<?> key) {
        return tagged(key, 1);
    }
    
    /**
     * Creates a new ingredient from the given tag with the given count.
     *
     * @param key The key of the tag to create an ingredient of.
     * @param count The number of source instances that this ingredient represents.
     * @return The ingredient which represents the given tag.
     */
    default IRecipeIngredient tagged(TagKey<?> key, int count) {
        return tagged(key, (double) count);
    }
    
    /**
     * Creates a new ingredient from the given tag with the given count.
     *
     * @param key The key of the tag to create an ingredient of.
     * @param count The number of source instances that this ingredient represents.
     * @return The ingredient which represents the given tag.
     */
    IRecipeIngredient tagged(TagKey<?> key, double count);
    
    /**
     * Creates a new ingredient by configuring a simple ingredient builder.
     *
     * @param ingredientBuilderConsumer The consumer to configure the builder.
     * @return The ingredient which represents the given tag.
     */
    IRecipeIngredient from(Consumer<ISimpleRecipeIngredientBuilder> ingredientBuilderConsumer);
}
