package com.ldtteam.aequivaleo.vanilla;

import com.google.common.collect.Lists;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.plugin.AequivaleoPlugin;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.recipe.IRecipeTypeProcessingRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.GenericRecipeEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.calculator.IRecipeCalculator;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.util.TriFunction;
import com.ldtteam.aequivaleo.vanilla.api.IVanillaAequivaleoPluginAPI;
import com.ldtteam.aequivaleo.vanilla.api.VanillaAequivaleoPluginAPI;
import com.ldtteam.aequivaleo.vanilla.api.tags.ITagEquivalencyRegistry;
import com.ldtteam.aequivaleo.vanilla.api.util.Constants;
import com.ldtteam.aequivaleo.vanilla.config.Configuration;
import com.ldtteam.aequivaleo.vanilla.recipe.equivalency.CookingEquivalencyRecipe;
import com.ldtteam.aequivaleo.vanilla.recipe.equivalency.SimpleEquivalencyRecipe;
import com.ldtteam.aequivaleo.vanilla.recipe.equivalency.SmithingEquivalencyRecipe;
import com.ldtteam.aequivaleo.vanilla.recipe.equivalency.StoneCuttingEquivalencyRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SmithingRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.ModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AequivaleoPlugin
public class VanillaAequivaleoPlugin implements IAequivaleoPlugin
{
    private static final Logger LOGGER = LogManager.getLogger();

    private Configuration configuration;

    @Override
    public String getId()
    {
        return "Vanilla";
    }

    @Override
    public void onConstruction()
    {
        LOGGER.info("Started Aequivaleo vanilla plugin.");
        IVanillaAequivaleoPluginAPI.Holder.setInstance(VanillaAequivaleoPluginAPI.getInstance());

        configuration = new Configuration(ModLoadingContext.get().getActiveContainer());
    }

    @Override
    public void onCommonSetup()
    {
        LOGGER.info("Running aequivaleo vanilla plugin common setup.");
        LOGGER.debug("Registering tags.");

        configuration.getServer().tagsToRegister
          .get()
          .stream()
          .map(ResourceLocation::new)
          .forEach(ITagEquivalencyRegistry.getInstance()::addTag);

        LOGGER.debug("Registering recipe processing types.");
        IRecipeTypeProcessingRegistry.getInstance()
          .registerAs(Constants.SIMPLE_RECIPE_TYPE, IRecipeType.CRAFTING)
          .registerAs(Constants.COOKING_RECIPE_TYPE, IRecipeType.SMELTING, IRecipeType.BLASTING, IRecipeType.CAMPFIRE_COOKING, IRecipeType.SMOKING)
          .registerAs(Constants.STONE_CUTTING_RECIPE_TYPE, IRecipeType.STONECUTTING)
          .registerAs(Constants.SMITHING_RECIPE_TYPE, IRecipeType.SMITHING);

    }

    @Override
    public void onReloadStartedFor(final ServerWorld world)
    {
        final List<IRecipe<?>> smeltingRecipe = Lists.newArrayList();

        IRecipeTypeProcessingRegistry
          .getInstance()
          .getRecipeTypesToBeProcessedAs(Constants.COOKING_RECIPE_TYPE)
          .forEach(type -> smeltingRecipe.addAll(getRecipes(type, world)));

        smeltingRecipe
          .parallelStream()
          .forEach(recipe -> processSmeltingRecipe(world, recipe));

        final List<IRecipe<?>> stoneCuttingsRecipe = Lists.newArrayList();

        IRecipeTypeProcessingRegistry
          .getInstance()
          .getRecipeTypesToBeProcessedAs(Constants.STONE_CUTTING_RECIPE_TYPE)
          .forEach(type -> stoneCuttingsRecipe.addAll(getRecipes(type, world)));

        stoneCuttingsRecipe
          .parallelStream()
          .forEach(recipe -> processStoneCuttingRecipe(world, recipe));

        final List<IRecipe<?>> smithingRecipe = Lists.newArrayList();

        IRecipeTypeProcessingRegistry
          .getInstance()
          .getRecipeTypesToBeProcessedAs(Constants.SMITHING_RECIPE_TYPE)
          .forEach(type -> smithingRecipe.addAll(getRecipes(type, world)));

        smithingRecipe
          .parallelStream()
          .forEach(recipe -> processSmithingRecipe(world, recipe));

        final List<IRecipe<?>> craftingRecipes = Lists.newArrayList();

        IRecipeTypeProcessingRegistry
          .getInstance()
          .getRecipeTypesToBeProcessedAs(Constants.SIMPLE_RECIPE_TYPE)
          .forEach(type -> craftingRecipes.addAll(getRecipes(type, world)));

        craftingRecipes
          .parallelStream()
          .forEach(recipe -> processCraftingRecipe(world, recipe));

        final List<IRecipe<?>> genericRecipes = Lists.newArrayList();

        final List<Pattern> blackListPatterns = configuration.getServer().recipeTypeNamePatternsToExclude
          .get()
          .stream()
          .map(Pattern::compile)
          .collect(Collectors.toList());

        final Set<IRecipeType<?>> knownTypes = IRecipeTypeProcessingRegistry.getInstance().getAllKnownTypes();
        Registry.RECIPE_TYPE.getEntries().stream()
          .filter(entry -> !knownTypes.contains(entry.getValue()))
          .filter(entry -> blackListPatterns.stream().noneMatch(blp -> blp.matcher(entry.getKey().getLocation().toString()).find()))
          .forEach(
             entry -> genericRecipes.addAll(getRecipes(entry.getValue(), world))
          );

        genericRecipes
          .parallelStream()
          .forEach(recipe -> processGenericRecipe(world, recipe));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<IRecipe<?>> getRecipes(final IRecipeType type, final World world)
    {
        final List<? extends IRecipe<?>> recipes = world.getRecipeManager().getRecipesForType(type);
        return Lists.newArrayList(recipes);
    }

    private static void processSmeltingRecipe(@NotNull final World world, IRecipe<?> iRecipe)
    {
        processIRecipe(world, iRecipe, IRecipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new CookingEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private static void processCraftingRecipe(@NotNull final World world, IRecipe<?> iRecipe)
    {
        processIRecipe(world, iRecipe, IRecipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new SimpleEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private static void processStoneCuttingRecipe(@NotNull final World world, IRecipe<?> iRecipe)
    {
        processIRecipe(world, iRecipe, IRecipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new StoneCuttingEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private static void processGenericRecipe(@NotNull final World world, IRecipe<?> iRecipe)
    {
        processIRecipe(world, iRecipe, IRecipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new GenericRecipeEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private static void processSmithingRecipe(@NotNull final World world, IRecipe<?> iRecipe)
    {
        processIRecipe(world,
          iRecipe,
          smithingRecipe -> {
              if (!(smithingRecipe instanceof SmithingRecipe))
                  throw new IllegalArgumentException("Recipe is not a smithing recipe.");

              final NonNullList<Ingredient> ingredients = NonNullList.create();
              ingredients.add(((SmithingRecipe) smithingRecipe).base);
              ingredients.add(((SmithingRecipe) smithingRecipe).addition);
              return ingredients;
          },
          (inputs, requiredKnownOutputs, outputs) -> new SmithingEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private static void processIRecipe(
      @NotNull final World world,
      final IRecipe<?> iRecipe,
      final Function<IRecipe<?>, NonNullList<Ingredient>> ingredientExtractor,
      final TriFunction<SortedSet<IRecipeIngredient>, SortedSet<ICompoundContainer<?>>, SortedSet<ICompoundContainer<?>>, IEquivalencyRecipe> recipeFactory
    )
    {
        if (iRecipe.getRecipeOutput().isEmpty())
        {
            return;
        }

        final List<IEquivalencyRecipe> variants = IRecipeCalculator.getInstance().getAllVariants(
          iRecipe,
          ingredientExtractor,
          IRecipeCalculator.getInstance()::getAllVariantsFromSimpleIngredient,
          recipeFactory
        ).collect(Collectors.toList());

        if (variants.isEmpty() && !iRecipe.getId().getNamespace().equals("minecraft")) {
            LOGGER.error(String.format("Failed to process recipe: %s See ingredient error logs for more information.", iRecipe.getId()));
        }

        variants.forEach(recipe -> IEquivalencyRecipeRegistry.getInstance(world.getDimensionKey()).register(recipe));
    }
}
