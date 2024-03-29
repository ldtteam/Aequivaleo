package com.ldtteam.aequivaleo.api.recipe.equivalency.calculator;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.util.TriFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.NonNullList;

import java.util.List;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Represents a calculator that can help with creating recipes for aequivaleo.
 */
public interface IRecipeCalculator
{

    static IRecipeCalculator getInstance() {
        return IAequivaleoAPI.getInstance().getRecipeCalculator();
    }

    /**
     * Method used to calculate all variants of a recipe, taking the container items into account.
     * This method uses the vanilla {@link Ingredient#getItems()} ()} method to get the stacks which are valie
     * for a given ingredient.
     *
     * If your particular recipe requires aequivaleo to use a different way to convert an ingredient
     * to a list of valid itemstacks then you use {@link #getAllVariants(ServerLevel, Recipe, Function, TriFunction)}
     * and supply it your own handler.
     *
     * @param world The world the recipe is in.
     * @param recipe The recipe to calculate the variants for.
     * @param recipeFactory The factory that is used to convert the ingredients, required known outputs, and outputs into a recipe.
     * @return A stream of recipes which represent all collapsed variants.
     */
    default Stream<IEquivalencyRecipe> getAllVariants(
      ServerLevel world,
      Recipe<?> recipe,
      TriFunction<SortedSet<IRecipeIngredient>, SortedSet<ICompoundContainer<?>>, SortedSet<ICompoundContainer<?>>, IEquivalencyRecipe> recipeFactory
    ) {
        return this.getAllVariants(
          world,
          recipe,
          this::getAllVariantsFromSimpleIngredient,
          recipeFactory
        );
    };

    /**
     * Method used to calculate all variants of a recipe, taking the container items into account.
     * This method uses {@link Recipe#getIngredients()} to extract the ingredients from a given recipe as input.
     * This method allows you to pass in your own handler which converts a given itemstack into a list
     * of {@link IRecipeIngredient}.
     *
     * @param world The world the recipe is in.
     * @param recipe The recipe to calculate the variants for.
     * @param ingredientHandler The ingredient handler to convert the ingredient into recipe ingredients.
     * @param recipeFactory The factory that is used to convert the ingredients, required known outputs, and outputs into a recipe.
     * @return A stream of recipes which represent all collapsed variants.
     */
    default Stream<IEquivalencyRecipe> getAllVariants(
      ServerLevel world,
      Recipe<?> recipe,
      Function<Ingredient, List<IRecipeIngredient>> ingredientHandler,
      TriFunction<SortedSet<IRecipeIngredient>, SortedSet<ICompoundContainer<?>>, SortedSet<ICompoundContainer<?>>, IEquivalencyRecipe> recipeFactory
    ) {
        return this.getAllVariants(
          world,
          recipe,
          Recipe::getIngredients,
          this::getAllVariantsFromSimpleIngredient,
          recipeFactory
        );
    };


    /**
     * Method used to calculate all variants of a recipe, taking the container items into account.
     * This method allows you to pass in your own extractor to be used to get the input ingredients from the recipe.
     * This method allows you to pass in your own handler which converts a given itemstack into a list
     * of {@link IRecipeIngredient}.
     *
     * @param world The world the recipe is in.
     * @param recipe The recipe to calculate the variants for.
     * @param ingredientExtractor The ingredient extractor.
     * @param ingredientHandler The ingredient handler to convert the ingredient into recipe ingredients.
     * @param recipeFactory The factory that is used to convert the ingredients, required known outputs, and outputs into a recipe.
     * @return A stream of recipes which represent all collapsed variants.
     */
    Stream<IEquivalencyRecipe> getAllVariants(
      ServerLevel world,
      Recipe<?> recipe,
      Function<Recipe<?>, NonNullList<Ingredient>> ingredientExtractor,
      Function<Ingredient, List<IRecipeIngredient>> ingredientHandler,
      TriFunction<SortedSet<IRecipeIngredient>, SortedSet<ICompoundContainer<?>>, SortedSet<ICompoundContainer<?>>, IEquivalencyRecipe> recipeFactory
    );

    /**
     * This method is for your convenience:
     * It converts the vanilla {@link Ingredient#getItems()} to a list of {@link IRecipeIngredient}
     * respecting the relevant {@link ItemStack#getCraftingRemainingItem()} ()} stacks.
     *
     * @param ingredient The ingredient to handle.
     * @return All the recipe ingredients which it represents, respecting the container item.
     */
    List<IRecipeIngredient> getAllVariantsFromSimpleIngredient(Ingredient ingredient);

    /**
     * Calculates all perturbations of a given input.
     * A perturbation is a list of all possible combinations of the input.
     * For example:
     * Input: [1, 2, 3]
     * Output: [[1, 2, 3], [1, 3, 2], [2, 1, 3], [2, 3, 1], [3, 2, 1], [3, 1, 2]]
     *
     * @param input The input to calculate the perturbations for.
     * @param maxSize The maximum size of the perturbations.
     * @return A list of all perturbations.
     * @param <T> The type of the input.
     */
    <T> List<List<T>> getAllPerturbations(List<T> input, int maxSize);
}
