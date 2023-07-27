package com.ldtteam.aequivaleo.vanilla.config;

import com.google.common.collect.ImmutableList;
import com.ldtteam.aequivaleo.api.config.AbstractAequivaleoConfiguration;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Mod server configuration.
 * Loaded serverside,
			  synced on connection.
 */
public class CommonConfiguration extends AbstractAequivaleoConfiguration
{
    public ForgeConfigSpec.ConfigValue<List<? extends String>> itemTagsToRegister;
    public ForgeConfigSpec.ConfigValue<List<? extends String>> recipeTypeNamePatternsToExclude;

    protected CommonConfiguration(final ForgeConfigSpec.Builder builder)
    {

        createCategory(builder,
			  "recipes");
        createCategory(builder,
			  "tags");
        itemTagsToRegister = defineList(
          builder,
          "recipes.tags.tagsToRegister",
          new ImmutableList.Builder<String>()
            .add(
              "minecraft:wool",
			  "minecraft:planks",
			  "minecraft:stone_bricks",
			  "minecraft:wooden_buttons",
			  "minecraft:buttons",
			  "minecraft:carpets",
			  "minecraft:wooden_doors",
			  "minecraft:wooden_stairs",
			  "minecraft:wooden_slabs",
			  "minecraft:wooden_fences",
			  "minecraft:wooden_pressure_plates",
			  "minecraft:wooden_trapdoors",
			  "minecraft:saplings",
			  "minecraft:logs",
			  "minecraft:dark_oak_logs",
			  "minecraft:oak_logs",
			  "minecraft:birch_logs",
			  "minecraft:acacia_logs",
			  "minecraft:jungle_logs",
			  "minecraft:spruce_logs",
			  "minecraft:banners",
			  "minecraft:sand",
			  "minecraft:walls",
			  "minecraft:anvil",
			  "minecraft:leaves",
			  "minecraft:small_flowers",
			  "minecraft:beds",
			  "minecraft:fishes",
			  "minecraft:signs",
			  "minecraft:music_discs",
			  "minecraft:arrows",
			  "forge:bones",
			  "forge:bookshelves",
			  "forge:chests/ender",
			  "forge:chests/trapped",
			  "forge:chests/wooden",
			  "forge:cobblestone",
			  "forge:crops/beetroot",
			  "forge:crops/carrot",
			  "forge:crops/nether_wart",
			  "forge:crops/potato",
			  "forge:crops/wheat",
			  "forge:dusts/prismarine",
			  "forge:dusts/redstone",
			  "forge:dusts/glowstone",
			  "forge:dyes",
			  "forge:dyes/black",
			  "forge:dyes/red",
			  "forge:dyes/green",
			  "forge:dyes/brown",
			  "forge:dyes/blue",
			  "forge:dyes/purple",
			  "forge:dyes/cyan",
			  "forge:dyes/light_gray",
			  "forge:dyes/gray",
			  "forge:dyes/pink",
			  "forge:dyes/lime",
			  "forge:dyes/yellow",
			  "forge:dyes/light_blue",
			  "forge:dyes/magenta",
			  "forge:dyes/orange",
			  "forge:dyes/white",
			  "forge:eggs",
			  "forge:ender_pearls",
			  "forge:feathers",
			  "forge:fence_gates",
			  "forge:fence_gates/wooden",
			  "forge:fences",
			  "forge:fences/nether_brick",
			  "forge:fences/wooden",
			  "forge:gems/diamond",
			  "forge:gems/emerald",
			  "forge:gems/lapis",
			  "forge:gems/prismarine",
			  "forge:gems/quartz",
			  "forge:glass",
			  "forge:glass/black",
			  "forge:glass/blue",
			  "forge:glass/brown",
			  "forge:glass/colorless",
			  "forge:glass/cyan",
			  "forge:glass/gray",
			  "forge:glass/green",
			  "forge:glass/light_blue",
			  "forge:glass/light_gray",
			  "forge:glass/lime",
			  "forge:glass/magenta",
			  "forge:glass/orange",
			  "forge:glass/pink",
			  "forge:glass/purple",
			  "forge:glass/red",
			  "forge:glass/white",
			  "forge:glass/yellow",
			  "forge:glass_panes",
			  "forge:glass_panes/black",
			  "forge:glass_panes/blue",
			  "forge:glass_panes/brown",
			  "forge:glass_panes/colorless",
			  "forge:glass_panes/cyan",
			  "forge:glass_panes/gray",
			  "forge:glass_panes/green",
			  "forge:glass_panes/light_blue",
			  "forge:glass_panes/light_gray",
			  "forge:glass_panes/lime",
			  "forge:glass_panes/magenta",
			  "forge:glass_panes/orange",
			  "forge:glass_panes/pink",
			  "forge:glass_panes/purple",
			  "forge:glass_panes/red",
			  "forge:glass_panes/white",
			  "forge:glass_panes/yellow",
			  "forge:gravel",
			  "forge:gunpowder",
			  "forge:heads",
			  "forge:ingots/brick",
			  "forge:ingots/gold",
			  "forge:ingots/iron",
			  "forge:ingots/nether_brick",
			  "forge:leather",
			  "forge:mushrooms",
			  "forge:nether_stars",
			  "forge:netherrack",
			  "forge:nuggets/gold",
			  "forge:nuggets/iron",
			  "forge:obsidian",
			  "forge:ores/coal",
			  "forge:ores/diamond",
			  "forge:ores/emerald",
			  "forge:ores/gold",
			  "forge:ores/iron",
			  "forge:ores/lapis",
			  "forge:ores/quartz",
			  "forge:ores/redstone",
			  "forge:rods/blaze",
			  "forge:rods/wooden",
			  "forge:sand",
			  "forge:sand/colorless",
			  "forge:sand/red",
			  "forge:sandstone",
			  "forge:seeds",
			  "forge:seeds/beetroot",
			  "forge:seeds/melon",
			  "forge:seeds/pumpkin",
			  "forge:seeds/wheat",
			  "forge:slimeballs",
			  "forge:stained_glass",
			  "forge:stained_glass_panes",
			  "forge:stone",
			  "forge:storage_blocks/coal",
			  "forge:storage_blocks/diamond",
			  "forge:storage_blocks/emerald",
			  "forge:storage_blocks/gold",
			  "forge:storage_blocks/iron",
			  "forge:storage_blocks/lapis",
			  "forge:storage_blocks/quartz",
			  "forge:storage_blocks/redstone",
			  "forge:string"
            )
            .build(),
          o -> {
              final String resLoc = o.toString();
              final String[] parts = resLoc.split(":");
              if (parts.length >= 3 || parts.length == 0)
                  return false;

              if (parts.length == 1) {
                  return ResourceLocation.isValidPath(parts[0]);
              }

              return ResourceLocation.isValidNamespace(parts[0]) && ResourceLocation.isValidPath(parts[1]);
          }
        );
        finishCategory(builder);
        createCategory(builder, "conversion");
        recipeTypeNamePatternsToExclude = defineList(
          builder,
          "recipes.conversion.types.auto-conversion-blacklist",
          new ImmutableList.Builder<String>()
            .build(),
          o -> {
              try {
                  Pattern.compile(o.toString());
                  return true;
              } catch (PatternSyntaxException pse) {
                  return false;
              }
          }
        );
        finishCategory(builder);
    }
}