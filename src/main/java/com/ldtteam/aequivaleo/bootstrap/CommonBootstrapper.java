package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.util.FluidStackUtils;
import com.ldtteam.aequivaleo.api.util.ItemStackUtils;
import com.ldtteam.aequivaleo.gameobject.equivalent.GameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.plugin.PluginManger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
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
                        (container) -> container.getContents() instanceof ItemStack,
                        (ICompoundContainer<ItemStack> left, ICompoundContainer<ItemStack> right) -> Optional.of(ItemStackUtils.compareItemStacksIgnoreStackSize(left.getContents(), right.getContents())));

        //Handle item equivalency:
        final Function<Item, ResourceLocation> itemNameGetter = (item) -> Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item));
        GameObjectEquivalencyHandlerRegistry.getInstance()
                .registerNewHandler(
                        (container) -> container.getContents() instanceof Item,
                        (ICompoundContainer<Item> left, ICompoundContainer<Item> right) -> Optional.of(itemNameGetter.apply(left.getContents()).toString().equals(itemNameGetter.apply(right.getContents()).toString())));

        //Handle fluidstack equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
                .registerNewHandler(
                        (container) -> container.getContents() instanceof FluidStack,
                        (ICompoundContainer<FluidStack> left, ICompoundContainer<FluidStack> right) -> Optional.of(FluidStackUtils.compareFluidStacksIgnoreStackSize(left.getContents(), right.getContents())));

        //Handle fluid equivalency:
        final Function<Fluid, ResourceLocation> fluidNameGetter = (item) -> Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(item));
        GameObjectEquivalencyHandlerRegistry.getInstance()
                .registerNewHandler(
                        (container) -> container.getContents() instanceof Fluid,
                        (ICompoundContainer<Fluid> left, ICompoundContainer<Fluid> right) -> Optional.of(fluidNameGetter.apply(left.getContents()).toString().equals(fluidNameGetter.apply(right.getContents()).toString())));

        //Handle enchantments equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
                .registerNewHandler(
                        (container) -> container.getContents() instanceof Enchantment,
                        (ICompoundContainer<Enchantment> left, ICompoundContainer<Enchantment> right) -> Optional.of(Objects.requireNonNull(ForgeRegistries.ENCHANTMENTS.getKey(left.getContents())).equals(ForgeRegistries.ENCHANTMENTS.getKey(right.getContents()))));

        //Handle entity types equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
                .registerNewHandler((container) -> container.getContents() instanceof EntityType,
                        (ICompoundContainer<EntityType<?>> left, ICompoundContainer<EntityType<?>> right) -> Optional.of(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(left.getContents())).equals(ForgeRegistries.ENTITY_TYPES.getKey(right.getContents()))));

        //Handle blocks equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
                .registerNewHandler(
                        (container) -> container.getContents() instanceof Block,
                        (ICompoundContainer<Block> left, ICompoundContainer<Block> right) -> Optional.of(ForgeRegistries.BLOCKS.getKey(left.getContents()).equals(ForgeRegistries.BLOCKS.getKey(right.getContents()))));
    }

    private static void doHandlePluginLoad() {
        LOGGER.info("Invoking plugin callbacks");
        PluginManger.getInstance().run(IAequivaleoPlugin::onCommonSetup);
    }
}
