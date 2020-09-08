package com.ldtteam.aequivaleo.utils;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
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

        final List<SortedSet<IRecipeIngredient>> variants = getAllInputVariants(recipe.getIngredients(), true);

        return variants
             .stream()
             .map(iRecipeIngredients -> {

              final SortedSet<ICompoundContainer<?>> containers =
                mapStacks(
                  iRecipeIngredients.stream()
                    .map(iRecipeIngredient -> iRecipeIngredient.getCandidates()
                      .stream()
                      .map(ICompoundContainer::getContents)
                      .filter(ItemStack.class::isInstance)
                      .map(ItemStack.class::cast)
                      .filter(stack -> !stack.isEmpty())
                      .map(ItemStack::getContainerItem)
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
        if (mcIngredients.isEmpty() || (checkSimple && mcIngredients.stream().anyMatch(ingredient -> !ingredient.isSimple())))
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
        final SortedSet<ICompoundContainer<?>> result =
          stacks.stream().map(stack -> CompoundContainerFactoryManager.getInstance().wrapInContainer(stack, stack.getCount())).collect(Collectors.toCollection(TreeSet::new));
        return result;
    }

    private static final class RecipeData
    {
        private final SortedSet<IRecipeIngredient>     ingredients;
        private final SortedSet<ICompoundContainer<?>> requiredKnownOutputs;
        private final SortedSet<ICompoundContainer<?>> outputs;

        private RecipeData(
          final SortedSet<IRecipeIngredient> ingredients,
          final SortedSet<ICompoundContainer<?>> requiredKnownOutputs, final SortedSet<ICompoundContainer<?>> outputs)
        {
            this.ingredients = ingredients;
            this.requiredKnownOutputs = requiredKnownOutputs;
            this.outputs = outputs;
        }

        public SortedSet<IRecipeIngredient> getIngredients()
        {
            return ingredients;
        }

        public SortedSet<ICompoundContainer<?>> getRequiredKnownOutputs()
        {
            return requiredKnownOutputs;
        }

        public SortedSet<ICompoundContainer<?>> getOutputs()
        {
            return outputs;
        }
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
