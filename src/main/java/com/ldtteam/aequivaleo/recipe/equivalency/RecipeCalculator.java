package com.ldtteam.aequivaleo.recipe.equivalency;

import com.google.common.collect.Maps;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.calculator.IRecipeCalculator;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import com.ldtteam.aequivaleo.api.util.TriFunction;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.utils.Permutations;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeCalculator implements IRecipeCalculator {

    private static final RecipeCalculator INSTANCE = new RecipeCalculator();

    public static RecipeCalculator getInstance() {
        return INSTANCE;
    }

    private RecipeCalculator() {
    }

    @Override
    public Stream<IEquivalencyRecipe> getAllVariants(
            @NotNull final ServerLevel serverLevel,
            final Recipe<?> recipe,
            final Function<Recipe<?>, NonNullList<Ingredient>> ingredientExtractor,
            final Function<Ingredient, List<IRecipeIngredient>> ingredientHandler,
            final TriFunction<SortedSet<IRecipeIngredient>, SortedSet<ICompoundContainer<?>>, SortedSet<ICompoundContainer<?>>, IEquivalencyRecipe> recipeFactory
    ) {
        if (recipe.isSpecial()) {
            return Stream.empty();
        }

        if (recipe.getResultItem(serverLevel.registryAccess()).isEmpty()) {
            return Stream.empty();
        }

        final ICompoundContainer<?> result = CompoundContainerFactoryManager.getInstance().wrapInContainer(recipe.getResultItem(serverLevel.registryAccess()), recipe.getResultItem(serverLevel.registryAccess()).getCount());
        final SortedSet<ICompoundContainer<?>> resultSet = new TreeSet<>();
        resultSet.add(result);

        final List<SortedSet<IRecipeIngredient>> variants = getAllInputVariants(ingredientExtractor.apply(recipe)
                        .stream()
                        .filter(i -> i.getItems().length > 0)
                        .collect(Collectors.toList()),
                true, ingredientHandler);

        return variants
                .stream()
                .map(iRecipeIngredients -> {

                    final SortedSet<ICompoundContainer<?>> containers =
                            mapStacks(
                                    iRecipeIngredients.stream()
                                            .map(iRecipeIngredient -> iRecipeIngredient.getCandidates()
                                                    .stream()
                                                    .map(container -> Pair.of(container.getContents(), iRecipeIngredient.getRequiredCount().intValue() * container.getContentsCount().intValue()))
                                                    .filter(integerPair -> integerPair.getKey() instanceof ItemStack)
                                                    .map(integerPair -> Pair.of((ItemStack) integerPair.getKey(), integerPair.getValue()))
                                                    .filter(itemStackIntegerPair -> !itemStackIntegerPair.getKey().isEmpty())
                                                    .filter(itemStackIntegerPair -> itemStackIntegerPair.getKey().hasCraftingRemainingItem())
                                                    .map(itemStackIntegerPair -> {
                                                        final ItemStack containerStack = itemStackIntegerPair.getKey().getCraftingRemainingItem();
                                                        containerStack.setCount(itemStackIntegerPair.getValue());
                                                        return containerStack;
                                                    })
                                                    .filter(stack -> !stack.isEmpty())
                                                    .collect(Collectors.toList())
                                            )
                                            .filter(stacks -> !stacks.isEmpty())
                                            .findAny()
                                            .orElse(Collections.emptyList())
                            );

                    return recipeFactory.apply(iRecipeIngredients, containers, resultSet);
                });
    }

    private List<SortedSet<IRecipeIngredient>> getAllInputVariants(
            final List<Ingredient> mcIngredients,
            final boolean checkSimple,
            final Function<Ingredient, List<IRecipeIngredient>> ingredientHandler) {
        if (mcIngredients.isEmpty() || (checkSimple && !Aequivaleo.getInstance().getConfiguration().getServer().allowNoneSimpleIngredients.get() && mcIngredients.stream().anyMatch(ingredient -> !ingredient.isSimple()))) {
            return Collections.emptyList();
        }

        if (mcIngredients.size() == 1) {
            return IngredientHandler.getInstance().attemptIngredientConversion(
                            ingredientHandler,
                            mcIngredients.get(0)
                    )
                    .stream()
                    .map(iRecipeIngredient -> {
                        final SortedSet<IRecipeIngredient> ingredients = new TreeSet<>();
                        ingredients.add(iRecipeIngredient);
                        return ingredients;
                    })
                    .collect(Collectors.toList());
        }

        final Ingredient target = mcIngredients.get(0);
        final List<Ingredient> ingredients = new ArrayList<>(mcIngredients);
        ingredients.remove(0);

        final List<IRecipeIngredient> targetIngredients = IngredientHandler.getInstance().attemptIngredientConversion(
                ingredientHandler,
                target
        );
        final Set<IRecipeIngredient> targets = new TreeSet<>(targetIngredients);

        final List<SortedSet<IRecipeIngredient>> subVariants = getAllInputVariants(ingredients, false, ingredientHandler);

        if (targets.isEmpty())
            return subVariants;

        return subVariants
                .stream()
                .flatMap(subRecpIng -> targets.stream()
                        .map(nextIng -> {
                            final SortedSet<IRecipeIngredient> newIngs = new TreeSet<>(subRecpIng);

                            Optional<IRecipeIngredient> exIng;
                            if ((exIng = newIngs.stream().filter(ing -> ing.getCandidates().equals(nextIng.getCandidates())).findFirst()).isPresent()) {
                                newIngs.remove(exIng.get());
                                newIngs.add(new SimpleIngredientBuilder()
                                        .from(exIng.get()).withCount(exIng.get().getRequiredCount() + nextIng.getRequiredCount())
                                        .createIngredient()
                                );
                            } else {
                                newIngs.add(nextIng);
                            }

                            return newIngs;
                        }))
                .collect(Collectors.toList());
    }

    @Override
    public List<IRecipeIngredient> getAllVariantsFromSimpleIngredient(final Ingredient ingredient) {
        final List<ItemStack> stacks = Arrays.asList(ingredient.getItems());
        final Collection<Collection<ItemStack>> groupedByContainer =
                GroupingUtils.groupByUsingSet(stacks, stack -> new ItemStackEqualityWrapper(stack.hasCraftingRemainingItem() ? stack.getCraftingRemainingItem() : ItemStack.EMPTY));

        return groupedByContainer
                .stream()
                .map(this::mapStacks)
                .map(wrappedStacks -> new SimpleIngredientBuilder().from(wrappedStacks).withCount(1d).createIngredient())
                .collect(Collectors.toList());
    }

    @Override
    public <T> List<List<T>> getAllPerturbations(List<T> input, int maxPermutations) {
        final Permutations<T> permutations = new Permutations<>();

        return permutations.getPermutations(input, maxPermutations);
    }

    private SortedSet<ICompoundContainer<?>> mapStacks(final Collection<ItemStack> stacks) {
        final List<ICompoundContainer<?>> wrappedStacks =
                stacks.stream().map(stack -> CompoundContainerFactoryManager.getInstance().wrapInContainer(stack, stack.getCount())).collect(Collectors.toList());

        final Collection<Collection<ICompoundContainer<?>>> groupedStacks = GroupingUtils.groupByUsingSet(
                wrappedStacks,
                s -> CompoundContainerFactoryManager.getInstance().wrapInContainer(s.getContents(), 1)
        );

        return groupedStacks
                .stream()
                .map(c -> CompoundContainerFactoryManager.getInstance().wrapInContainer(c.iterator().next().getContents(), c.stream().mapToDouble(ICompoundContainer::getContentsCount).sum()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private record ItemStackEqualityWrapper(ItemStack stack) {

        @Override
            public int hashCode() {
                return Objects.hash(
                        ForgeRegistries.ITEMS.getKey(stack.getItem()),
                        stack.getDamageValue(),
                        stack.getOrCreateTag()
                );
            }

            @Override
            public boolean equals(final Object o) {
                if (!(o instanceof final ItemStackEqualityWrapper other)) {
                    return false;
                }

                return Objects.equals(
                        ForgeRegistries.ITEMS.getKey(stack.getItem()),
                        ForgeRegistries.ITEMS.getKey(other.stack.getItem())
                ) &&
                        Objects.equals(
                                stack.getDamageValue(),
                                other.stack.getDamageValue()
                        ) &&
                        Objects.equals(
                                stack.getOrCreateTag(),
                                other.stack.getOrCreateTag()
                        );
            }
        }

    public static final class IngredientHandler {

        private static final Logger LOGGER = LogManager.getLogger();

        private static final IngredientHandler INSTANCE = new IngredientHandler();

        public static IngredientHandler getInstance() {
            return INSTANCE;
        }

        private final Map<Ingredient, Exception> cnfExceptions = Maps.newHashMap();
        private final Map<Ingredient, Exception> genericExceptions = Maps.newHashMap();

        public List<IRecipeIngredient> attemptIngredientConversion(
                final Function<Ingredient, List<IRecipeIngredient>> ingredientHandler,
                final Ingredient ingredient
        ) {
            try {
                return this.attemptInternalIngredientConversion(
                        ingredientHandler,
                        ingredient
                );
            } catch (ClassNotFoundException cnfEx) {
                cnfExceptions.put(ingredient, cnfEx);
            } catch (Exception ex) {
                genericExceptions.put(ingredient, ex);
            }

            return Collections.emptyList();
        }

        public void reset() {
            this.cnfExceptions.clear();
            this.genericExceptions.clear();
        }

        public void logErrors() {
            switch (Aequivaleo.getInstance().getConfiguration().getServer().ingredientLogLevelEnumValue.get()) {
                case NONE -> {
                    NoopLoggingHandler.log(cnfExceptions);
                    NoopLoggingHandler.log(genericExceptions);
                }
                case JUST_ITEMS -> {
                    IngredientLoggingHandler.log(cnfExceptions);
                    IngredientLoggingHandler.log(genericExceptions);
                }
                case FULL -> {
                    FullLoggingHandler.log(cnfExceptions);
                    FullLoggingHandler.log(genericExceptions);
                }
            }
        }

        public List<IRecipeIngredient> attemptInternalIngredientConversion(
                final Function<Ingredient, List<IRecipeIngredient>> ingredientHandler,
                final Ingredient ingredient
        ) throws ClassNotFoundException {
            return ingredientHandler.apply(ingredient);
        }

        private static final class IngredientLoggingHandler {
            private static final Logger LOGGER = LogManager.getLogger();

            private static void log(final Map<Ingredient, ? extends Exception> errorMap) {
                errorMap.keySet()
                        .forEach(ingredient -> {
                            logIngredient(ingredient, LOGGER);
                        });
            }

            private static void logIngredient(final Ingredient ingredient, final Logger logger) {
                logger.error(String.format("Failed to process Ingredient. IsVanilla?: %s - IsSimple?: %s. Contained ItemStacks:",
                        ingredient.isVanilla() ? "Yes" : "No",
                        ingredient.isSimple() ? "Yes" : "No"));
                for (final ItemStack matchingStack : ingredient.getItems()) {
                    logger.error(String.format("  > Item: %s - NBT: %s", ForgeRegistries.ITEMS.getKey(matchingStack.getItem()), matchingStack.save(new CompoundTag())));
                }
            }
        }

        private static final class FullLoggingHandler {
            private static final Logger LOGGER = LogManager.getLogger();

            private static void log(final Map<Ingredient, ? extends Throwable> errorMap) {
                errorMap.keySet()
                        .forEach(ingredient -> {
                            IngredientLoggingHandler.logIngredient(ingredient, LOGGER);
                            //noinspection PlaceholderCountMatchesArgumentCount
                            LOGGER.error("Causing exception:", errorMap.get(ingredient));
                            LOGGER.error("");
                            LOGGER.error("");
                        });
            }
        }

        private static final class NoopLoggingHandler {
            private static void log(final Map<Ingredient, ? extends Throwable> errorMap) {
                //Noop
            }
        }
    }

}
