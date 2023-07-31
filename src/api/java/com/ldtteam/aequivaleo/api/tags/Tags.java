package com.ldtteam.aequivaleo.api.tags;

import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class Tags {

    public static void init() {
        Items.init();
    }

    public static class Items {
        private static void init() {}

        public static final TagKey<Item> BLOCKED_COLOR_CYCLE = tag("compatibility/blocked/color_cycle");

        private static TagKey<Item> tag(String name)
        {
            return ItemTags.create(new ResourceLocation(Constants.MOD_ID, name));
        }
    }
}
