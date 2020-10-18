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
import com.ldtteam.aequivaleo.config.Configuration;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeCalculator implements IRecipeCalculator
{

    private static final RecipeCalculator INSTANCE = new RecipeCalculator();

    public static RecipeCalculator getInstance()
    {
        return INSTANCE;
    }

    private RecipeCalculator()
    {
    }

    @Override
    public Stream<IEquivalencyRecipe> getAllVariants(
      final IRecipe<?> recipe,
      final Function<Ingredient, List<IRecipeIngredient>> ingredientHandler,
      final TriFunction<SortedSet<IRecipeIngredient>, SortedSet<ICompoundContainer<?>>, SortedSet<ICompoundContainer<?>>, IEquivalencyRecipe> recipeFactory
    )
    {
        if (recipe.isDynamic())
        {
            return Stream.empty();
        }

        if (recipe.getRecipeOutput().isEmpty())
        {
            return Stream.empty();
        }

        final ICompoundContainer<?> result = CompoundContainerFactoryManager.getInstance().wrapInContainer(recipe.getRecipeOutput(), recipe.getRecipeOutput().getCount());
        final SortedSet<ICompoundContainer<?>> resultSet = new TreeSet<>();
        resultSet.add(result);

        final List<SortedSet<IRecipeIngredient>> variants = getAllInputVariants(recipe.getIngredients()
                                                                                  .stream()
                                                                                  .filter(i -> i.getMatchingStacks().length > 0)
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
                                                       .map(itemStackIntegerPair -> {
                                                           final ItemStack containerStack = itemStackIntegerPair.getKey().getContainerItem();
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
      final Function<Ingredient, List<IRecipeIngredient>> ingredientHandler)
    {
        if (mcIngredients.isEmpty() || (checkSimple && !Aequivaleo.getInstance().getConfiguration().getServer().allowNoneSimpleIngredients.get() && mcIngredients.stream().anyMatch(ingredient -> !ingredient.isSimple())))
        {
            return Collections.emptyList();
        }

        if (mcIngredients.size() == 1)
        {
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
        final List<ItemStack> stacks = Arrays.asList(ingredient.getMatchingStacks());
        final Collection<Collection<ItemStack>> groupedByContainer =
          GroupingUtils.groupBy(stacks, stack -> new ItemStackEqualityWrapper(stack.getContainerItem()));

        return groupedByContainer
          .stream()
          .map(this::mapStacks)
          .map(wrappedStacks -> new SimpleIngredientBuilder().from(wrappedStacks).withCount(1d).createIngredient())
          .collect(Collectors.toList());
    }

    private SortedSet<ICompoundContainer<?>> mapStacks(final Collection<ItemStack> stacks) {
        final List<ICompoundContainer<?>> wrappedStacks =
          stacks.stream().map(stack -> CompoundContainerFactoryManager.getInstance().wrapInContainer(stack, stack.getCount())).collect(Collectors.toList());

        final Collection<Collection<ICompoundContainer<?>>> groupedStacks = GroupingUtils.groupBy(
          wrappedStacks,
          s -> CompoundContainerFactoryManager.getInstance().wrapInContainer(s.getContents(), 1)
        );

        final SortedSet<ICompoundContainer<?>> result =
          groupedStacks
            .stream()
            .map(c -> CompoundContainerFactoryManager.getInstance().wrapInContainer(c.iterator().next().getContents(), c.stream().mapToDouble(ICompoundContainer::getContentsCount).sum()))
            .collect(Collectors.toCollection(TreeSet::new));

        return result;
    }

    private static final class ItemStackEqualityWrapper
    {
        private final ItemStack stack;

        private ItemStackEqualityWrapper(final ItemStack stack) {this.stack = stack;}

        @Override
        public int hashCode()
        {
            return Objects.hash(
              stack.getItem().getRegistryName(),
              stack.getDamage(),
              stack.getOrCreateTag()
            );
        }

        @Override
        public boolean equals(final Object o)
        {
            if (!(o instanceof ItemStackEqualityWrapper))
            {
                return false;
            }

            final ItemStackEqualityWrapper other = (ItemStackEqualityWrapper) o;

            return Objects.equals(
              stack.getItem().getRegistryName(),
              other.stack.getItem().getRegistryName()
            ) &&
                     Objects.equals(
                       stack.getDamage(),
                       other.stack.getDamage()
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

        public List<IRecipeIngredient> attemptIngredientConversion (
          final Function<Ingredient, List<IRecipeIngredient>> ingredientHandler,
          final Ingredient ingredient
        ) {
            try
            {
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
                case NONE:
                    NoopLoggingHandler.log(cnfExceptions);
                    NoopLoggingHandler.log(genericExceptions);
                    break;
                case JUST_ITEMS:
                    IngredientLoggingHandler.log(cnfExceptions);
                    IngredientLoggingHandler.log(genericExceptions);
                    break;
                case FULL:
                    FullLoggingHandler.log(cnfExceptions);
                    FullLoggingHandler.log(genericExceptions);
                    break;
            }
        }

        public List<IRecipeIngredient> attemptInternalIngredientConversion (
          final Function<Ingredient, List<IRecipeIngredient>> ingredientHandler,
          final Ingredient ingredient
        ) throws ClassNotFoundException
        {
            return ingredientHandler.apply(ingredient);
        }

        private static final class IngredientLoggingHandler {
            private static final Logger LOGGER = LogManager.getLogger();

            private static void log(final Map<Ingredient, ? extends Exception> errorMap) {
                errorMap.keySet()
                  .forEach(ingredient -> {
                      LOGGER.error(String.format("Failed to process Ingredient. IsVanilla?: %s - IsSimple?: %s. Contained ItemStacks:",
                        ingredient.isVanilla() ? "Yes" : "No",
                        ingredient.isSimple() ? "Yes" : "No"));
                      for (final ItemStack matchingStack : ingredient.getMatchingStacks())
                      {
                          LOGGER.error(String.format("  > Item: %s - NBT: %s", matchingStack.getItem().getRegistryName(), matchingStack.write(new CompoundNBT()).toString()));
                      }
                  });
            }
        }

        private static final class FullLoggingHandler {
            private static final Logger LOGGER = LogManager.getLogger();

            private static void log(final Map<Ingredient, ? extends Throwable> errorMap) {
                errorMap.keySet()
                  .forEach(ingredient -> {
                      LOGGER.error(String.format("Failed to process Ingredient. IsVanilla?: %s - IsSimple?: %s. Contained ItemStacks:",
                        ingredient.isVanilla() ? "Yes" : "No",
                        ingredient.isSimple() ? "Yes" : "No"));
                      for (final ItemStack matchingStack : ingredient.getMatchingStacks())
                      {
                          LOGGER.error(String.format("  > Item: %s - NBT: %s", matchingStack.getItem().getRegistryName(), matchingStack.write(new CompoundNBT()).toString()));
                      }
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
