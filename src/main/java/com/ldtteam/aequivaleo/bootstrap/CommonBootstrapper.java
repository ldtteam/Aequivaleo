package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.util.FluidStackUtils;
import com.ldtteam.aequivaleo.api.util.ItemStackUtils;
import com.ldtteam.aequivaleo.gameobject.equivalent.GameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
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
            (ICompoundContainer<ItemStack> left, ICompoundContainer<ItemStack> right) -> Optional.of(ItemStackUtils.compareItemStacksIgnoreStackSize(left.getContents(), right.getContents())));

        //Handle item equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
          .registerNewHandler(
            (container) -> container.getContents() instanceof Item,
            (ICompoundContainer<Item> left, ICompoundContainer<Item> right) -> Optional.of(Objects.requireNonNull(left.getContents().getRegistryName()).toString().equals(Objects.requireNonNull(right.getContents()
                                                                                                                                                                                                   .getRegistryName()).toString())));
        //Handle fluidstack equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
          .registerNewHandler(
            (container) -> container.getContents() instanceof FluidStack,
            (ICompoundContainer<FluidStack> left, ICompoundContainer<FluidStack> right) -> Optional.of(FluidStackUtils.compareFluidStacksIgnoreStackSize(left.getContents(), right.getContents())));

        //Handle fluid equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
          .registerNewHandler(
            (container) -> container.getContents() instanceof Fluid,
            (ICompoundContainer<Fluid> left, ICompoundContainer<Fluid> right) -> Optional.of(Objects.requireNonNull(left.getContents().getRegistryName()).toString().equals(Objects.requireNonNull(right.getContents()
                                                                                                                                                                                                   .getRegistryName()).toString())));
    }

    private static void doHandlePluginLoad() {
        LOGGER.info("Invoking plugin callbacks");
        PluginManger.getInstance().run(IAequivaleoPlugin::onCommonSetup);
    }
}
