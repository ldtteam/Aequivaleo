package com.ldtteam.aequivaleo.vanilla.config;

import com.google.common.collect.ImmutableList;
import com.ldtteam.aequivaleo.api.config.AbstractAequivaleoConfiguration;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.Tags;

import java.util.List;
import java.util.function.Predicate;

/**
 * Mod server configuration.
 * Loaded serverside, synced on connection.
 */
public class ServerConfiguration extends AbstractAequivaleoConfiguration
{
    public ForgeConfigSpec.ConfigValue<List<? extends String>> tagsToRegister;

    protected ServerConfiguration(final ForgeConfigSpec.Builder builder)
    {

        createCategory(builder, "recipes");
        createCategory(builder, "tags");
        tagsToRegister = defineList(
          builder,
          "recipes.tags.tagsToRegister",
          new ImmutableList.Builder<String>()
            .add(
              ItemTags.WOOL.getName().toString(),
              ItemTags.PLANKS.getName().toString(),
              ItemTags.STONE_BRICKS.getName().toString(),
              ItemTags.WOODEN_BUTTONS.getName().toString(),
              ItemTags.BUTTONS.getName().toString(),
              ItemTags.CARPETS.getName().toString(),
              ItemTags.WOODEN_DOORS.getName().toString(),
              ItemTags.WOODEN_STAIRS.getName().toString(),
              ItemTags.WOODEN_SLABS.getName().toString(),
              ItemTags.WOODEN_FENCES.getName().toString(),
              ItemTags.WOODEN_PRESSURE_PLATES.getName().toString(),
              ItemTags.WOODEN_TRAPDOORS.getName().toString(),
              ItemTags.SAPLINGS.getName().toString(),
              ItemTags.LOGS.getName().toString(),
              ItemTags.DARK_OAK_LOGS.getName().toString(),
              ItemTags.OAK_LOGS.getName().toString(),
              ItemTags.BIRCH_LOGS.getName().toString(),
              ItemTags.ACACIA_LOGS.getName().toString(),
              ItemTags.JUNGLE_LOGS.getName().toString(),
              ItemTags.SPRUCE_LOGS.getName().toString(),
              ItemTags.BANNERS.getName().toString(),
              ItemTags.SAND.getName().toString(),
              ItemTags.WALLS.getName().toString(),
              ItemTags.ANVIL.getName().toString(),
              ItemTags.LEAVES.getName().toString(),
              ItemTags.SMALL_FLOWERS.getName().toString(),
              ItemTags.BEDS.getName().toString(),
              ItemTags.FISHES.getName().toString(),
              ItemTags.SIGNS.getName().toString(),
              ItemTags.MUSIC_DISCS.getName().toString(),
              ItemTags.ARROWS.getName().toString(),

              Tags.Items.BONES.getName().toString(),
              Tags.Items.BOOKSHELVES.getName().toString(),
              Tags.Items.CHESTS_ENDER.getName().toString(),
              Tags.Items.CHESTS_TRAPPED.getName().toString(),
              Tags.Items.CHESTS_WOODEN.getName().toString(),
              Tags.Items.COBBLESTONE.getName().toString(),
              Tags.Items.CROPS_BEETROOT.getName().toString(),
              Tags.Items.CROPS_CARROT.getName().toString(),
              Tags.Items.CROPS_NETHER_WART.getName().toString(),
              Tags.Items.CROPS_POTATO.getName().toString(),
              Tags.Items.CROPS_WHEAT.getName().toString(),
              Tags.Items.DUSTS_PRISMARINE.getName().toString(),
              Tags.Items.DUSTS_REDSTONE.getName().toString(),
              Tags.Items.DUSTS_GLOWSTONE.getName().toString(),

              Tags.Items.DYES.getName().toString(),
              Tags.Items.DYES_BLACK.getName().toString(),
              Tags.Items.DYES_RED.getName().toString(),
              Tags.Items.DYES_GREEN.getName().toString(),
              Tags.Items.DYES_BROWN.getName().toString(),
              Tags.Items.DYES_BLUE.getName().toString(),
              Tags.Items.DYES_PURPLE.getName().toString(),
              Tags.Items.DYES_CYAN.getName().toString(),
              Tags.Items.DYES_LIGHT_GRAY.getName().toString(),
              Tags.Items.DYES_GRAY.getName().toString(),
              Tags.Items.DYES_PINK.getName().toString(),
              Tags.Items.DYES_LIME.getName().toString(),
              Tags.Items.DYES_YELLOW.getName().toString(),
              Tags.Items.DYES_LIGHT_BLUE.getName().toString(),
              Tags.Items.DYES_MAGENTA.getName().toString(),
              Tags.Items.DYES_ORANGE.getName().toString(),
              Tags.Items.DYES_WHITE.getName().toString(),

              Tags.Items.EGGS.getName().toString(),
              Tags.Items.ENDER_PEARLS.getName().toString(),
              Tags.Items.FEATHERS.getName().toString(),
              Tags.Items.FENCE_GATES.getName().toString(),
              Tags.Items.FENCE_GATES_WOODEN.getName().toString(),
              Tags.Items.FENCES.getName().toString(),
              Tags.Items.FENCES_NETHER_BRICK.getName().toString(),
              Tags.Items.FENCES_WOODEN.getName().toString(),
              Tags.Items.GEMS_DIAMOND.getName().toString(),
              Tags.Items.GEMS_EMERALD.getName().toString(),
              Tags.Items.GEMS_LAPIS.getName().toString(),
              Tags.Items.GEMS_PRISMARINE.getName().toString(),
              Tags.Items.GEMS_QUARTZ.getName().toString(),

              Tags.Items.GLASS.getName().toString(),
              Tags.Items.GLASS_BLACK.getName().toString(),
              Tags.Items.GLASS_BLUE.getName().toString(),
              Tags.Items.GLASS_BROWN.getName().toString(),
              Tags.Items.GLASS_COLORLESS.getName().toString(),
              Tags.Items.GLASS_CYAN.getName().toString(),
              Tags.Items.GLASS_GRAY.getName().toString(),
              Tags.Items.GLASS_GREEN.getName().toString(),
              Tags.Items.GLASS_LIGHT_BLUE.getName().toString(),
              Tags.Items.GLASS_LIGHT_GRAY.getName().toString(),
              Tags.Items.GLASS_LIME.getName().toString(),
              Tags.Items.GLASS_MAGENTA.getName().toString(),
              Tags.Items.GLASS_ORANGE.getName().toString(),
              Tags.Items.GLASS_PINK.getName().toString(),
              Tags.Items.GLASS_PURPLE.getName().toString(),
              Tags.Items.GLASS_RED.getName().toString(),
              Tags.Items.GLASS_WHITE.getName().toString(),
              Tags.Items.GLASS_YELLOW.getName().toString(),

              Tags.Items.GLASS_PANES.getName().toString(),
              Tags.Items.GLASS_PANES_BLACK.getName().toString(),
              Tags.Items.GLASS_PANES_BLUE.getName().toString(),
              Tags.Items.GLASS_PANES_BROWN.getName().toString(),
              Tags.Items.GLASS_PANES_COLORLESS.getName().toString(),
              Tags.Items.GLASS_PANES_CYAN.getName().toString(),
              Tags.Items.GLASS_PANES_GRAY.getName().toString(),
              Tags.Items.GLASS_PANES_GREEN.getName().toString(),
              Tags.Items.GLASS_PANES_LIGHT_BLUE.getName().toString(),
              Tags.Items.GLASS_PANES_LIGHT_GRAY.getName().toString(),
              Tags.Items.GLASS_PANES_LIME.getName().toString(),
              Tags.Items.GLASS_PANES_MAGENTA.getName().toString(),
              Tags.Items.GLASS_PANES_ORANGE.getName().toString(),
              Tags.Items.GLASS_PANES_PINK.getName().toString(),
              Tags.Items.GLASS_PANES_PURPLE.getName().toString(),
              Tags.Items.GLASS_PANES_RED.getName().toString(),
              Tags.Items.GLASS_PANES_WHITE.getName().toString(),
              Tags.Items.GLASS_PANES_YELLOW.getName().toString(),

              Tags.Items.GRAVEL.getName().toString(),
              Tags.Items.GUNPOWDER.getName().toString(),
              Tags.Items.HEADS.getName().toString(),
              Tags.Items.INGOTS_BRICK.getName().toString(),
              Tags.Items.INGOTS_GOLD.getName().toString(),
              Tags.Items.INGOTS_IRON.getName().toString(),
              Tags.Items.INGOTS_NETHER_BRICK.getName().toString(),
              Tags.Items.LEATHER.getName().toString(),
              Tags.Items.MUSHROOMS.getName().toString(),
              Tags.Items.NETHER_STARS.getName().toString(),
              Tags.Items.NETHERRACK.getName().toString(),
              Tags.Items.NUGGETS_GOLD.getName().toString(),
              Tags.Items.NUGGETS_IRON.getName().toString(),
              Tags.Items.OBSIDIAN.getName().toString(),
              Tags.Items.ORES_COAL.getName().toString(),
              Tags.Items.ORES_DIAMOND.getName().toString(),
              Tags.Items.ORES_EMERALD.getName().toString(),
              Tags.Items.ORES_GOLD.getName().toString(),
              Tags.Items.ORES_IRON.getName().toString(),
              Tags.Items.ORES_LAPIS.getName().toString(),
              Tags.Items.ORES_QUARTZ.getName().toString(),
              Tags.Items.ORES_REDSTONE.getName().toString(),
              Tags.Items.RODS_BLAZE.getName().toString(),
              Tags.Items.RODS_WOODEN.getName().toString(),

              Tags.Items.SAND.getName().toString(),
              Tags.Items.SAND_COLORLESS.getName().toString(),
              Tags.Items.SAND_RED.getName().toString(),

              Tags.Items.SANDSTONE.getName().toString(),
              Tags.Items.SEEDS.getName().toString(),
              Tags.Items.SEEDS_BEETROOT.getName().toString(),
              Tags.Items.SEEDS_MELON.getName().toString(),
              Tags.Items.SEEDS_PUMPKIN.getName().toString(),
              Tags.Items.SEEDS_WHEAT.getName().toString(),
              Tags.Items.SLIMEBALLS.getName().toString(),
              Tags.Items.STAINED_GLASS.getName().toString(),
              Tags.Items.STAINED_GLASS_PANES.getName().toString(),
              Tags.Items.STONE.getName().toString(),
              Tags.Items.STORAGE_BLOCKS_COAL.getName().toString(),
              Tags.Items.STORAGE_BLOCKS_DIAMOND.getName().toString(),
              Tags.Items.STORAGE_BLOCKS_EMERALD.getName().toString(),
              Tags.Items.STORAGE_BLOCKS_GOLD.getName().toString(),
              Tags.Items.STORAGE_BLOCKS_IRON.getName().toString(),
              Tags.Items.STORAGE_BLOCKS_LAPIS.getName().toString(),
              Tags.Items.STORAGE_BLOCKS_QUARTZ.getName().toString(),
              Tags.Items.STORAGE_BLOCKS_REDSTONE.getName().toString(),
              Tags.Items.STRING.getName().toString()
            )
            .build(),
          o -> {
              final String resLoc = o.toString();
              final String[] parts = resLoc.split(":");
              if (parts.length >= 3 || parts.length == 0)
                  return false;

              if (parts.length == 1) {
                  return ResourceLocation.isPathValid(parts[0]);
              }

              return ResourceLocation.isResouceNameValid(parts[0]) && ResourceLocation.isPathValid(parts[1]);
          }
        );
        finishCategory(builder);
        finishCategory(builder);
    }
}