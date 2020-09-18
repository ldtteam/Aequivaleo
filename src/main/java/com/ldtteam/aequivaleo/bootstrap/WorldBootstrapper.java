package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.analyzer.EquivalencyRecipeRegistry;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.compound.information.locked.LockedCompoundInformationRegistry;
import com.ldtteam.aequivaleo.gameobject.equivalent.GameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import com.ldtteam.aequivaleo.recipe.equivalency.InstancedEquivalency;
import com.ldtteam.aequivaleo.recipe.equivalency.TagEquivalencyRecipe;
import com.ldtteam.aequivaleo.vanilla.tags.TagEquivalencyRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
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
        PluginManger.getInstance().run(plugin -> plugin.onReloadStartedFor(world));
    }
}
