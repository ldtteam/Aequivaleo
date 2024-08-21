package com.ldtteam.aequivaleo.api.recipe.equivalency.calculator;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import com.ldtteam.aequivaleo.api.util.ItemStackUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents all variants of a recipe, based on its ingredients and output.
 * <p>
 *     This class is used to calculate all possible variants of a recipe, based on the ingredients and output.
 *     It will take into account different remainders of an ingredient and split the recipe into multiple variants.
 * </p>
 *
 * @param input The input ingredients.
 * @param output The output of the recipe.
 */
public record RecipeVariants(Collection<Ingredient> input, ICompoundContainer<?> output) {

    /**
     * Creates a new recipe variants collection.
     *
     * @param recipe The recipe to create the variants for.
     * @param access The registry access.
     */
    public RecipeVariants(Recipe<?> recipe, RegistryAccess access) {
        this(Arrays.asList(recipe.getIngredients().toArray(new Ingredient[0])), ICompoundContainer.from(recipe.getResultItem(access), recipe.getResultItem(access).getCount()));
    }

    /**
     * Represents a counted ingredient.
     * <p>
     *     This class is used to represent an ingredient and the amount of times it is used in a recipe.
     * </p>
     *
     * @param representative The representative ingredient.
     * @param count The amount of times the ingredient is used.
     */
    public record CountedIngredient(Ingredient representative, int count) {
        CountedIngredient(Collection<Ingredient> ingredients) {
            this(ingredients.stream().findFirst().orElseThrow(), ingredients.size());
        }
    }

    /**
     * Represents an input with a remainder.
     * <p>
     *     This class is used to represent an input ingredient and the remainder of the ingredient.
     *     A remainder is the element that is left after the ingredient is used in a recipe.
     *     For example, a bucket is the remainder of a water bucket.
     * </p>
     *
     * @param input The input ingredient.
     * @param remainder The remainder of the ingredient.
     */
    public record InputWithRemainder(IRecipeIngredient input, IRecipeIngredient remainder) {
    }

    /**
     * Get all variants of the recipe.
     *
     * @return The recipe variants.
     */
    public Collection<RecipeVariant> variants() {
        final Collection<Collection<Ingredient>> groupedIngredients = GroupingUtils.groupByUsingList(input.stream().filter(ingredient -> ingredient.getItems().length > 0).toList(), IngredientEquivalency::new);
        final Collection<Collection<InputWithRemainder>> explodedIngredients = groupedIngredients.stream()
                .map(CountedIngredient::new)
                .map(countedIngredient -> explodeIngredient(countedIngredient.representative(), countedIngredient.count()))
                .collect(Collectors.toSet());
        final Collection<Collection<InputWithRemainder>> explodedVariants = explodeVariants(explodedIngredients);
        return explodedVariants.stream()
                .map(variantInputs -> {
                    final Collection<IRecipeIngredient> ingredients = variantInputs.stream()
                            .map(InputWithRemainder::input)
                            .filter(IRecipeIngredient::isValid)
                            .collect(Collectors.toSet());
                    final Collection<IRecipeIngredient> remainders = variantInputs.stream()
                            .map(InputWithRemainder::remainder)
                            .filter(IRecipeIngredient::isValid)
                            .collect(Collectors.toSet());
                    final ICompoundContainer<?> output = this.output;
                    return new RecipeVariant(ingredients, remainders, output);
                })
                .collect(Collectors.toSet());
    }

    /**
     * Explode an ingredient into its components.
     * <p>
     *     This method will explode an ingredient into its components.
     *     If an ingredient has different inputs, then multiple ingredients will be returned if a different remainder is discovered.
     * </p>
     *
     * @param ingredient The ingredient to explode.
     * @param count The amount of times the ingredient is used.
     * @return The exploded ingredient.
     */
    public Set<InputWithRemainder> explodeIngredient(Ingredient ingredient, int count) {
        final Collection<Collection<ItemStack>> ingredientItemsByRemainder = GroupingUtils.groupByUsingSet(Arrays.asList(ingredient.getItems()), RemainderEquivalency::new);

        return ingredientItemsByRemainder.stream()
                .map(stacks -> {
                    final Collection<ICompoundContainer<?>> containers = stacks.stream()
                            .map(stack -> ICompoundContainer.from(stack, 1))
                            .collect(Collectors.toSet());
                    final IRecipeIngredient input = new SimpleIngredientBuilder().from(new TreeSet<>(containers)).withCount(count).createIngredient();
                    final ItemStack remainder = stacks.stream().findFirst().orElseThrow().getCraftingRemainingItem();
                    final IRecipeIngredient remainderIngredient = new SimpleIngredientBuilder().from(ICompoundContainer.from(remainder, 1)).withCount(count).createIngredient();
                    return new InputWithRemainder(input, remainderIngredient);
                })
                .collect(Collectors.toSet());

    }

    /**
     * Explode a collection of ingredients into all possible variants.
     * <p>
     *     This method will explode a collection of ingredients into all possible variants.
     *     It will take into account that multiple ingredients can be used in a recipe.
     * </p>
     *
     * @param ingredients The ingredients to explode.
     * @return The exploded variants.
     */
    public Collection<Collection<InputWithRemainder>> explodeVariants(Collection<Collection<InputWithRemainder>> ingredients) {
        if (ingredients.isEmpty()) {
            return new HashSet<>();
        }

        if (ingredients.size() == 1) {
            return ingredients;
        }

        final Collection<InputWithRemainder> firstIngredient = ingredients.stream().findFirst().orElseThrow();
        final Collection<Collection<InputWithRemainder>> restIngredients = ingredients.stream().skip(1).collect(Collectors.toSet());
        return explodeVariants(firstIngredient, restIngredients);
    }

    /**
     * Explode a collection of ingredients into all possible variants.
     * <p>
     *     This method will explode a collection of ingredients into all possible variants.
     *     It will take into account that multiple ingredients can be used in a recipe.
     * </p>
     *
     * @param me The ingredients to explode.
     * @param remainder The remainder of the ingredients.
     * @return The exploded variants.
     */
    public Collection<Collection<InputWithRemainder>> explodeVariants(Collection<InputWithRemainder> me, Collection<Collection<InputWithRemainder>> remainder) {
        if (remainder.isEmpty()) {
            final Collection<Collection<InputWithRemainder>> exploded = new ArrayList<>();
            exploded.add(new ArrayList<>(me));
            return exploded;
        }

        final Collection<InputWithRemainder> firstRemainder = remainder.stream().findFirst().orElseThrow();
        final Collection<Collection<InputWithRemainder>> restRemainder = remainder.stream().skip(1).collect(Collectors.toSet());
        final Collection<Collection<InputWithRemainder>> remainingExploded = explodeVariants(firstRemainder, restRemainder);

        final Collection<Collection<InputWithRemainder>> exploded = new ArrayList<>();
        for (InputWithRemainder ingredient : me) {
            for (Collection<InputWithRemainder> remaining : remainingExploded) {
                final Collection<InputWithRemainder> explodedVariant = new ArrayList<>(remaining);
                explodedVariant.add(ingredient);
                exploded.add(explodedVariant);
            }
        }
        return exploded;
    }

    /**
     * Wrapper class that helps to accumulate ingredients into maps and properly compare them to each other.
     */
    @SuppressWarnings("deprecation")
    private final static class IngredientEquivalency {

        private final ItemStack[] items;

        private IngredientEquivalency(Ingredient ingredient) {
            items = ingredient.getItems();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IngredientEquivalency that = (IngredientEquivalency) o;

            if (items.length != that.items.length) return false;

            for (ItemStack item : items) {
                for (ItemStack itemStack : that.items) {
                    if (!ItemStackUtils.compareItemStacksIgnoreStackSize(item, itemStack)) {
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(Arrays.stream(items)
                    .map(ItemStack::getItem)
                    .map(BuiltInRegistries.ITEM::getKey)
                    .map(Objects::toString)
                    .sorted()
                    .toArray());
        }
    }

    /**
     * Wrapper class that helps to accumulate remainders into maps and properly compare them to each other.
     */
    @SuppressWarnings("deprecation")
    private final static class RemainderEquivalency {

        private final ItemStack items;

        private RemainderEquivalency(ItemStack ingredient) {
            items = ingredient;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RemainderEquivalency that = (RemainderEquivalency) o;

            final ItemStack thisContainer = this.items.getCraftingRemainingItem();
            final ItemStack thatContainer = that.items.getCraftingRemainingItem();

            return ItemStackUtils.compareItemStacksIgnoreStackSize(thisContainer, thatContainer);
        }

        @Override
        public int hashCode() {
            return BuiltInRegistries.ITEM.getKey(items.getCraftingRemainingItem().getItem()).hashCode();
        }
    }
}
