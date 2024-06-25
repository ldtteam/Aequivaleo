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
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public record RecipeVariants(Collection<Ingredient> input, ICompoundContainer<?> output) {

    public RecipeVariants(Recipe<?> recipe, RegistryAccess access) {
        this(Arrays.asList(recipe.getIngredients().toArray(new Ingredient[0])), ICompoundContainer.from(recipe.getResultItem(access), recipe.getResultItem(access).getCount()));
    }

    public record CountedIngredient(Ingredient representative, int count) {
        CountedIngredient(Collection<Ingredient> ingredients) {
            this(ingredients.stream().findFirst().orElseThrow(), ingredients.size());
        }
    }

    public record InputWithRemainder(IRecipeIngredient input, IRecipeIngredient remainder) {
    }

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
     * Construct an {@code IllegalStateException} with appropriate message.
     *
     * @param k the duplicate key
     * @param u 1st value to be accumulated/merged
     * @param v 2nd value to be accumulated/merged
     */
    private static IllegalStateException duplicateKeyException(
            Object k, Object u, Object v) {
        return new IllegalStateException(String.format(
                "Duplicate key %s (attempted merging values %s and %s)",
                k, u, v));
    }

    /**
     * {@code BinaryOperator<Map>} that merges the contents of its right
     * argument into its left argument, throwing {@code IllegalStateException}
     * if duplicate keys are encountered.
     *
     * @param <K> type of the map keys
     * @param <V> type of the map values
     * @param <M> type of the map
     * @return a merge function for two maps
     */
    private static <K, V, M extends Map<K, V>>
    BinaryOperator<M> uniqKeysMapMerger() {
        return (m1, m2) -> {
            for (Map.Entry<K, V> e : m2.entrySet()) {
                K k = e.getKey();
                V v = Objects.requireNonNull(e.getValue());
                V u = m1.putIfAbsent(k, v);
                if (u != null) throw duplicateKeyException(k, u, v);
            }
            return m1;
        };
    }

    /**
     * {@code BiConsumer<Map, T>} that accumulates (key, value) pairs
     * extracted from elements into the map, throwing {@code IllegalStateException}
     * if duplicate keys are encountered.
     *
     * @param keyMapper   a function that maps an element into a key
     * @param valueMapper a function that maps an element into a value
     * @param <T>         type of elements
     * @param <K>         type of map keys
     * @param <V>         type of map values
     * @return an accumulating consumer
     */
    private static <T, K, V>
    BiConsumer<Map<K, V>, T> uniqKeysMapAccumulator(Function<? super T, ? extends K> keyMapper,
                                                    Function<? super T, ? extends V> valueMapper) {
        return (map, element) -> {
            K k = keyMapper.apply(element);
            V v = Objects.requireNonNull(valueMapper.apply(element));
            V u = map.putIfAbsent(k, v);
            if (u != null) throw duplicateKeyException(k, u, v);
        };
    }


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
