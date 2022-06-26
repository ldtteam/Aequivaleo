package com.ldtteam.aequivaleo.vanilla;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.plugin.AequivaleoPlugin;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.recipe.IRecipeTypeProcessingRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.GenericRecipeEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.calculator.IRecipeCalculator;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.api.util.StreamUtils;
import com.ldtteam.aequivaleo.api.util.TriFunction;
import com.ldtteam.aequivaleo.vanilla.api.IVanillaAequivaleoPluginAPI;
import com.ldtteam.aequivaleo.vanilla.api.VanillaAequivaleoPluginAPI;
import com.ldtteam.aequivaleo.vanilla.api.tags.ITagEquivalencyRegistry;
import com.ldtteam.aequivaleo.vanilla.api.util.Constants;
import com.ldtteam.aequivaleo.vanilla.config.Configuration;
import com.ldtteam.aequivaleo.vanilla.recipe.equivalency.*;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fml.ModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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
        LOGGER.info("Running aequivaleo common setup.");
        LOGGER.debug("Registering tags.");

        configuration.getCommon().itemTagsToRegister
          .get()
          .stream()
          .map(ResourceLocation::new)
          .map(name -> TagKey.create(Registry.ITEM_REGISTRY, name))
          .forEach(ITagEquivalencyRegistry.getInstance()::addTag);

        LOGGER.debug("Registering recipe processing types.");
        IRecipeTypeProcessingRegistry.getInstance()
          .registerAs(Constants.SIMPLE_RECIPE_TYPE, RecipeType.CRAFTING)
          .registerAs(Constants.COOKING_RECIPE_TYPE, RecipeType.SMELTING, RecipeType.BLASTING, RecipeType.CAMPFIRE_COOKING, RecipeType.SMOKING)
          .registerAs(Constants.STONE_CUTTING_RECIPE_TYPE, RecipeType.STONECUTTING)
          .registerAs(Constants.SMITHING_RECIPE_TYPE, RecipeType.SMITHING);

    }

    @Override
    public void onReloadStartedFor(final ServerLevel world)
    {
        final List<Recipe<?>> smeltingRecipe = Lists.newArrayList();

        IRecipeTypeProcessingRegistry
          .getInstance()
          .getRecipeTypesToBeProcessedAs(Constants.COOKING_RECIPE_TYPE)
          .forEach(type -> smeltingRecipe.addAll(getRecipes(type, world)));

        StreamUtils.execute(
          () -> smeltingRecipe
            .parallelStream()
            .forEach(recipe -> processSmeltingRecipe(world, recipe))
        );

        final List<Recipe<?>> stoneCuttingsRecipe = Lists.newArrayList();

        IRecipeTypeProcessingRegistry
          .getInstance()
          .getRecipeTypesToBeProcessedAs(Constants.STONE_CUTTING_RECIPE_TYPE)
          .forEach(type -> stoneCuttingsRecipe.addAll(getRecipes(type, world)));

        StreamUtils.execute(
          () -> stoneCuttingsRecipe
              .parallelStream()
              .forEach(recipe -> processStoneCuttingRecipe(world, recipe))
        );

        final List<Recipe<?>> smithingRecipe = Lists.newArrayList();

        IRecipeTypeProcessingRegistry
          .getInstance()
          .getRecipeTypesToBeProcessedAs(Constants.SMITHING_RECIPE_TYPE)
          .forEach(type -> smithingRecipe.addAll(getRecipes(type, world)));

        StreamUtils.execute(
          () -> smithingRecipe
            .parallelStream()
            .forEach(recipe -> processSmithingRecipe(world, recipe))
        );

        final List<Recipe<?>> craftingRecipes = Lists.newArrayList();

        IRecipeTypeProcessingRegistry
          .getInstance()
          .getRecipeTypesToBeProcessedAs(Constants.SIMPLE_RECIPE_TYPE)
          .forEach(type -> craftingRecipes.addAll(getRecipes(type, world)));

        StreamUtils.execute(
          () ->craftingRecipes
            .parallelStream()
            .forEach(recipe -> processCraftingRecipe(world, recipe))
        );

        final List<Recipe<?>> genericRecipes = Lists.newArrayList();

        final List<Pattern> blackListPatterns = configuration.getCommon().recipeTypeNamePatternsToExclude
          .get()
          .stream()
          .map(Pattern::compile)
          .collect(Collectors.toList());

        final Set<RecipeType<?>> knownTypes = IRecipeTypeProcessingRegistry.getInstance().getAllKnownTypes();
        Registry.RECIPE_TYPE.entrySet().stream()
          .filter(entry -> !knownTypes.contains(entry.getValue()))
          .filter(entry -> blackListPatterns.stream().noneMatch(blp -> blp.matcher(entry.getKey().location().toString()).find()))
          .forEach(
             entry -> genericRecipes.addAll(getRecipes(entry.getValue(), world))
          );

        StreamUtils.execute(
          () -> genericRecipes
            .parallelStream()
            .forEach(recipe -> processGenericRecipe(world, recipe))
        );
    }

    private static List<Recipe<?>> getRecipes(final RecipeType<?> type, final ServerLevel world)
    {
        if (world.getRecipeManager().recipes.get(type) == null)
        {
            LOGGER.error("Could not find any recipes for recipe type: " + type + " its recipes array value is null!");
            return Lists.newArrayList();
        }

        return Lists.newArrayList(world.getRecipeManager().recipes.get(type).values());
    }

    private static void processSmeltingRecipe(@NotNull final ServerLevel world, Recipe<?> iRecipe)
    {
        processIRecipe(world, iRecipe, Recipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new CookingEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private static void processCraftingRecipe(@NotNull final ServerLevel world, Recipe<?> iRecipe)
    {
        processIRecipe(world, iRecipe, Recipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new SimpleEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private static void processStoneCuttingRecipe(@NotNull final ServerLevel world, Recipe<?> iRecipe)
    {
        processIRecipe(world, iRecipe, Recipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new StoneCuttingEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private static void processGenericRecipe(@NotNull final ServerLevel world, Recipe<?> iRecipe)
    {
        processIRecipe(world, iRecipe, Recipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new GenericRecipeEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private static void processSmithingRecipe(@NotNull final ServerLevel world, Recipe<?> iRecipe)
    {
        processIRecipe(world,
          iRecipe,
          smithingRecipe -> {
              if (!(smithingRecipe instanceof UpgradeRecipe))
                  throw new IllegalArgumentException("Recipe is not a smithing recipe.");

              final NonNullList<Ingredient> ingredients = NonNullList.create();
              ingredients.add(((UpgradeRecipe) smithingRecipe).base);
              ingredients.add(((UpgradeRecipe) smithingRecipe).addition);
              return ingredients;
          },
          (inputs, requiredKnownOutputs, outputs) -> new SmithingEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private static void processIRecipe(
      @NotNull final ServerLevel world,
      final Recipe<?> iRecipe,
      final Function<Recipe<?>, NonNullList<Ingredient>> ingredientExtractor,
      final TriFunction<SortedSet<IRecipeIngredient>, SortedSet<ICompoundContainer<?>>, SortedSet<ICompoundContainer<?>>, IEquivalencyRecipe> recipeFactory
    )
    {
        try {
            if (iRecipe.getResultItem().isEmpty())
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

            variants.forEach(recipe -> IEquivalencyRecipeRegistry.getInstance(world.dimension()).register(recipe));
        }
        catch (Exception ex)
        {
            LOGGER.error("A recipe has throw an exception while processing: " + iRecipe.getId(), ex);
        }
    }

    private static void processBucketFluidRecipeFor(
      @NotNull final Level world, final Item item) {
        if (!(item instanceof final BucketItem bucketItem))
            return;

        if (bucketItem.getFluid().isSame(Fluids.EMPTY))
            return;

        final ICompoundContainer<?> emptyBucketContainer = ICompoundContainerFactoryManager.getInstance().wrapInContainer(
          Items.BUCKET, 1
        );
        final IRecipeIngredient emptyBucketIngredient = new SimpleIngredientBuilder().from(emptyBucketContainer).createIngredient();

        final Fluid fluid = bucketItem.getFluid();
        final ICompoundContainer<?> fluidContainer = ICompoundContainerFactoryManager.getInstance().wrapInContainer(
          fluid, 1000
        );
        final IRecipeIngredient fluidIngredient = new SimpleIngredientBuilder().from(fluidContainer).createIngredient();

        final ICompoundContainer<?> fullBucketContainer = ICompoundContainerFactoryManager.getInstance().wrapInContainer(
          bucketItem, 1
        );
        final IRecipeIngredient fullBucketIngredient = new SimpleIngredientBuilder().from(fullBucketContainer
        ).createIngredient();

        final BucketFluidRecipe fillingRecipe = new BucketFluidRecipe(
          Sets.newHashSet(emptyBucketIngredient, fluidIngredient),
          Sets.newHashSet(fullBucketContainer)
        );
        final BucketFluidRecipe emptyingRecipe = new BucketFluidRecipe(
          Sets.newHashSet(fullBucketIngredient),
          Sets.newHashSet(emptyBucketContainer, fluidContainer)
        );

        IEquivalencyRecipeRegistry.getInstance(world.dimension()).register(
          fillingRecipe
        );
        IEquivalencyRecipeRegistry.getInstance(world.dimension()).register(
          emptyingRecipe
        );
    }
}
