package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.util.FluidStackUtils;
import com.ldtteam.aequivaleo.api.util.ItemStackUtils;
import com.ldtteam.aequivaleo.gameobject.equivalent.GameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class CommonBootstrapper {
    private static final Logger LOGGER = LogManager.getLogger();

    static void doBootstrap() {
        LOGGER.info("Bootstrapping Aequivaleo");

        doBootstrapEquivalencyHandler();

        doHandlePluginLoad();
    }

    private static void doBootstrapEquivalencyHandler() {
        LOGGER.info("Registering equivalency handlers.");
        //Handle itemstack equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
                .registerNewHandler(
                        (container) -> container.contents() instanceof ItemStack,
                        (ICompoundContainer<ItemStack> left, ICompoundContainer<ItemStack> right) -> Optional.of(ItemStackUtils.compareItemStacksIgnoreStackSize(left.contents(), right.contents())));

        //Handle item equivalency:
        final Function<Item, ResourceLocation> itemNameGetter = (item) -> Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item));
        GameObjectEquivalencyHandlerRegistry.getInstance()
                .registerNewHandler(
                        (container) -> container.contents() instanceof Item,
                        (ICompoundContainer<Item> left, ICompoundContainer<Item> right) -> Optional.of(itemNameGetter.apply(left.contents()).toString().equals(itemNameGetter.apply(right.contents()).toString())));

        //Handle fluidstack equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
                .registerNewHandler(
                        (container) -> container.contents() instanceof FluidStack,
                        (ICompoundContainer<FluidStack> left, ICompoundContainer<FluidStack> right) -> Optional.of(FluidStackUtils.compareFluidStacksIgnoreStackSize(left.contents(), right.contents())));

        //Handle fluid equivalency:
        final Function<Fluid, ResourceLocation> fluidNameGetter = (item) -> Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(item));
        GameObjectEquivalencyHandlerRegistry.getInstance()
                .registerNewHandler(
                        (container) -> container.contents() instanceof Fluid,
                        (ICompoundContainer<Fluid> left, ICompoundContainer<Fluid> right) -> Optional.of(fluidNameGetter.apply(left.contents()).toString().equals(fluidNameGetter.apply(right.contents()).toString())));

        //Handle enchantments equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
                .registerNewHandler(
                        (container) -> container.contents() instanceof Enchantment,
                        (ICompoundContainer<Enchantment> left, ICompoundContainer<Enchantment> right) -> Optional.of(Objects.requireNonNull(BuiltInRegistries.ENCHANTMENT.getKey(left.contents())).equals(BuiltInRegistries.ENCHANTMENT.getKey(right.contents()))));

        //Handle entity types equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
                .registerNewHandler((container) -> container.contents() instanceof EntityType,
                        (ICompoundContainer<EntityType<?>> left, ICompoundContainer<EntityType<?>> right) -> Optional.of(Objects.requireNonNull(BuiltInRegistries.ENTITY_TYPE.getKey(left.contents())).equals(BuiltInRegistries.ENTITY_TYPE.getKey(right.contents()))));

        //Handle blocks equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
                .registerNewHandler(
                        (container) -> container.contents() instanceof Block,
                        (ICompoundContainer<Block> left, ICompoundContainer<Block> right) -> Optional.of(BuiltInRegistries.BLOCK.getKey(left.contents()).equals(BuiltInRegistries.BLOCK.getKey(right.contents()))));
    }

    private static void doHandlePluginLoad() {
        LOGGER.info("Invoking plugin callbacks");
        PluginManger.getInstance().run(IAequivaleoPlugin::onCommonSetup);
    }
}
