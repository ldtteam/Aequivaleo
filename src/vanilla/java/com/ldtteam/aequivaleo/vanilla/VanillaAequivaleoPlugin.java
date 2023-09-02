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
import com.ldtteam.aequivaleo.api.tags.Tags;
import com.ldtteam.aequivaleo.api.util.StreamUtils;
import com.ldtteam.aequivaleo.api.util.TriFunction;
import com.ldtteam.aequivaleo.vanilla.api.IVanillaAequivaleoPluginAPI;
import com.ldtteam.aequivaleo.vanilla.api.VanillaAequivaleoPluginAPI;
import com.ldtteam.aequivaleo.vanilla.api.tags.ITagEquivalencyRegistry;
import com.ldtteam.aequivaleo.vanilla.api.util.Constants;
import com.ldtteam.aequivaleo.vanilla.config.Configuration;
import com.ldtteam.aequivaleo.vanilla.recipe.equivalency.*;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fml.ModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

@AequivaleoPlugin
public class VanillaAequivaleoPlugin implements IAequivaleoPlugin {
    private static final Logger LOGGER = LogManager.getLogger();

    private Configuration configuration;

    private static List<Recipe<?>> getRecipes(final RecipeType<?> type, final ResourceLocation serializerName, final ServerLevel world) {
        if (world.getRecipeManager().recipes.get(type) == null) {
            LOGGER.error("Could not find any recipes for recipe type: " + type + " its recipes array value is null!");
            return Lists.newArrayList();
        }

        return world.getRecipeManager().recipes.get(type).values().stream().filter(recipe -> recipe.getType() == type)
                .filter(recipe -> world.registryAccess().registryOrThrow(Registries.RECIPE_SERIALIZER).getKey(recipe.getSerializer()) == serializerName)
                .toList();
    }

    private static List<Recipe<?>> getRecipes(final RecipeType<?> type, final ServerLevel world) {
        if (world.getRecipeManager().recipes.get(type) == null) {
            LOGGER.error("Could not find any recipes for recipe type: " + type + " its recipes array value is null!");
            return Lists.newArrayList();
        }

        return world.getRecipeManager().recipes.get(type).values().stream().filter(recipe -> recipe.getType() == type)
                .toList();
    }

    private void processSmeltingRecipe(@NotNull final ServerLevel world, Recipe<?> iRecipe) {
        processRecipe(world, iRecipe, Recipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new CookingEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private void processCraftingRecipe(@NotNull final ServerLevel world, Recipe<?> iRecipe) {
        processRecipe(world, iRecipe, Recipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new SimpleEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private void processStoneCuttingRecipe(@NotNull final ServerLevel world, Recipe<?> iRecipe) {
        processRecipe(world, iRecipe, Recipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new StoneCuttingEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private void processGenericRecipe(@NotNull final ServerLevel world, Recipe<?> iRecipe) {
        processRecipe(world, iRecipe, Recipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new GenericRecipeEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private void processSmithingTransformRecipe(@NotNull final ServerLevel world, Recipe<?> iRecipe) {
        processRecipe(world,
                iRecipe,
                smithingRecipe -> {
                    if (!(smithingRecipe instanceof SmithingTransformRecipe))
                        throw new IllegalArgumentException("Recipe is not a smithing recipe.");

                    final NonNullList<Ingredient> ingredients = NonNullList.create();
                    ingredients.add(((SmithingTransformRecipe) smithingRecipe).template);
                    ingredients.add(((SmithingTransformRecipe) smithingRecipe).base);
                    ingredients.add(((SmithingTransformRecipe) smithingRecipe).addition);
                    return ingredients;
                },
                (inputs, requiredKnownOutputs, outputs) -> new SmithingEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private void processSmithingTrimRecipe(@NotNull final ServerLevel world, Recipe<?> iRecipe) {
        processRecipe(world,
                iRecipe,
                smithingRecipe -> {
                    if (!(smithingRecipe instanceof SmithingTrimRecipe))
                        throw new IllegalArgumentException("Recipe is not a smithing recipe.");

                    final NonNullList<Ingredient> ingredients = NonNullList.create();
                    ingredients.add(((SmithingTrimRecipe) smithingRecipe).template);
                    ingredients.add(((SmithingTrimRecipe) smithingRecipe).base);
                    ingredients.add(((SmithingTrimRecipe) smithingRecipe).addition);
                    return ingredients;
                },
                (inputs, requiredKnownOutputs, outputs) -> new SmithingEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private static void processDecoratedPotRecipe(@NotNull final ServerLevel world) {
        world.registryAccess().registry(Registries.ITEM).orElseThrow()
                .getTag(ItemTags.DECORATED_POT_SHARDS)
                .ifPresent(holder -> {
                    final List<Item> sherds = holder.stream().map(Holder::get).toList();
                    final List<List<Item>> permutedSherds = IRecipeCalculator.getInstance().getAllPerturbations(sherds, 4);
                    final List<DecoratedPotEquivalencyRecipe> recipes = permutedSherds.stream()
                            .map(sherdList -> {
                                final DecoratedPotBlockEntity.Decorations decorations = new DecoratedPotBlockEntity.Decorations(sherdList.get(0), sherdList.get(1), sherdList.get(2), sherdList.get(3));
                                return new DecoratedPotEquivalencyRecipe(world, decorations);
                            })
                            .toList();

                    recipes.forEach(recipe -> IEquivalencyRecipeRegistry.getInstance(world.dimension()).register(recipe));
                });
    }

    private void processRecipe(
            @NotNull final ServerLevel world,
            final Recipe<?> recipe,
            final Function<Recipe<?>, NonNullList<Ingredient>> ingredientExtractor,
            final TriFunction<SortedSet<IRecipeIngredient>, SortedSet<ICompoundContainer<?>>, SortedSet<ICompoundContainer<?>>, IEquivalencyRecipe> recipeFactory
    ) {
        try {
            if (recipe.getResultItem(world.registryAccess()).isEmpty()) {
                return;
            }

            if (isNotCompatibleRecipe(world, recipe)) {
                return;
            }

            final List<IEquivalencyRecipe> variants = IRecipeCalculator.getInstance().getAllVariants(
                    world,
                    recipe,
                    ingredientExtractor,
                    IRecipeCalculator.getInstance()::getAllVariantsFromSimpleIngredient,
                    recipeFactory
            ).toList();

            if (configuration.getCommon().logEmptyVariantsWarning.get() && variants.isEmpty() && !recipe.getId().getNamespace().equals("minecraft")) {
                LOGGER.error(String.format("Failed to process recipe: %s See ingredient error logs for more information.", recipe.getId()));
            }

            variants.forEach(variant -> IEquivalencyRecipeRegistry.getInstance(world.dimension()).register(variant));
        } catch (Exception ex) {
            LOGGER.error("A recipe has throw an exception while processing: " + recipe.getId(), ex);
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

    private static boolean isNotCompatibleRecipe(@NotNull final ServerLevel world, final Recipe<?> recipe) {
        return isDyeingRecipe(world, recipe);
    }

    private static boolean isDyeingRecipe(@NotNull final ServerLevel world, final Recipe<?> recipe) {
        return isShapelessColoringRecipe(world, recipe) || isShapedCountedColorRecipe(world, recipe);
    }

    private static boolean isShapelessColoringRecipe(@NotNull ServerLevel world, Recipe<?> recipe) {
        return recipe.getSerializer() == RecipeSerializer.SHAPELESS_RECIPE &&
                recipe.getResultItem(world.registryAccess()).is(Tags.Items.BLOCKED_COLOR_CYCLE) &&
                recipe.getIngredients().size() == 2 &&
                recipe.getIngredients().get(0).getItems().length == 1 &&
                recipe.getIngredients().get(0).getItems()[0].is(net.minecraftforge.common.Tags.Items.DYES) &&
                recipe.getIngredients().get(1).getItems().length == 15;
    }

    private static boolean isShapedCountedColorRecipe(@NotNull ServerLevel world, Recipe<?> recipe) {
        if (recipe.getSerializer() != RecipeSerializer.SHAPED_RECIPE)
            return false;

        if (recipe.getIngredients().size() != 9)
            return false;

        final Ingredient colorIngredient = recipe.getIngredients().get(4);
        if (colorIngredient.getItems().length != 1)
            return false;

        if (!colorIngredient.getItems()[0].is(net.minecraftforge.common.Tags.Items.DYES))
            return false;

        final List<ItemStack[]> noneAirNoneColorIngredients = new ArrayList<>();
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            if (i == 4)
                continue;

            final Ingredient candidate = recipe.getIngredients().get(i);
            if (candidate.isEmpty())
                continue;

            noneAirNoneColorIngredients.add(candidate.getItems());
        }

        if (!areAllTheSameItemType(noneAirNoneColorIngredients, recipe.getResultItem(world.registryAccess())))
            return false;

        return noneAirNoneColorIngredients.size() == recipe.getResultItem(world.registryAccess()).getCount();
    }

    private static boolean areAllTheSameItemType(List<ItemStack[]> items, ItemStack... others) {
        if (items.isEmpty())
            return true;

        if (items.size() == 1 && items.get(0).length <= 1)
            return true;

        final List<ItemStack[]> workingList = new ArrayList<>(items);
        workingList.add(others);

        ItemStack item = workingList.get(0)[0];
        return workingList.stream().flatMap(Arrays::stream).allMatch(stack -> stack.getItem().getClass().equals(item.getItem().getClass()));
    }

    @Override
    public String getId() {
        return "Vanilla";
    }

    @Override
    public void onConstruction() {
        LOGGER.info("Started Aequivaleo vanilla plugin.");
        IVanillaAequivaleoPluginAPI.Holder.setInstance(VanillaAequivaleoPluginAPI.getInstance());

        configuration = new Configuration(ModLoadingContext.get().getActiveContainer());
    }

    @Override
    public void onCommonSetup() {
        LOGGER.info("Running aequivaleo common setup.");
        LOGGER.debug("Registering tags.");

        configuration.getCommon().itemTagsToRegister
                .get()
                .stream()
                .map(ResourceLocation::new)
                .map(name -> TagKey.create(Registries.ITEM, name))
                .forEach(ITagEquivalencyRegistry.getInstance()::addTag);

        LOGGER.debug("Registering recipe processing types.");
        IRecipeTypeProcessingRegistry.getInstance()
                .registerAs(Constants.SIMPLE_RECIPE_TYPE, RecipeType.CRAFTING)
                .registerAs(Constants.COOKING_RECIPE_TYPE, RecipeType.SMELTING, RecipeType.BLASTING, RecipeType.CAMPFIRE_COOKING, RecipeType.SMOKING)
                .registerAs(Constants.STONE_CUTTING_RECIPE_TYPE, RecipeType.STONECUTTING)
                .registerAs(Constants.SMITHING_TRANSFORM_RECIPE_TYPE, RecipeType.SMITHING)
                .registerAs(Constants.SMITHING_TRIM_RECIPE_TYPE, RecipeType.SMITHING)
                .registerAs(Constants.DECORATED_POT_RECIPE_TYPE, RecipeType.CRAFTING);

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReloadStartedFor(final ServerLevel world) {

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

        final List<Recipe<?>> smithingTransformRecipes = Lists.newArrayList();

        IRecipeTypeProcessingRegistry
                .getInstance()
                .getRecipeTypesToBeProcessedAs(Constants.SMITHING_TRANSFORM_RECIPE_TYPE)
                .forEach(type -> smithingTransformRecipes.addAll(getRecipes(type, Constants.SMITHING_TRANSFORM_RECIPE_TYPE, world)));

        StreamUtils.execute(
                () -> smithingTransformRecipes
                        .parallelStream()
                        .forEach(recipe -> processSmithingTransformRecipe(world, recipe))
        );

        final List<Recipe<?>> smithingTrimRecipes = Lists.newArrayList();

        IRecipeTypeProcessingRegistry
                .getInstance()
                .getRecipeTypesToBeProcessedAs(Constants.SMITHING_TRIM_RECIPE_TYPE)
                .forEach(type -> smithingTrimRecipes.addAll(getRecipes(type, Constants.SMITHING_TRIM_RECIPE_TYPE, world)));

        StreamUtils.execute(
                () -> smithingTrimRecipes
                        .parallelStream()
                        .forEach(recipe -> processSmithingTrimRecipe(world, recipe))
        );

        //processDecoratedPotRecipe(world);

        final List<Recipe<?>> craftingRecipes = Lists.newArrayList();

        IRecipeTypeProcessingRegistry
                .getInstance()
                .getRecipeTypesToBeProcessedAs(Constants.SIMPLE_RECIPE_TYPE)
                .forEach(type -> craftingRecipes.addAll(getRecipes(type, world)));

        StreamUtils.execute(
                () -> craftingRecipes
                        .parallelStream()
                        .forEach(recipe -> processCraftingRecipe(world, recipe))
        );

        final List<Recipe<?>> genericRecipes = Lists.newArrayList();

        final List<Pattern> blackListTypePatterns = configuration.getCommon().recipeTypeNamePatternsToExclude
                .get()
                .stream()
                .map(Pattern::compile)
                .toList();

        final Set<RecipeType<?>> knownTypes = IRecipeTypeProcessingRegistry.getInstance().getAllKnownTypes();
        BuiltInRegistries.RECIPE_TYPE.entrySet().stream()
                .filter(entry -> !knownTypes.contains(entry.getValue()))
                .filter(entry -> blackListTypePatterns.stream().noneMatch(blp -> blp.matcher(entry.getKey().location().toString()).find()))
                .forEach(
                        entry -> genericRecipes.addAll(getRecipes(entry.getValue(), world))
                );

        StreamUtils.execute(
                () -> genericRecipes
                        .parallelStream()
                        .forEach(recipe -> processGenericRecipe(world, recipe))
        );
    }
}
