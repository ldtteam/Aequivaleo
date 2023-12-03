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
import net.minecraft.world.Container;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AequivaleoPlugin
public class VanillaAequivaleoPlugin implements IAequivaleoPlugin {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private Configuration configuration;
    
    private static <C extends Container, T extends Recipe<C>> List<? extends Recipe<C>> getRecipes(final RecipeType<T> type, final ResourceLocation serializerName, final ServerLevel world) {
        return world.getRecipeManager().getAllRecipesFor(type).stream().filter(recipe -> recipe.value().getType() == type)
                       .filter(recipe -> world.registryAccess().registryOrThrow(Registries.RECIPE_SERIALIZER).getKey(recipe.value().getSerializer()).equals(serializerName))
                       .map(RecipeHolder::value)
                       .toList();
    }
    
    private static <C extends Container, T extends Recipe<C>> List<? extends Recipe<C>> getRecipes(final RecipeType<T> type, final ServerLevel world) {
        return world.getRecipeManager().getAllRecipesFor(type).stream().filter(recipe -> recipe.value().getType() == type)
                       .map(RecipeHolder::value)
                       .toList();
    }
    
    private void processSmeltingRecipe(@NotNull final ServerLevel world, Recipe<?> iRecipe) {
        processRecipe(world, iRecipe, Recipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new CookingEquivalencyRecipe(getRecipeId(world, iRecipe), inputs, requiredKnownOutputs, outputs));
    }
    
    private void processCraftingRecipe(@NotNull final ServerLevel world, Recipe<?> iRecipe) {
        processRecipe(world, iRecipe, Recipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new SimpleEquivalencyRecipe(getRecipeId(world, iRecipe), inputs, requiredKnownOutputs, outputs));
    }
    
    private void processStoneCuttingRecipe(@NotNull final ServerLevel world, Recipe<?> iRecipe) {
        processRecipe(world, iRecipe, Recipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new StoneCuttingEquivalencyRecipe(getRecipeId(world, iRecipe), inputs, requiredKnownOutputs, outputs));
    }
    
    private void processGenericRecipe(@NotNull final ServerLevel world, Recipe<?> iRecipe) {
        processRecipe(world, iRecipe, Recipe::getIngredients, (inputs, requiredKnownOutputs, outputs) -> new GenericRecipeEquivalencyRecipe(getRecipeId(world, iRecipe), inputs, requiredKnownOutputs, outputs));
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
                (inputs, requiredKnownOutputs, outputs) -> new SmithingEquivalencyRecipe(getRecipeId(world, iRecipe), inputs, requiredKnownOutputs, outputs));
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
                (inputs, requiredKnownOutputs, outputs) -> new SmithingEquivalencyRecipe(getRecipeId(world, iRecipe), inputs, requiredKnownOutputs, outputs));
    }
    
    private static void processDecoratedPotRecipe(@NotNull final ServerLevel world) {
        world.registryAccess().registry(Registries.ITEM).orElseThrow()
                .getTag(ItemTags.DECORATED_POT_SHERDS)
                .ifPresent(holder -> {
                    final List<Item> sherds = holder.stream().map(Holder::value).toList();
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
            
            if (configuration.getCommon().logEmptyVariantsWarning.get() && variants.isEmpty() && !getRecipeId(world, recipe).getNamespace().equals("minecraft")) {
                LOGGER.error(String.format("Failed to process recipe: %s See ingredient error logs for more information.", getRecipeId(world, recipe)));
            }
            
            variants.forEach(variant -> IEquivalencyRecipeRegistry.getInstance(world.dimension()).register(variant));
        } catch (Exception ex) {
            LOGGER.error("A recipe has throw an exception while processing: " + getRecipeId(world, recipe), ex);
        }
    }
    
    public void processPotionRecipe(ServerLevel level, PotionBrewing.Mix<Potion> recipe, ItemStack container) {
        final ItemStack inputStack = PotionUtils.setPotion(container, recipe.from);
        final ItemStack outputStack = PotionUtils.setPotion(container, recipe.to);
        
        for (ItemStack reagent : recipe.ingredient.getItems()) {
            IEquivalencyRecipeRegistry.getInstance(level.dimension())
                    .register(new PotionEquivalencyRecipe(inputStack, reagent, outputStack));
        }
    }
    
    public void processPotionContainerRecipe(ServerLevel level, PotionBrewing.Mix<Item> recipe) {
        final ItemStack inputStack = new ItemStack(recipe.from);
        final ItemStack outputStack = new ItemStack(recipe.to);
        
        for (ItemStack reagent : recipe.ingredient.getItems()) {
            IEquivalencyRecipeRegistry.getInstance(level.dimension())
                    .register(new PotionEquivalencyRecipe(inputStack, reagent, outputStack));
        }
    }
    
    public void processRecipeWithInAndOut(ServerLevel level, String name, ItemStack outStack, ItemStack... inStacks) {
        IEquivalencyRecipeRegistry.getInstance(level.dimension()).register(new SimpleEquivalencyRecipe(new ResourceLocation("custom/" + name),
                Arrays.stream(inStacks).map(inStack -> ICompoundContainerFactoryManager.getInstance().wrapInContainer(inStack.copyWithCount(1), inStack.getCount()))
                        .map(IRecipeIngredient::from).collect(Collectors.toSet()),
                Arrays.stream(inStacks)
                        .map(ItemStack::getCraftingRemainingItem)
                        .filter(stack -> !stack.isEmpty())
                        .map(inStack -> ICompoundContainerFactoryManager.getInstance().wrapInContainer(inStack.copyWithCount(1), inStack.getCount()))
                        .collect(Collectors.toSet()),
                Set.of(ICompoundContainerFactoryManager.getInstance().wrapInContainer(outStack.copyWithCount(1), outStack.getCount()))));
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
        final IRecipeIngredient emptyBucketIngredient = IRecipeIngredient.from(emptyBucketContainer);
        
        final Fluid fluid = bucketItem.getFluid();
        final ICompoundContainer<?> fluidContainer = ICompoundContainerFactoryManager.getInstance().wrapInContainer(
                fluid, 1000
        );
        final IRecipeIngredient fluidIngredient = IRecipeIngredient.from(fluidContainer);
        
        final ICompoundContainer<?> fullBucketContainer = ICompoundContainerFactoryManager.getInstance().wrapInContainer(
                bucketItem, 1
        );
        final IRecipeIngredient fullBucketIngredient = IRecipeIngredient.from(fullBucketContainer);
        
        final BucketFluidRecipe fillingRecipe = new BucketFluidRecipe(
                Sets.newHashSet(emptyBucketIngredient, fluidIngredient),
                Sets.newHashSet(),
                Sets.newHashSet(fullBucketContainer)
        );
        final BucketFluidRecipe emptyingRecipe = new BucketFluidRecipe(
                Sets.newHashSet(fullBucketIngredient),
                Sets.newHashSet(emptyBucketContainer),
                Sets.newHashSet(fluidContainer)
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
        if (true)
            return false;
        
        return isShapelessColoringRecipe(world, recipe) || isShapedCountedColorRecipe(world, recipe);
    }
    
    private static boolean isShapelessColoringRecipe(@NotNull ServerLevel world, Recipe<?> recipe) {
        return recipe.getSerializer() == RecipeSerializer.SHAPELESS_RECIPE &&
                       recipe.getResultItem(world.registryAccess()).is(Tags.Items.BLOCKED_COLOR_CYCLE) &&
                       recipe.getIngredients().size() == 2 &&
                       recipe.getIngredients().get(0).getItems().length == 1 &&
                       recipe.getIngredients().get(0).getItems()[0].is(net.neoforged.neoforge.common.Tags.Items.DYES) &&
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
        
        if (!colorIngredient.getItems()[0].is(net.neoforged.neoforge.common.Tags.Items.DYES))
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
                .forEach(type -> collectRecipesForTypeInto(world, type, smeltingRecipe));
        
        StreamUtils.execute(
                () -> smeltingRecipe
                              .parallelStream()
                              .forEach(recipe -> processSmeltingRecipe(world, recipe))
        );
        
        final List<Recipe<?>> stoneCuttingsRecipe = Lists.newArrayList();
        
        IRecipeTypeProcessingRegistry
                .getInstance()
                .getRecipeTypesToBeProcessedAs(Constants.STONE_CUTTING_RECIPE_TYPE)
                .forEach(type -> collectRecipesForTypeInto(world, type, stoneCuttingsRecipe));
        
        StreamUtils.execute(
                () -> stoneCuttingsRecipe
                              .parallelStream()
                              .forEach(recipe -> processStoneCuttingRecipe(world, recipe))
        );
        
        final List<Recipe<?>> smithingTransformRecipes = Lists.newArrayList();
        
        IRecipeTypeProcessingRegistry
                .getInstance()
                .getRecipeTypesToBeProcessedAs(Constants.SMITHING_TRANSFORM_RECIPE_TYPE)
                .forEach(type -> collectRecipesForTypeInto(world, type, Constants.SMITHING_TRANSFORM_RECIPE_TYPE, smithingTransformRecipes));
        
        StreamUtils.execute(
                () -> smithingTransformRecipes
                              .parallelStream()
                              .forEach(recipe -> processSmithingTransformRecipe(world, recipe))
        );
        
        final List<Recipe<?>> smithingTrimRecipes = Lists.newArrayList();
        
        IRecipeTypeProcessingRegistry
                .getInstance()
                .getRecipeTypesToBeProcessedAs(Constants.SMITHING_TRIM_RECIPE_TYPE)
                .forEach(type -> collectRecipesForTypeInto(world, type, Constants.SMITHING_TRIM_RECIPE_TYPE, smithingTrimRecipes));
        
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
                .forEach(type -> collectRecipesForTypeInto(world, type, craftingRecipes));
        
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
                        entry -> collectRecipesForTypeInto(world, entry.getValue(), genericRecipes)
                );
        
        StreamUtils.execute(
                () -> genericRecipes
                              .parallelStream()
                              .forEach(recipe -> processGenericRecipe(world, recipe))
        );
        
        processCustomRecipes(world);
        
        processWaterBottleFillRecipe(world);
        
        processPotionRecipes(world);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T extends Recipe<C>, C extends Container> void collectRecipesForTypeInto(ServerLevel world, RecipeType type, ResourceLocation serializerId, List<Recipe<?>> smeltingRecipe) {
        smeltingRecipe.addAll(getRecipes(type, serializerId, world));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T extends Recipe<C>, C extends Container> void collectRecipesForTypeInto(ServerLevel world, RecipeType type, List<Recipe<?>> smeltingRecipe) {
        smeltingRecipe.addAll(getRecipes(type, world));
    }
    
    private void processCustomRecipes(ServerLevel world) {
        processRecipeWithInAndOut(world, "concrete_from_powder_black", new ItemStack(Blocks.BLACK_CONCRETE), new ItemStack(Blocks.BLACK_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "concrete_from_powder_blue", new ItemStack(Blocks.BLUE_CONCRETE), new ItemStack(Blocks.BLUE_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "concrete_from_powder_brown", new ItemStack(Blocks.BROWN_CONCRETE), new ItemStack(Blocks.BROWN_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "concrete_from_powder_cyan", new ItemStack(Blocks.CYAN_CONCRETE), new ItemStack(Blocks.CYAN_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "concrete_from_powder_gray", new ItemStack(Blocks.GRAY_CONCRETE), new ItemStack(Blocks.GRAY_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "concrete_from_powder_green", new ItemStack(Blocks.GREEN_CONCRETE), new ItemStack(Blocks.GREEN_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "concrete_from_powder_light_blue", new ItemStack(Blocks.LIGHT_BLUE_CONCRETE), new ItemStack(Blocks.LIGHT_BLUE_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "concrete_from_powder_lime", new ItemStack(Blocks.LIME_CONCRETE), new ItemStack(Blocks.LIME_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "concrete_from_powder_magenta", new ItemStack(Blocks.MAGENTA_CONCRETE), new ItemStack(Blocks.MAGENTA_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "concrete_from_powder_orange", new ItemStack(Blocks.ORANGE_CONCRETE), new ItemStack(Blocks.ORANGE_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "concrete_from_powder_pink", new ItemStack(Blocks.PINK_CONCRETE), new ItemStack(Blocks.PINK_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "concrete_from_powder_purple", new ItemStack(Blocks.PURPLE_CONCRETE), new ItemStack(Blocks.PURPLE_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "concrete_from_powder_red", new ItemStack(Blocks.RED_CONCRETE), new ItemStack(Blocks.RED_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "concrete_from_powder_light_gray", new ItemStack(Blocks.LIGHT_GRAY_CONCRETE), new ItemStack(Blocks.LIGHT_GRAY_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "concrete_from_powder_white", new ItemStack(Blocks.WHITE_CONCRETE), new ItemStack(Blocks.WHITE_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "concrete_from_powder_yellow", new ItemStack(Blocks.YELLOW_CONCRETE), new ItemStack(Blocks.YELLOW_CONCRETE_POWDER));
        processRecipeWithInAndOut(world, "colored_shulker_box_black", new ItemStack(Blocks.BLACK_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.BLACK_DYE));
        processRecipeWithInAndOut(world, "colored_shulker_box_blue", new ItemStack(Blocks.BLUE_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.BLUE_DYE));
        processRecipeWithInAndOut(world, "colored_shulker_box_brown", new ItemStack(Blocks.BROWN_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.BROWN_DYE));
        processRecipeWithInAndOut(world, "colored_shulker_box_cyan", new ItemStack(Blocks.CYAN_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.CYAN_DYE));
        processRecipeWithInAndOut(world, "colored_shulker_box_gray", new ItemStack(Blocks.GRAY_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.GRAY_DYE));
        processRecipeWithInAndOut(world, "colored_shulker_box_green", new ItemStack(Blocks.GREEN_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.GREEN_DYE));
        processRecipeWithInAndOut(world, "colored_shulker_box_light_blue", new ItemStack(Blocks.LIGHT_BLUE_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.LIGHT_BLUE_DYE));
        processRecipeWithInAndOut(world, "colored_shulker_box_lime", new ItemStack(Blocks.LIME_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.LIME_DYE));
        processRecipeWithInAndOut(world, "colored_shulker_box_magenta", new ItemStack(Blocks.MAGENTA_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.MAGENTA_DYE));
        processRecipeWithInAndOut(world, "colored_shulker_box_orange", new ItemStack(Blocks.ORANGE_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.ORANGE_DYE));
        processRecipeWithInAndOut(world, "colored_shulker_box_pink", new ItemStack(Blocks.PINK_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.PINK_DYE));
        processRecipeWithInAndOut(world, "colored_shulker_box_purple", new ItemStack(Blocks.PURPLE_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.PURPLE_DYE));
        processRecipeWithInAndOut(world, "colored_shulker_box_red", new ItemStack(Blocks.RED_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.RED_DYE));
        processRecipeWithInAndOut(world, "colored_shulker_box_light_gray", new ItemStack(Blocks.LIGHT_GRAY_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.LIGHT_GRAY_DYE));
        processRecipeWithInAndOut(world, "colored_shulker_box_white", new ItemStack(Blocks.WHITE_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.WHITE_DYE));
        processRecipeWithInAndOut(world, "colored_shulker_box_yellow", new ItemStack(Blocks.YELLOW_SHULKER_BOX), new ItemStack(Blocks.SHULKER_BOX), new ItemStack(Items.YELLOW_DYE));
    }
    
    private void processWaterBottleFillRecipe(ServerLevel world) {
        final BucketFluidRecipe fillBottleRecipe = new BucketFluidRecipe(
                Set.of(IRecipeIngredient.from(Items.GLASS_BOTTLE, 1), IRecipeIngredient.from(Fluids.WATER, 250)),
                Set.of(),
                Set.of(ICompoundContainer.from(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)))
        );
    }
    
    private void processPotionRecipes(ServerLevel world) {
        for (PotionBrewing.Mix<Item> containerMix : PotionBrewing.CONTAINER_MIXES) {
            processPotionContainerRecipe(world, containerMix);
        }
        
        for (Ingredient container : PotionBrewing.ALLOWED_CONTAINERS) {
            for (ItemStack containerStack : container.getItems()) {
                for (PotionBrewing.Mix<Potion> potionMix : PotionBrewing.POTION_MIXES) {
                    processPotionRecipe(world, potionMix, containerStack);
                }
            }
        }
    }
    
    private static ResourceLocation getRecipeId(final ServerLevel level, final Recipe<?> recipe) {
        return getAllRecipesFor(level, recipe.getType())
                       .stream()
                       .filter(holder -> holder.value().equals(recipe))
                       .map(RecipeHolder::id)
                       .findFirst()
                       .orElseThrow(() -> new IllegalStateException("Could not find recipe id for recipe!"));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static  <C extends Container, T extends Recipe<C>> List<RecipeHolder<T>> getAllRecipesFor(final ServerLevel level, RecipeType type) {
        return level.getRecipeManager().getAllRecipesFor(type);
    }
}
