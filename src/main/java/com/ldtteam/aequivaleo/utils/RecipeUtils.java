package com.ldtteam.aequivaleo.utils;

import com.ibm.icu.impl.Pair;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.util.GroupingUtils;
import com.ldtteam.aequivaleo.api.util.TriFunction;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RecipeUtils
{

    private RecipeUtils()
    {
        throw new IllegalStateException("Tried to initialize: RecipeUtils but this is a Utility class.");
    }

    public static Stream<IEquivalencyRecipe> getAllVariants(
      final IRecipe<?> recipe,
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
      true);

        return variants
             .stream()
             .map(iRecipeIngredients -> {

              final SortedSet<ICompoundContainer<?>> containers =
                mapStacks(
                  iRecipeIngredients.stream()
                    .map(iRecipeIngredient -> iRecipeIngredient.getCandidates()
                      .stream()
                      .map(container -> Pair.of(container.getContents(), iRecipeIngredient.getRequiredCount().intValue() * container.getContentsCount().intValue()))
                      .filter(integerPair -> integerPair.first instanceof ItemStack)
                      .map(integerPair -> Pair.of((ItemStack) integerPair.first, integerPair.second))
                      .filter(itemStackIntegerPair -> !itemStackIntegerPair.first.isEmpty())
                      .map(itemStackIntegerPair -> {
                          final ItemStack containerStack = itemStackIntegerPair.first.getContainerItem();
                          containerStack.setCount(itemStackIntegerPair.second);
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

    public static List<SortedSet<IRecipeIngredient>> getAllInputVariants(final List<Ingredient> mcIngredients)
    {
        return getAllInputVariants(mcIngredients, true);
    }

    public static List<SortedSet<IRecipeIngredient>> getAllInputVariants(final List<Ingredient> mcIngredients, final boolean checkSimple)
    {
        if (mcIngredients.isEmpty() || (checkSimple && !Aequivaleo.getInstance().getConfiguration().getServer().allowNoneSimpleIngredients.get() && mcIngredients.stream().anyMatch(ingredient -> !ingredient.isSimple())))
        {
            return Collections.emptyList();
        }

        if (mcIngredients.size() == 1)
        {
            return getAllVariantsFromSimpleIngredient(mcIngredients.get(0))
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

        final List<IRecipeIngredient> targetIngredients = getAllVariantsFromSimpleIngredient(target);
        final Set<IRecipeIngredient> targets = new TreeSet<>(targetIngredients);

        final List<SortedSet<IRecipeIngredient>> subVariants = getAllInputVariants(ingredients, false);

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

    private static List<IRecipeIngredient> getAllVariantsFromSimpleIngredient(final Ingredient ingredient) {
        return Arrays.stream(
          ingredient.getMatchingStacks()
        ).collect(Collectors.groupingBy(stack -> new ItemStackEqualityWrapper(stack.getContainerItem())))
                           .values()
                           .stream()
                           .map(RecipeUtils::mapStacks)
                           .map(stacks -> new SimpleIngredientBuilder().from(stacks).withCount(1d).createIngredient()).collect(Collectors.toList());
    }

    private static SortedSet<ICompoundContainer<?>> mapStacks(final List<ItemStack> stacks) {
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
}
