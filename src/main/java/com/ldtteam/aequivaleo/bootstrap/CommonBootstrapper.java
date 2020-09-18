package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.util.ItemStackUtils;
import com.ldtteam.aequivaleo.gameobject.equivalent.GameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;

public final class CommonBootstrapper
{
    private static final Logger LOGGER = LogManager.getLogger();

    static void doBootstrap()
    {
        LOGGER.info("Bootstrapping aequivaleo");

        doBootstrapEquivalencyHandler();

        doHandlePluginLoad();
    }

    private static void doBootstrapEquivalencyHandler()
    {
        LOGGER.info("Registering equivalency handlers.");
        //Handle itemstack equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
          .registerNewHandler(
            (container) -> container.getContents() instanceof ItemStack,
            (ICompoundContainer<ItemStack> left, ICompoundContainer<ItemStack> right) -> Optional.of(ItemStackUtils.compareItemStacksIgnoreStackSize(left.getContents(), right.getContents()))
          );

        //Handle item equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
          .registerNewHandler(
            (container) -> container.getContents() instanceof Item,
            (ICompoundContainer<Item> left, ICompoundContainer<Item> right) -> Optional.of(Objects.requireNonNull(left.getContents().getRegistryName()).toString().equals(Objects.requireNonNull(right.getContents()
                                                                                                                                                 .getRegistryName()).toString()))
          );

        //Handle block equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
          .registerNewHandler(
            (container) -> container.getContents() instanceof Block,
            (ICompoundContainer<Block> left, ICompoundContainer<Block> right) -> Optional.of(Objects.requireNonNull(left.getContents().getRegistryName()).toString().equals(Objects.requireNonNull(right.getContents()
                                                                                                                                                 .getRegistryName()).toString()))
          );

        //Handle blockstate equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
          .registerNewHandler(
            (container) -> container.getContents() instanceof BlockState,
            (ICompoundContainer<BlockState> left, ICompoundContainer<BlockState> right) -> {
                if (left.getContents().getBlock() != right.getContents().getBlock())
                    return Optional.of(false);

                for (final Property<?> property : left.getContents().getProperties())
                {
                    if (!right.getContents().getProperties().contains(property))
                        return Optional.of(false);

                    final Object leftValue = left.getContents().get(property);
                    final Object rightValue = right.getContents().get(property);

                    if (leftValue != rightValue)
                        return Optional.of(false);
                }

                return Optional.of(true);
            });
    }

    private static void doHandlePluginLoad() {
        LOGGER.info("Invoking plugin callbacks");
        PluginManger.getInstance().run(IAequivaleoPlugin::onCommonSetup);
    }
}
