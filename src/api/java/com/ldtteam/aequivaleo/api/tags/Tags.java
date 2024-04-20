package com.ldtteam.aequivaleo.api.tags;

import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Class to hold all the tags used by the mod.
 */
public class Tags {

    /**
     * Initialize the tags.
     */
    public static void init() {
        Items.init();
    }

    /**
     * Class to hold all the item tags.
     */
    public static class Items {
        private static void init() {}

        /**
         * The tag for the color cycle items.
         * If an item is in this tag, it will be blocked from the color cycle.
         */
        public static final TagKey<Item> BLOCKED_COLOR_CYCLE = tag("compatibility/blocked/color_cycle");

        private static TagKey<Item> tag(String name)
        {
            return ItemTags.create(new ResourceLocation(Constants.MOD_ID, name));
        }
    }
}
