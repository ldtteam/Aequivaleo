package com.ldtteam.aequivaleo.api.tags;

import com.ldtteam.aequivaleo.api.util.TagUtils;
import net.minecraft.tags.*;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Registry that allows for the registration of tags that can be used during analysis.
 */
public interface ITagEquivalencyRegistry
{
    /**
     * Adds a given tag to the registry.
     * Allowing the tag to be used by the analysis engine during analysis, and marks all game objects included in the tag to be equal.
     *
     * @param tag The tag that indicates that a given set of game objects contained in the tag are equal to one another.
     * @return The registry with the tag added for analysis.
     */
    ITagEquivalencyRegistry addTag(@NotNull final ITag.INamedTag<?> tag);

    /**
     * Adds a given tag to the registry, via its name.
     * Allowing the tag to be used by the analysis engine during analysis, and marks all game objects included in the tag to be equal.
     *
     * @param tagName The name of the tag that indicates that a given set of game objects contained in the tag are equal to one another.
     * @return The registry with the tag added for analysis.
     */
    default ITagEquivalencyRegistry addTag(@NotNull final ResourceLocation tagName) {
        TagUtils.get(tagName).forEach(this::addTag);
        return this;
    }
}
