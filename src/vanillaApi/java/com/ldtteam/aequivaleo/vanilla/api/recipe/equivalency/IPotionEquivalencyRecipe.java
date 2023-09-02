package com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency;

import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;

/**
 * Represents an equivalency recipe that comes from brewing a potion.
 */
public interface IPotionEquivalencyRecipe extends IEquivalencyRecipe
{
    /**
     * The tag that determines the equivalency.
     *
     * @return The tag.
     */
    Potion getPotion();

    /**
     * The item used for potion container creation.
     *
     * @return The item used to create the potion container.
     * @see Items#POTION
     * @see Items#LINGERING_POTION
     * @see Items#SPLASH_POTION
     */
    Item getContainerItem();
}
