package com.ldtteam.aequivaleo.bootstrap;

import com.google.common.collect.Sets;
import com.ldtteam.aequivaleo.Aequivaleo;
import com.ldtteam.aequivaleo.analyzer.EquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.event.OnWorldDataReloadedEvent;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.util.ItemStackUtils;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryRegistry;
import com.ldtteam.aequivaleo.compound.information.contribution.ContributionInformationProviderRegistry;
import com.ldtteam.aequivaleo.compound.information.locked.LockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.compound.information.validity.ValidCompoundTypeInformationProviderRegistry;
import com.ldtteam.aequivaleo.gameobject.equivalent.GameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.recipe.equivalency.DropsEquivalency;
import com.ldtteam.aequivaleo.recipe.equivalency.SimpleEquivalancyRecipe;
import com.ldtteam.aequivaleo.recipe.equivalency.SmeltingEquivalancyRecipe;
import com.ldtteam.aequivaleo.recipe.equivalency.TagEquivalencyRecipe;
import com.ldtteam.aequivaleo.tags.TagEquivalencyRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.tags.ITag;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

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
        doBootstrapBlockDropEquivalencies(world);

        doFireDataLoadEvent(world);
    }

    private static void resetDataForWorld(final World world)
    {
        LockedCompoundInformationRegistry.getInstance(world.func_234923_W_()).reset();
        ValidCompoundTypeInformationProviderRegistry.getInstance(world.func_234923_W_()).reset();
        EquivalencyRecipeRegistry.getInstance(world.func_234923_W_()).reset();
        ContributionInformationProviderRegistry.getInstance(world.func_234923_W_()).reset();
    }

    private static void doBootstrapTagInformation(final World world)
    {
        TagEquivalencyRegistry.getInstance().get().forEach((ITag.INamedTag<?> tag) -> doBootstrapSingleTagInformation(world, tag));
    }

    private static <T> void doBootstrapSingleTagInformation(final World world, final ITag.INamedTag<T> tag) {
        final Collection<ICompoundContainer<?>> elementsOfTag = tag.getAllElements()
                                                                  .stream()
                                                                  .map(stack -> CompoundContainerFactoryRegistry.getInstance().wrapInContainer(stack, 1d))
                                                                  .collect(Collectors.toList());

        elementsOfTag.forEach(inputStack -> elementsOfTag
          .stream()
          .filter(outputStack -> !GameObjectEquivalencyHandlerRegistry.getInstance().areGameObjectsEquivalent(inputStack, outputStack))
          .forEach(outputStack -> EquivalencyRecipeRegistry.getInstance(world.func_234923_W_())
            .register(
              new TagEquivalencyRecipe<>(
                tag,
                Sets.newHashSet(inputStack),
                Sets.newHashSet(outputStack)
              ))));
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
        processIRecipe(world, iRecipe, SmeltingEquivalancyRecipe::new);
    }

    private static void processCraftingRecipe(@NotNull final World world, IRecipe<?> iRecipe)
    {
        processIRecipe(world, iRecipe, SimpleEquivalancyRecipe::new);
    }

    private static void processIRecipe(
      @NotNull final World world,
      IRecipe<?> iRecipe,
      BiFunction<Set<ICompoundContainer<?>>, Set<ICompoundContainer<?>>, IEquivalencyRecipe> recipeProducer
    )
    {
        if (iRecipe.getRecipeOutput().isEmpty())
        {
            return;
        }

        final NonNullList<Ingredient> ingredients = iRecipe.getIngredients();
        final List<Ingredient> withOutEmptyIngredients = ingredients.stream()
                                                           .filter(ingredient -> !ingredient.test(ItemStack.EMPTY) && ingredient.getMatchingStacks().length > 0
                                                                                   && !ItemStackUtils.isEmpty(ingredient.getMatchingStacks()[0]))
                                                           .collect(Collectors.toList());

        final List<ItemStack> inputStacks = withOutEmptyIngredients.stream()
                                              .map(ingredient -> ingredient.getMatchingStacks()[0])
                                              .filter(itemStack -> !itemStack.isEmpty())
                                              .collect(Collectors.toList());

        final Set<ICompoundContainer<?>> wrappedInput = inputStacks
                                                                 .stream()
                                                                 .map(stack -> CompoundContainerFactoryRegistry.getInstance()
                                                                                 .wrapInContainer(stack, stack.getCount()))
                                                                 .collect(Collectors.toMap(wrapper -> wrapper, ICompoundContainer::getContentsCount, Double::sum))
                                                                 .entrySet()
                                                                 .stream()
                                                                 .map(iCompoundContainerWrapperDoubleEntry -> CompoundContainerFactoryRegistry.getInstance()
                                                                                                                .wrapInContainer(iCompoundContainerWrapperDoubleEntry.getKey()
                                                                                                                                   .getContents(),
                                                                                                                  iCompoundContainerWrapperDoubleEntry.getValue()))
                                                                 .collect(Collectors.toSet());

        final ICompoundContainer<?> outputWrapped = CompoundContainerFactoryRegistry.getInstance().wrapInContainer(iRecipe.getRecipeOutput(),
          iRecipe.getRecipeOutput().getCount());


        EquivalencyRecipeRegistry.getInstance(world.func_234923_W_()).register(recipeProducer.apply(wrappedInput, Sets.newHashSet(outputWrapped)));
    }

    private static void doBootstrapBlockDropEquivalencies(
      @NotNull final ServerWorld world
    )
    {
        ForgeRegistries.BLOCKS.getValues().forEach(block -> {
            final ICompoundContainer<?> compoundContainer = CompoundContainerFactoryRegistry.getInstance().wrapInContainer(block, 1);
            try {
                final DropsEquivalency inputRecipe = new DropsEquivalency(compoundContainer, true, world);
                final DropsEquivalency outputRecipe = new DropsEquivalency(compoundContainer, false, world);
                EquivalencyRecipeRegistry.getInstance(world.func_234923_W_()).register(inputRecipe).register(outputRecipe);
            } catch (Exception ex) {
                if (Aequivaleo.getInstance().getConfiguration().getCommon().writeExceptionOnBlockDropFailure.get()) {
                    LOGGER.warn(String.format("Could not determine blockdrops for: %s it was not possible to calculate the drops. Potentially a TileEntity or proper world is required.",
                      block.getRegistryName()), ex);
                }
                else
                {
                    LOGGER.warn(String.format("Could not determine blockdrops for: %s it was not possible to calculate the drops. Potentially a TileEntity or proper world is required. Turn the config value for block drop exceptions on to see more details.",
                      block.getRegistryName()));
                }
            }
        });
    }

    private static void doFireDataLoadEvent(
      @NotNull final ServerWorld world) {
        LOGGER.info(String.format("Firing data loaded event for world: %s", world.func_234923_W_().func_240901_a_()));
        MinecraftForge.EVENT_BUS.post(new OnWorldDataReloadedEvent(world));
    }
}
