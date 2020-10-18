package com.ldtteam.aequivaleo.vanilla;

import com.google.common.collect.Lists;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.plugin.AequivaleoPlugin;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.recipe.IRecipeTypeProcessingRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.calculator.IRecipeCalculator;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.vanilla.api.IVanillaAequivaleoPluginAPI;
import com.ldtteam.aequivaleo.vanilla.api.VanillaAequivaleoPluginAPI;
import com.ldtteam.aequivaleo.vanilla.api.tags.ITagEquivalencyRegistry;
import com.ldtteam.aequivaleo.api.util.TriFunction;
import com.ldtteam.aequivaleo.vanilla.api.util.Constants;
import com.ldtteam.aequivaleo.vanilla.config.Configuration;
import com.ldtteam.aequivaleo.vanilla.recipe.equivalency.FurnaceEquivalencyRecipe;
import com.ldtteam.aequivaleo.vanilla.recipe.equivalency.VanillaCraftingEquivalencyRecipe;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.ModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.SortedSet;
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

        configuration.getServer().tagsToRegister
          .get()
          .stream()
          .map(ResourceLocation::new)
          .forEach(ITagEquivalencyRegistry.getInstance()::addTag);

        LOGGER.debug("Registering recipe processing types.");
        IRecipeTypeProcessingRegistry.getInstance()
          .registerAs(Constants.SIMPLE_RECIPE_TYPE, IRecipeType.CRAFTING)
          .registerAs(Constants.COOKING_RECIPE_TYPE, IRecipeType.SMELTING, IRecipeType.BLASTING, IRecipeType.CAMPFIRE_COOKING, IRecipeType.SMOKING);
    }

    @Override
    public void onReloadStartedFor(final ServerWorld world)
    {

        final List<IRecipe<?>> craftingRecipes = Lists.newArrayList();

        IRecipeTypeProcessingRegistry
          .getInstance()
          .getRecipeTypesToBeProcessedAs(Constants.SIMPLE_RECIPE_TYPE)
          .forEach(type -> craftingRecipes.addAll(getRecipes(type, world)));

        craftingRecipes
          .parallelStream()
          .forEach(recipe -> processCraftingRecipe(world, recipe));

        final List<IRecipe<?>> smeltingRecipe = Lists.newArrayList();

        IRecipeTypeProcessingRegistry
          .getInstance()
          .getRecipeTypesToBeProcessedAs(Constants.COOKING_RECIPE_TYPE)
          .forEach(type -> smeltingRecipe.addAll(getRecipes(type, world)));

        smeltingRecipe
          .parallelStream()
          .forEach(recipe -> processSmeltingRecipe(world, recipe));
    }

    private static List<IRecipe<?>> getRecipes(final IRecipeType type, final World world)
    {
        final List<? extends IRecipe<?>> recipes = world.getRecipeManager().getRecipesForType(type);
        final List<IRecipe<?>> result = Lists.newArrayList(recipes);
        return result;
    }

    private static void processSmeltingRecipe(@NotNull final World world, IRecipe<?> iRecipe)
    {
        processIRecipe(world, iRecipe, (inputs, requiredKnownOutputs, outputs) -> new FurnaceEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private static void processCraftingRecipe(@NotNull final World world, IRecipe<?> iRecipe)
    {
        processIRecipe(world, iRecipe, (inputs, requiredKnownOutputs, outputs) -> new VanillaCraftingEquivalencyRecipe(iRecipe.getId(), inputs, requiredKnownOutputs, outputs));
    }

    private static void processIRecipe(
      @NotNull final World world,
      IRecipe<?> iRecipe,
      TriFunction<SortedSet<IRecipeIngredient>, SortedSet<ICompoundContainer<?>>, SortedSet<ICompoundContainer<?>>, IEquivalencyRecipe> recipeFactory
    )
    {
        if (iRecipe.getRecipeOutput().isEmpty())
        {
            return;
        }

        final List<IEquivalencyRecipe> variants = IRecipeCalculator.getInstance().getAllVariants(
          iRecipe,
          recipeFactory
        ).collect(Collectors.toList());

        if (variants.isEmpty()) {
            LOGGER.error(String.format("Failed to process recipe: %s See ingredient error logs for more information.", iRecipe.getId()));
        }

        variants.forEach(recipe -> {
            IEquivalencyRecipeRegistry.getInstance(world.getDimensionKey()).register(recipe);
        });
    }
}
