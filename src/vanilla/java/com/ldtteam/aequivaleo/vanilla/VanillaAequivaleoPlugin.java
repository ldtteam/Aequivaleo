package com.ldtteam.aequivaleo.vanilla;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.plugin.AequivaleoPlugin;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IRecipeCalculator;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.tags.ITagEquivalencyRegistry;
import com.ldtteam.aequivaleo.api.util.TriFunction;
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

import java.util.ArrayList;
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
    }

    @Override
    public void onReloadStartedFor(final ServerWorld world)
    {

        final List<ICraftingRecipe> craftingRecipes = world.getRecipeManager().func_241447_a_(IRecipeType.CRAFTING);
        craftingRecipes
          .parallelStream()
          .forEach(recipe -> processCraftingRecipe(world, recipe));

        final List<AbstractCookingRecipe> smeltingRecipe = new ArrayList<>();
        smeltingRecipe.addAll(world.getRecipeManager().func_241447_a_(IRecipeType.SMELTING));
        smeltingRecipe.addAll(world.getRecipeManager().func_241447_a_(IRecipeType.BLASTING));
        smeltingRecipe.addAll(world.getRecipeManager().func_241447_a_(IRecipeType.CAMPFIRE_COOKING));
        smeltingRecipe.addAll(world.getRecipeManager().func_241447_a_(IRecipeType.SMOKING));

        smeltingRecipe
          .parallelStream()
          .forEach(recipe -> processSmeltingRecipe(world, recipe));
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

        variants.forEach(recipe -> {
            IEquivalencyRecipeRegistry.getInstance(world.func_234923_W_()).register(recipe);
        });
    }
}
