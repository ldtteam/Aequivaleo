package com.ldtteam.aequivaleo.api.util;

import com.google.common.collect.Lists;
import net.minecraft.tags.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TagUtils
{

    private TagUtils()
    {
        throw new IllegalStateException("Tried to initialize: TagUtils but this is a Utility class.");
    }

    public static List<Tag.Named<?>> get(@NotNull final ResourceLocation name)
    {
        final List<Tag.Named<?>> result = Lists.newArrayList();

        getTag(BlockTags.getAllTags(), name).ifPresent(result::add);
        getTag(ItemTags.getAllTags(), name).ifPresent(result::add);
        getTag(EntityTypeTags.getAllTags(), name).ifPresent(result::add);
        getTag(FluidTags.getAllTags(), name).ifPresent(result::add);

        return result;
    }

    public static TagCollection<?> getTagCollection(final ResourceLocation collectionId)
    {
        switch (collectionId.toString())
        {
            case "minecraft:block":
                return BlockTags.getAllTags();
            case "minecraft:item":
                return ItemTags.getAllTags();
            case "minecraft:entity_type":
                return EntityTypeTags.getAllTags();
            case "minecraft:fluid":
                return FluidTags.getAllTags();
            default:
                return SerializationTags.getInstance().getCustomTypeCollection(collectionId);
        }
    }

    public static ResourceLocation getTagCollectionName(final Tag.Named<?> tag)
    {
        if (isContainedIn(BlockTags.getAllTags(), tag))
            return new ResourceLocation("block");

        if (isContainedIn(ItemTags.getAllTags(), tag))
            return new ResourceLocation("item");

        if (isContainedIn(FluidTags.getAllTags(), tag))
            return new ResourceLocation("fluid");

        if (isContainedIn(EntityTypeTags.getAllTags(), tag))
            return new ResourceLocation("entity_type");

        for (final Map.Entry<ResourceLocation, TagCollection<?>> resourceLocationITagCollectionEntry : SerializationTags.getInstance().getCustomTagTypes().entrySet())
        {
            if (isContainedIn(
              resourceLocationITagCollectionEntry.getValue(),
              tag
            ))
                return resourceLocationITagCollectionEntry.getKey();
        }

        throw new IllegalArgumentException("Could not find the collection for the tag: " + tag.getName());
    }

    private static boolean isContainedIn(final TagCollection<?> collection, final Tag.Named<?> tag)
    {
        return collection.getAllTags()
          .entrySet()
          .stream()
          .filter(e -> e.getKey().equals(tag.getName()))
          .anyMatch(e -> areTagsEqual(e.getValue(), tag));
    }

    private static boolean areTagsEqual(final ITag<?> leftTag, final ITag<?> rightTag) {
        try {
            return leftTag.getAllElements().stream().allMatch(element -> isContainedInTag(element, rightTag)) &&
                     rightTag.getAllElements().stream().allMatch(element -> isContainedInTag(element, leftTag));
        } catch (Exception ex) {
            return false; //Likely tags are not bound yet. Just bail out.
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> boolean  isContainedInTag(final Object candidate, final ITag<T> tag) {
        try {
            final T tCandidate = (T) candidate;
            return tag.contains(tCandidate);
        } catch(ClassCastException e) {
            return false; //Obviously we are not contained in the tag if we can not be casted to the tags type.
        }
    }

    public static Optional<Tag.Named<?>> getTag(final ResourceLocation tagCollectionName, final ResourceLocation location) {
        return Optional.ofNullable(getTagCollection(tagCollectionName).getAllTags().get(location))
                 .filter(Tag.Named.class::isInstance)
                 .map(tag -> (Tag.Named<?>) tag);
    }

    public static <T> Optional<Tag.Named<T>> getTag(final TagCollection<T> tTagCollection, final ResourceLocation location) {
        return Optional.ofNullable(tTagCollection.getAllTags().get(location))
                 .filter(Tag.Named.class::isInstance)
                 .map(tag -> (Tag.Named<T>) tag);
    }
}
