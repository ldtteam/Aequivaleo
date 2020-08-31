package com.ldtteam.aequivaleo.bootstrap;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.event.OnGlobalDataLoadedEvent;
import com.ldtteam.aequivaleo.api.util.*;
import com.ldtteam.aequivaleo.compound.container.registry.CompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.gameobject.equivalent.GameObjectEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.gameobject.loottable.LootTableAnalyserRegistry;
import com.ldtteam.aequivaleo.heat.Heat;
import com.ldtteam.aequivaleo.tags.TagEquivalencyRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.state.Property;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class CommonBootstrapper
{
    private static final Logger LOGGER = LogManager.getLogger();

    static void doBootstrap()
    {
        LOGGER.info("Bootstrapping aequivaleo");

        doBootstrapEquivalencyHandler();
        doBootstrapTagNames();
        doBootstrapLootTableAnalyzers();

        doFireDataLoadedEvent();

        doPrepopulateTypeCache();
    }

    private static void doBootstrapEquivalencyHandler()
    {
        LOGGER.info("Registering equivalency handlers.");
        //Handle itemstack equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
          .registerNewHandler(
            ItemStack.class,
            (left, right) -> Optional.of(ItemStackUtils.compareItemStacksIgnoreStackSize(left.getContents(), right.getContents()))
          );

        //Handle item equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
          .registerNewHandler(
            Item.class,
            (left, right) -> Optional.of(Objects.requireNonNull(left.getContents().getRegistryName()).toString().equals(Objects.requireNonNull(right.getContents()
                                                                                                                                                 .getRegistryName()).toString()))
          );

        //Handle block equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
          .registerNewHandler(
            Block.class,
            (left, right) -> Optional.of(Objects.requireNonNull(left.getContents().getRegistryName()).toString().equals(Objects.requireNonNull(right.getContents()
                                                                                                                                                 .getRegistryName()).toString()))
          );

        //Handle blockstate equivalency:
        GameObjectEquivalencyHandlerRegistry.getInstance()
          .registerNewHandler(
            BlockState.class,
            (left, right) -> {
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


    private static void doBootstrapTagNames()
    {
        LOGGER.info("Registering tags.");
        TagEquivalencyRegistry.getInstance()
          .addTag(ItemTags.WOOL.getName())
          .addTag(ItemTags.PLANKS.getName())
          .addTag(ItemTags.STONE_BRICKS.getName())
          .addTag(ItemTags.WOODEN_BUTTONS.getName())
          .addTag(ItemTags.BUTTONS.getName())
          .addTag(ItemTags.CARPETS.getName())
          .addTag(ItemTags.WOODEN_DOORS.getName())
          .addTag(ItemTags.WOODEN_STAIRS.getName())
          .addTag(ItemTags.WOODEN_SLABS.getName())
          .addTag(ItemTags.WOODEN_FENCES.getName())
          .addTag(ItemTags.WOODEN_PRESSURE_PLATES.getName())
          .addTag(ItemTags.WOODEN_TRAPDOORS.getName())
          .addTag(ItemTags.SAPLINGS.getName())
          .addTag(ItemTags.LOGS.getName())
          .addTag(ItemTags.DARK_OAK_LOGS.getName())
          .addTag(ItemTags.OAK_LOGS.getName())
          .addTag(ItemTags.BIRCH_LOGS.getName())
          .addTag(ItemTags.ACACIA_LOGS.getName())
          .addTag(ItemTags.JUNGLE_LOGS.getName())
          .addTag(ItemTags.SPRUCE_LOGS.getName())
          .addTag(ItemTags.BANNERS.getName())
          .addTag(ItemTags.SAND.getName())
          .addTag(ItemTags.WALLS.getName())
          .addTag(ItemTags.ANVIL.getName())
          .addTag(ItemTags.LEAVES.getName())
          .addTag(ItemTags.SMALL_FLOWERS.getName())
          .addTag(ItemTags.BEDS.getName())
          .addTag(ItemTags.FISHES.getName())
          .addTag(ItemTags.SIGNS.getName())
          .addTag(ItemTags.MUSIC_DISCS.getName())
          .addTag(ItemTags.ARROWS.getName())

          .addTag(Tags.Items.ARROWS.getName())
          .addTag(Tags.Items.BONES.getName())
          .addTag(Tags.Items.BOOKSHELVES.getName())
          .addTag(Tags.Items.CHESTS_ENDER.getName())
          .addTag(Tags.Items.CHESTS_TRAPPED.getName())
          .addTag(Tags.Items.CHESTS_WOODEN.getName())
          .addTag(Tags.Items.COBBLESTONE.getName())
          .addTag(Tags.Items.CROPS_BEETROOT.getName())
          .addTag(Tags.Items.CROPS_CARROT.getName())
          .addTag(Tags.Items.CROPS_NETHER_WART.getName())
          .addTag(Tags.Items.CROPS_POTATO.getName())
          .addTag(Tags.Items.CROPS_WHEAT.getName())
          .addTag(Tags.Items.DUSTS_PRISMARINE.getName())
          .addTag(Tags.Items.DUSTS_REDSTONE.getName())
          .addTag(Tags.Items.DUSTS_GLOWSTONE.getName())

          .addTag(Tags.Items.DYES.getName())
          .addTag(Tags.Items.DYES_BLACK.getName())
          .addTag(Tags.Items.DYES_RED.getName())
          .addTag(Tags.Items.DYES_GREEN.getName())
          .addTag(Tags.Items.DYES_BROWN.getName())
          .addTag(Tags.Items.DYES_BLUE.getName())
          .addTag(Tags.Items.DYES_PURPLE.getName())
          .addTag(Tags.Items.DYES_CYAN.getName())
          .addTag(Tags.Items.DYES_LIGHT_GRAY.getName())
          .addTag(Tags.Items.DYES_GRAY.getName())
          .addTag(Tags.Items.DYES_PINK.getName())
          .addTag(Tags.Items.DYES_LIME.getName())
          .addTag(Tags.Items.DYES_YELLOW.getName())
          .addTag(Tags.Items.DYES_LIGHT_BLUE.getName())
          .addTag(Tags.Items.DYES_MAGENTA.getName())
          .addTag(Tags.Items.DYES_ORANGE.getName())
          .addTag(Tags.Items.DYES_WHITE.getName())

          .addTag(Tags.Items.EGGS.getName())
          .addTag(Tags.Items.ENDER_PEARLS.getName())
          .addTag(Tags.Items.FEATHERS.getName())
          .addTag(Tags.Items.FENCE_GATES.getName())
          .addTag(Tags.Items.FENCE_GATES_WOODEN.getName())
          .addTag(Tags.Items.FENCES.getName())
          .addTag(Tags.Items.FENCES_NETHER_BRICK.getName())
          .addTag(Tags.Items.FENCES_WOODEN.getName())
          .addTag(Tags.Items.GEMS_DIAMOND.getName())
          .addTag(Tags.Items.GEMS_EMERALD.getName())
          .addTag(Tags.Items.GEMS_LAPIS.getName())
          .addTag(Tags.Items.GEMS_PRISMARINE.getName())
          .addTag(Tags.Items.GEMS_QUARTZ.getName())

          .addTag(Tags.Items.GLASS.getName())
          .addTag(Tags.Items.GLASS_BLACK.getName())
          .addTag(Tags.Items.GLASS_BLUE.getName())
          .addTag(Tags.Items.GLASS_BROWN.getName())
          .addTag(Tags.Items.GLASS_COLORLESS.getName())
          .addTag(Tags.Items.GLASS_CYAN.getName())
          .addTag(Tags.Items.GLASS_GRAY.getName())
          .addTag(Tags.Items.GLASS_GREEN.getName())
          .addTag(Tags.Items.GLASS_LIGHT_BLUE.getName())
          .addTag(Tags.Items.GLASS_LIGHT_GRAY.getName())
          .addTag(Tags.Items.GLASS_LIME.getName())
          .addTag(Tags.Items.GLASS_MAGENTA.getName())
          .addTag(Tags.Items.GLASS_ORANGE.getName())
          .addTag(Tags.Items.GLASS_PINK.getName())
          .addTag(Tags.Items.GLASS_PURPLE.getName())
          .addTag(Tags.Items.GLASS_RED.getName())
          .addTag(Tags.Items.GLASS_WHITE.getName())
          .addTag(Tags.Items.GLASS_YELLOW.getName())

          .addTag(Tags.Items.GLASS_PANES.getName())
          .addTag(Tags.Items.GLASS_PANES_BLACK.getName())
          .addTag(Tags.Items.GLASS_PANES_BLUE.getName())
          .addTag(Tags.Items.GLASS_PANES_BROWN.getName())
          .addTag(Tags.Items.GLASS_PANES_COLORLESS.getName())
          .addTag(Tags.Items.GLASS_PANES_CYAN.getName())
          .addTag(Tags.Items.GLASS_PANES_GRAY.getName())
          .addTag(Tags.Items.GLASS_PANES_GREEN.getName())
          .addTag(Tags.Items.GLASS_PANES_LIGHT_BLUE.getName())
          .addTag(Tags.Items.GLASS_PANES_LIGHT_GRAY.getName())
          .addTag(Tags.Items.GLASS_PANES_LIME.getName())
          .addTag(Tags.Items.GLASS_PANES_MAGENTA.getName())
          .addTag(Tags.Items.GLASS_PANES_ORANGE.getName())
          .addTag(Tags.Items.GLASS_PANES_PINK.getName())
          .addTag(Tags.Items.GLASS_PANES_PURPLE.getName())
          .addTag(Tags.Items.GLASS_PANES_RED.getName())
          .addTag(Tags.Items.GLASS_PANES_WHITE.getName())
          .addTag(Tags.Items.GLASS_PANES_YELLOW.getName())

          .addTag(Tags.Items.GRAVEL.getName())
          .addTag(Tags.Items.GUNPOWDER.getName())
          .addTag(Tags.Items.HEADS.getName())
          .addTag(Tags.Items.INGOTS_BRICK.getName())
          .addTag(Tags.Items.INGOTS_GOLD.getName())
          .addTag(Tags.Items.INGOTS_IRON.getName())
          .addTag(Tags.Items.INGOTS_NETHER_BRICK.getName())
          .addTag(Tags.Items.LEATHER.getName())
          .addTag(Tags.Items.MUSHROOMS.getName())
          .addTag(Tags.Items.MUSIC_DISCS.getName())
          .addTag(Tags.Items.NETHER_STARS.getName())
          .addTag(Tags.Items.NETHERRACK.getName())
          .addTag(Tags.Items.NUGGETS_GOLD.getName())
          .addTag(Tags.Items.NUGGETS_IRON.getName())
          .addTag(Tags.Items.OBSIDIAN.getName())
          .addTag(Tags.Items.ORES_COAL.getName())
          .addTag(Tags.Items.ORES_DIAMOND.getName())
          .addTag(Tags.Items.ORES_EMERALD.getName())
          .addTag(Tags.Items.ORES_GOLD.getName())
          .addTag(Tags.Items.ORES_IRON.getName())
          .addTag(Tags.Items.ORES_LAPIS.getName())
          .addTag(Tags.Items.ORES_QUARTZ.getName())
          .addTag(Tags.Items.ORES_REDSTONE.getName())
          .addTag(Tags.Items.RODS_BLAZE.getName())
          .addTag(Tags.Items.RODS_WOODEN.getName())

          .addTag(Tags.Items.SAND.getName())
          .addTag(Tags.Items.SAND_COLORLESS.getName())
          .addTag(Tags.Items.SAND_RED.getName())

          .addTag(Tags.Items.SANDSTONE.getName())
          .addTag(Tags.Items.SEEDS.getName())
          .addTag(Tags.Items.SEEDS_BEETROOT.getName())
          .addTag(Tags.Items.SEEDS_MELON.getName())
          .addTag(Tags.Items.SEEDS_PUMPKIN.getName())
          .addTag(Tags.Items.SEEDS_WHEAT.getName())
          .addTag(Tags.Items.SLIMEBALLS.getName())
          .addTag(Tags.Items.STAINED_GLASS.getName())
          .addTag(Tags.Items.STAINED_GLASS_PANES.getName())
          .addTag(Tags.Items.STONE.getName())
          .addTag(Tags.Items.STORAGE_BLOCKS_COAL.getName())
          .addTag(Tags.Items.STORAGE_BLOCKS_DIAMOND.getName())
          .addTag(Tags.Items.STORAGE_BLOCKS_EMERALD.getName())
          .addTag(Tags.Items.STORAGE_BLOCKS_GOLD.getName())
          .addTag(Tags.Items.STORAGE_BLOCKS_IRON.getName())
          .addTag(Tags.Items.STORAGE_BLOCKS_LAPIS.getName())
          .addTag(Tags.Items.STORAGE_BLOCKS_QUARTZ.getName())
          .addTag(Tags.Items.STORAGE_BLOCKS_REDSTONE.getName())
          .addTag(Tags.Items.STRING.getName());
    }

    private static void doBootstrapLootTableAnalyzers()
    {
        LOGGER.info("Registering loot table analyzers");
        LootTableAnalyserRegistry.getInstance().register(
          Block.class,
          ( blockState, world) -> {
              final ItemStack harvester = BlockUtils.getHarvestingToolForBlock(blockState);
              LootContext.Builder builder = (new LootContext.Builder(world)).withParameter(LootParameters.POSITION, BlockPos.ZERO)
                                                                .withParameter(LootParameters.TOOL, harvester)
                                                                .withParameter(LootParameters.BLOCK_STATE, blockState);

              final TileEntity entity = blockState.createTileEntity(new SingleBlockBlockReader(blockState));
              if (entity != null) {
                  builder = builder.withParameter(LootParameters.BLOCK_ENTITY, entity);
              }
              final LootTable table = ServerLifecycleHooks.getCurrentServer().getLootTableManager().getLootTableFromLocation(blockState.getBlock().getLootTable());
              return table.generate(builder.build(LootParameterSets.BLOCK))
                       .stream()
                       .map(itemStack -> CompoundContainerFactoryManager.getInstance().wrapInContainer(itemStack, 1))
                       .collect(
                         Collectors.toSet());
          }
        );
    }

    private static void doFireDataLoadedEvent() {
        LOGGER.info("Firing global data loaded event.");
        MinecraftForge.EVENT_BUS.post(new OnGlobalDataLoadedEvent());
    }

    private static void doPrepopulateTypeCache() {
        LOGGER.info("Pre-populating the super types of common classes.");
        TypeUtils.getAllSuperTypesExcludingObject(ItemStack.class);
        TypeUtils.getAllSuperTypesExcludingObject(Block.class);
        TypeUtils.getAllSuperTypesExcludingObject(BlockState.class);
        TypeUtils.getAllSuperTypesExcludingObject(Heat.class);
    }
}
