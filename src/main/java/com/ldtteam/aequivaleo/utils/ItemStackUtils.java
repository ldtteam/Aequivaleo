package com.ldtteam.aequivaleo.utils;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public class ItemStackUtils {
    
    private static Logger LOGGER = LogUtils.getLogger();
    
    private ItemStackUtils() {
        throw new IllegalStateException("Tried to create utility class!");
    }
    
    public static ItemStack copyWithJsonOps(ItemStack stack) {
        return ItemStack.CODEC.decode(
            JsonOps.INSTANCE,
            ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack).getOrThrow(false, LOGGER::error)
        ).getOrThrow(false, LOGGER::error).getFirst();
    }
}
