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
 * An iterator which calculates all possible variants of a recipe.
 * Note this iterator is not thread safe.
 * Note this iterator is not fail-fast.
 * Note this iterator will return a mutable object that gets mutated on each iteration, to prevent allocations.
 */
public class RecipeVariantIterator implements Iterator<RecipeVariant> {

    private final int totalVariants;
    private final InputWithRemainder[][] input;
    private final ICompoundContainer<?> output;
    private final RecipeVariant current;

    private int currentVariant = 0;

    /**
     * Creates a new recipe variants collection.
     *
     * @param recipe The recipe to create the variants for.
     * @param access The registry access.
     */
    public RecipeVariantIterator(Recipe<?> recipe, RegistryAccess access) {
        this(Arrays.asList(recipe.getIngredients().toArray(new Ingredient[0])), ICompoundContainer.from(recipe.getResultItem(access), recipe.getResultItem(access).getCount()));
    }

    /**
     * Creates a new recipe variants collection.
     *
     * @param inputs The inputs of the recipe.
     * @param output The output of the recipe.
     */
    public RecipeVariantIterator(final Collection<Ingredient> inputs, final ICompoundContainer<?> output) {
        this.input = calculateInputs(inputs);
        this.output = output;

        this.current = new RecipeVariant(output);
        this.totalVariants = calculateTotalVariants(input);
    }

    private static InputWithRemainder[][] calculateInputs(final Collection<Ingredient> input) {
        final Collection<Collection<Ingredient>> groupedIngredients = GroupingUtils.groupByUsingList(input.stream().filter(ingredient -> ingredient.getItems().length > 0).toList(), IngredientEquivalency::new);

        List<InputWithRemainder[]> list = new ArrayList<>();
        for (Collection<Ingredient> groupedIngredient : groupedIngredients) {
            CountedIngredient countedIngredient = new CountedIngredient(groupedIngredient);
            InputWithRemainder[] inputWithRemainders = explodeIngredient(countedIngredient.representative(), countedIngredient.count());
            if (inputWithRemainders.length != 0) {
                list.add(inputWithRemainders);
            }
        }
        return list.toArray(new InputWithRemainder[0][]);
    }

    private static int calculateTotalVariants(final InputWithRemainder[][] input) {
        if (input.length == 0)
            return 0;

        return Arrays.stream(input)
                .mapToInt(variants -> variants.length)
                .reduce(1, (a, b) -> a * b);
    }

    @Override
    public boolean hasNext() {
        return currentVariant < totalVariants;
    }

    @Override
    public RecipeVariant next() {
        if (!hasNext())
            throw new NoSuchElementException();

        updateCurrent();

        return current;
    }

    private void updateCurrent() {
        final int[] indices = calculateIndices(currentVariant, input);
        final Collection<IRecipeIngredient> ingredients = new ArrayList<>();
        final Collection<IRecipeIngredient> remainders = new ArrayList<>();

        for (int i = 0; i < indices.length; i++) {
            final InputWithRemainder inputWithRemainder = input[i][indices[i]];
            ingredients.add(inputWithRemainder.input());
            if (inputWithRemainder.remainder().isValid())
                remainders.add(inputWithRemainder.remainder());
        }

        final Collection<Collection<IRecipeIngredient>> groupedIngredients = GroupingUtils.groupByUsingSet(ingredients, IRecipeIngredient::getCandidates);
        final Collection<Collection<IRecipeIngredient>> groupedRemainders = GroupingUtils.groupByUsingSet(remainders, IRecipeIngredient::getCandidates);

        final Collection<IRecipeIngredient> combinedIngredients = groupedIngredients.stream()
                .map(ingredientsGroup -> new SimpleIngredientBuilder().from(ingredientsGroup.iterator().next().getCandidates()).withCount(
                        ingredientsGroup.stream().mapToDouble(IRecipeIngredient::getRequiredCount).sum()
                ).createIngredient())
                .collect(Collectors.toSet());
        final Collection<IRecipeIngredient> combinedRemainders = groupedRemainders.stream()
                .map(remaindersGroup -> new SimpleIngredientBuilder().from(remaindersGroup.iterator().next().getCandidates()).withCount(
                        remaindersGroup.stream().mapToDouble(IRecipeIngredient::getRequiredCount).sum()
                ).createIngredient())
                .collect(Collectors.toSet());

        current.setIngredients(combinedIngredients);
        current.setRemainders(combinedRemainders);

        currentVariant += 1;
    }

    private int[] calculateIndices(int currentVariant, InputWithRemainder[][] input) {
        final int[] indices = new int[input.length];
        int remaining = currentVariant;

        for (int i = 0; i < input.length; i++) {
            final int variantCount = input[i].length;
            final int index = remaining % variantCount;
            remaining /= variantCount;
            indices[i] = index;
        }

        return indices;
    }


    private static InputWithRemainder[] explodeIngredient(Ingredient ingredient, int count) {
        final Collection<Collection<ItemStack>> ingredientItemsByRemainder = GroupingUtils.groupByUsingSet(Arrays.asList(ingredient.getItems()), RemainderEquivalency::new);

        return ingredientItemsByRemainder.stream()
                .filter(itemStacks -> !itemStacks.isEmpty())
                .map(stacks -> {
                    final Collection<ICompoundContainer<?>> containers = stacks.stream()
                            .map(stack -> ICompoundContainer.from(stack, 1))
                            .collect(Collectors.toSet());
                    final IRecipeIngredient input = new SimpleIngredientBuilder().from(new TreeSet<>(containers)).withCount(count).createIngredient();
                    final ItemStack remainder = stacks.stream().findFirst().orElseThrow().getCraftingRemainingItem();
                    final IRecipeIngredient remainderIngredient = new SimpleIngredientBuilder().from(ICompoundContainer.from(remainder, 1)).withCount(count).createIngredient();
                    return new InputWithRemainder(input, remainderIngredient);
                })
                .toArray(InputWithRemainder[]::new);

    }

    private record InputWithRemainder(IRecipeIngredient input, IRecipeIngredient remainder) {
    }

    private record CountedIngredient(Ingredient representative, int count) {
        CountedIngredient(Collection<Ingredient> ingredients) {
            this(ingredients.stream().findFirst().orElseThrow(), ingredients.size());
        }
    }

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
                if (!contained(item, that.items)) {
                    return false;
                }
            }

            return true;
        }

        private static boolean contained(ItemStack left, ItemStack[] right) {
            for (ItemStack itemStack : right) {
                if (ItemStackUtils.compareItemStacksIgnoreStackSize(left, itemStack)) {
                    return true;
                }
            }
            return false;
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
