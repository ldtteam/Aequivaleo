package com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency;

import com.ldtteam.aequivaleo.api.recipe.equivalency.IEquivalencyRecipe;
import net.minecraft.tags.Tag;

/**
 * Represents an equivalency recipe that comes from a tag.
 *
 * @param <T> The type for the tag.
 * @see Tag
 */
public interface ITagEquivalencyRecipe<T> extends IEquivalencyRecipe
{

    /**
     * The tag that determines the equivalency.
     *
     * @return The tag.
     */
    Tag.Named<T> getTag();
}
