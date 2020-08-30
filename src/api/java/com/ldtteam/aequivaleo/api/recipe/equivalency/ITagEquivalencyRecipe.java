package com.ldtteam.aequivaleo.api.recipe.equivalency;

import net.minecraft.tags.ITag;

/**
 * Represents an equivalency recipe that comes from a tag.
 *
 * @param <T> The type for the tag.
 * @see ITag
 */
public interface ITagEquivalencyRecipe<T> extends IEquivalencyRecipe
{

    /**
     * The tag that determines the equivalency.
     *
     * @return The tag.
     */
    ITag.INamedTag<T> getTag();
}
