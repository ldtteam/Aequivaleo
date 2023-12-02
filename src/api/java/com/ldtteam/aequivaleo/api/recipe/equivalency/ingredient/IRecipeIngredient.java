package com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.data.IRecipeIngredientType;
import com.ldtteam.aequivaleo.api.util.ModRegistries;
import com.ldtteam.aequivaleo.api.util.ModRegistryKeys;
import com.ldtteam.aequivaleo.api.util.SortedSetComparator;
import com.mojang.serialization.Codec;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

/**
 * Represents an ingredient to a recipe.
 * This basically defines all variants that are allows to go into one slot of a recipe.
 */
public interface IRecipeIngredient extends Comparable<IRecipeIngredient>
{

    Codec<IRecipeIngredient> CODEC = ModRegistries.RECIPE_INGREDIENT_TYPE
                                             .byNameCodec()
                                                .dispatch(
                                                        IRecipeIngredient::getType,
                                                        IRecipeIngredientType::codec
                                                );
    
    /**
     * Creates a new ingredient from a single source object.
     * This object needs to have an innate count that its container factory is aware of.
     *
     * @param source The source to create a recipe ingredient of.
     * @return The ingredient which represents the given source object.
     */
    static IRecipeIngredient from(Object source) {
        return IDefaultRecipeIngredients.getInstance().from(source);
    }

    /**
     * Creates a new ingredient from a single source container.
     *
     * @param source The source to create a recipe ingredient of.
     * @return The ingredient which represents the given source object.
     */
    static IRecipeIngredient from(ICompoundContainer<?> source) {
        return IDefaultRecipeIngredients.getInstance().from(source);
    }

    /**
     * Creates a new ingredient from a single source object.
     *
     * @param source The source to create a recipe ingredient of.
     * @param count The number of source instances that this ingredient represents.
     * @return The ingredient which represents the given source object.
     */
    static IRecipeIngredient from(Object source, int count) {
        return IDefaultRecipeIngredients.getInstance().from(source, count);
    }

    /**
     * Creates a new ingredient from a single source object.
     *
     * @param source The source to create a recipe ingredient of.
     * @param count The number of source instances that this ingredient represents.
     * @return The ingredient which represents the given source object.
     */
    static IRecipeIngredient from(Object source, double count) {
        return IDefaultRecipeIngredients.getInstance().from(source, count);
    }
    
    /**
     * Creates a new ingredient from the given tag.
     *
     * @param key The key of the tag to create an ingredient of.
     * @return The ingredient which represents the given tag.
     */
    static IRecipeIngredient tagged(TagKey<?> key) {
        return IDefaultRecipeIngredients.getInstance().tagged(key);
    }
    
    /**
     * Creates a new ingredient from the given tag with the given count.
     *
     * @param key The key of the tag to create an ingredient of.
     * @param count The number of source instances that this ingredient represents.
     * @return The ingredient which represents the given tag.
     */
    static IRecipeIngredient tagged(TagKey<?> key, int count) {
        return IDefaultRecipeIngredients.getInstance().tagged(key, count);
    }

    /**
     * Indicates if this ingredient is valid.
     *
     * @return {@code True} when valid.
     */
    default boolean isValid() {
        return !getCandidates().isEmpty() && getCandidates().stream().allMatch(ICompoundContainer::isValid);
    }

    /**
     * The candidates who match this ingredient.
     *
     * @return The candidates.
     */
    SortedSet<ICompoundContainer<?>> getCandidates();

    /**
     * The required count of this ingredient.
     *
     * @return The required count.
     */
    Double getRequiredCount();

    /**
     * The type of this ingredient.
     *
     * @return The type.
     */
    IRecipeIngredientType getType();

    @Override
    default int compareTo(@NotNull final IRecipeIngredient iRecipeIngredient)
    {
        final int candidateComparison = SortedSetComparator.<ICompoundContainer<?>>getInstance().compare(getCandidates(), iRecipeIngredient.getCandidates());
        if (candidateComparison != 0)
            return candidateComparison;

        return (int) (getRequiredCount() - iRecipeIngredient.getRequiredCount());
    }
}
