package com.ldtteam.aequivaleo.api.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Comparator;

public final class Comparators
{

    private Comparators()
    {
        throw new IllegalStateException("Tried to initialize: Comparators but this is a Utility class.");
    }

    public static final Comparator<ItemStack> ID_COMPARATOR = (itemStack1, itemStack2) -> {

        if (itemStack1 != null && itemStack2 != null) {
            if (!ItemStackUtils.isEmpty(itemStack1)  && !ItemStackUtils.isEmpty(itemStack2)) {
                // Sort on id
                if (Item.getIdFromItem(itemStack1.getItem()) - Item.getIdFromItem(itemStack2.getItem()) == 0) {
                    // Sort on item
                    if (itemStack1.getItem() == itemStack2.getItem()) {
                        // Then sort on meta
                        if ((itemStack1.getDamage() == itemStack2.getDamage())) {
                            // Then sort on NBT
                            if (itemStack1.hasTag() && itemStack2.hasTag()) {
                                // Then sort on stack size
                                if (ItemStack.areItemStackTagsEqual(itemStack1, itemStack2)) {
                                    return (itemStack1.getCount() - itemStack2.getCount());
                                }
                                else {
                                    return itemStack1.getTag().toString().compareTo(itemStack2.getTag().toString());
                                }
                            }
                            else if (!(itemStack1.hasTag()) && itemStack2.hasTag()) {
                                return -1;
                            }
                            else if (itemStack1.hasTag() && !(itemStack2.hasTag())) {
                                return 1;
                            }
                            else {
                                return (itemStack1.getCount() - itemStack2.getCount());
                            }
                        }
                        else {
                            return (itemStack1.getDamage() - itemStack2.getDamage());
                        }
                    }
                    else {
                        return itemStack1.getItem().getTranslationKey(itemStack1).compareToIgnoreCase(itemStack2.getItem().getTranslationKey(itemStack2));
                    }
                }
                else {
                    return Item.getIdFromItem(itemStack1.getItem()) - Item.getIdFromItem(itemStack2.getItem());
                }
            }
            else {
                return 0;
            }
        }
        else if (itemStack1 != null) {
            return -1;
        }
        else if (itemStack2 != null) {
            return 1;
        }
        else {
            return 0;
        }
    };

}
