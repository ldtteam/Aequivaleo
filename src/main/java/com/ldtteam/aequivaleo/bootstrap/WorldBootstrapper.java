package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.analyzer.EquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.util.TriFunction;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.information.locked.LockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.gameobject.equivalent.GameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.ldtteam.aequivaleo.recipe.equivalency.FurnaceEquivalencyRecipe;
import com.ldtteam.aequivaleo.recipe.equivalency.InstancedEquivalency;
import com.ldtteam.aequivaleo.recipe.equivalency.TagEquivalencyRecipe;
import com.ldtteam.aequivaleo.recipe.equivalency.VanillaCraftingEquivalencyRecipe;
import com.ldtteam.aequivaleo.tags.TagEquivalencyRegistry;
import com.ldtteam.aequivaleo.utils.RecipeUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tags.ITag;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class WorldBootstrapper
{

    private static final Logger LOGGER = LogManager.getLogger();

    private WorldBootstrapper()
    {
        throw new IllegalStateException("Tried to initialize: WorldBootstrapper but this is a Utility class.");
    }

    public static void onWorldReload(final ServerWorld world)
    {
        resetDataForWorld(world);

        doBootstrapTagInformation(world);
        doBootstrapDefaultCraftingRecipes(world);
        doBootstrapItemStackItemEquivalencies(world);

        doHandlePluginLoad(world);
    }

    private static void resetDataForWorld(final World world)
    {
        LockedCompoundInformationRegistry.getInstance(world.func_234923_W_()).reset();
        EquivalencyRecipeRegistry.getInstance(world.func_234923_W_()).reset();
    }

    private static void doBootstrapTagInformation(final World world)
    {
        for (ITag.INamedTag<?> tag : TagEquivalencyRegistry.getInstance().get())
        {
            doBootstrapSingleTagInformation(world, tag);
        }
    }

    private static <T> void doBootstrapSingleTagInformation(final World world, final ITag.INamedTag<T> tag) {
        final Collection<ICompoundContainer<?>> elementsOfTag = new ArrayList<>();
        for (T stack : tag.getAllElements())
        {
            ICompoundContainer<T> tiCompoundContainer = CompoundContainerFactoryManager.getInstance().wrapInContainer(stack, 1d);
            elementsOfTag.add(tiCompoundContainer);
        }

        for (ICompoundContainer<?> inputStack : elementsOfTag)
        {
            for (ICompoundContainer<?> outputStack : elementsOfTag)
            {
                if (!GameObjectEquivalencyHandlerRegistry.getInstance().areGameObjectsEquivalent(inputStack, outputStack))
                {
                    EquivalencyRecipeRegistry.getInstance(world.func_234923_W_())
                      .register(
                        new TagEquivalencyRecipe<>(
                          tag,
                          inputStack,
                          outputStack
                        ));
                }
            }
        }
    }

    private static void doBootstrapDefaultCraftingRecipes(@NotNull final World world)
    {
        final List<ICraftingRecipe> craftingRecipes = world.getRecipeManager().func_241447_a_(IRecipeType.CRAFTING);
        craftingRecipes
          .parallelStream()
          .forEach(recipe -> processCraftingRecipe(world, recipe));

        final List<FurnaceRecipe> smeltingRecipe = world.getRecipeManager().func_241447_a_(IRecipeType.SMELTING);
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

        final List<IEquivalencyRecipe> variants = RecipeUtils.getAllVariants(
          iRecipe,
          recipeFactory
        ).collect(Collectors.toList());

        variants.forEach(recipe -> {
            EquivalencyRecipeRegistry.getInstance(world.func_234923_W_()).register(recipe);
        });
    }

    private static void doBootstrapItemStackItemEquivalencies(
      @NotNull final ServerWorld world
    ) {
        StreamSupport.stream(ForgeRegistries.ITEMS.spliterator(), true).forEach(item -> {
            final NonNullList<ItemStack> group = NonNullList.create();
            if (item.getGroup() == null)
                return;

            item.fillItemGroup(Objects.requireNonNull(item.getGroup()), group);

            if (group.size() == 1)
            {
                final ICompoundContainer<?> itemContainer = CompoundContainerFactoryManager.getInstance().wrapInContainer(item, 1);
                final ICompoundContainer<?> itemStackContainer = CompoundContainerFactoryManager.getInstance().wrapInContainer(group.get(0), group.get(0).getCount());

                EquivalencyRecipeRegistry.getInstance(world.func_234923_W_())
                  .register(new InstancedEquivalency(
                    false, itemContainer, itemStackContainer
                  ))
                  .register(new InstancedEquivalency(
                    false, itemStackContainer, itemContainer
                  ));
            }
        });
    }

    private static void doHandlePluginLoad(
      @NotNull final ServerWorld world) {
        LOGGER.info(String.format("Invoking plugin callbacks: %s", world.func_234923_W_().func_240901_a_()));

        PluginManger.getInstance().getPlugins().parallelStream().forEach(plugin -> plugin.onReloadStartedFor(world));
    }
}
