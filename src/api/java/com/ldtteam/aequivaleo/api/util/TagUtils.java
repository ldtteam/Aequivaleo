package com.ldtteam.aequivaleo.api.util;

import com.google.common.collect.Lists;
import net.minecraft.tags.*;
import net.minecraft.util.ResourceLocation;
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

    public static List<ITag.INamedTag<?>> get(@NotNull final ResourceLocation name)
    {
        final List<ITag.INamedTag<?>> result = Lists.newArrayList();

        getTag(BlockTags.getCollection(), name).ifPresent(result::add);
        getTag(ItemTags.getCollection(), name).ifPresent(result::add);
        getTag(EntityTypeTags.getCollection(), name).ifPresent(result::add);
        getTag(FluidTags.getCollection(), name).ifPresent(result::add);

        return result;
    }

    public static ITagCollection<?> getTagCollection(final ResourceLocation collectionId)
    {
        switch (collectionId.toString())
        {
            case "minecraft:block":
                return TagCollectionManager.getManager().getBlockTags();
            case "minecraft:item":
                return TagCollectionManager.getManager().getItemTags();
            case "minecraft:entity_type":
                return TagCollectionManager.getManager().getEntityTypeTags();
            case "minecraft:fluid":
                return TagCollectionManager.getManager().getFluidTags();
            default:
                return TagCollectionManager.getManager().getCustomTypeCollection(collectionId);
        }
    }

    public static ResourceLocation getTagCollectionName(final ITag.INamedTag<?> tag)
    {
        if (isContainedIn(TagCollectionManager.getManager().getBlockTags(), tag))
            return new ResourceLocation("block");

        if (isContainedIn(TagCollectionManager.getManager().getItemTags(), tag))
            return new ResourceLocation("item");

        if (isContainedIn(TagCollectionManager.getManager().getFluidTags(), tag))
            return new ResourceLocation("fluid");

        if (isContainedIn(TagCollectionManager.getManager().getEntityTypeTags(), tag))
            return new ResourceLocation("entity_type");

        for (final Map.Entry<ResourceLocation, ITagCollection<?>> resourceLocationITagCollectionEntry : TagCollectionManager.getManager().getCustomTagTypes().entrySet())
        {
            if (isContainedIn(
              resourceLocationITagCollectionEntry.getValue(),
              tag
            ))
                return resourceLocationITagCollectionEntry.getKey();
        }

        throw new IllegalArgumentException("Could not find the collection for the tag: " + tag.getName());
    }

    private static boolean isContainedIn(final ITagCollection<?> collection, final ITag.INamedTag<?> tag)
    {
        return collection.getIDTagMap()
          .entrySet()
          .stream()
          .filter(e -> e.getKey().equals(tag.getName()))
          .anyMatch(e -> areTagsEqual(e.getValue(), tag));
    }

    public static boolean areTagsEqual(final ITag<?> leftTag, final ITag<?> rightTag) {
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

    public static Optional<ITag.INamedTag<?>> getTag(final ResourceLocation tagCollectionName, final ResourceLocation location) {
        return Optional.ofNullable(getTagCollection(tagCollectionName).getIDTagMap().get(location))
                 .filter(ITag.INamedTag.class::isInstance)
                 .map(tag -> (ITag.INamedTag<?>) tag);
    }

    public static <T> Optional<ITag.INamedTag<T>> getTag(final ITagCollection<T> tTagCollection, final ResourceLocation location) {
        return Optional.ofNullable(tTagCollection.getIDTagMap().get(location))
                 .filter(ITag.INamedTag.class::isInstance)
                 .map(tag -> (ITag.INamedTag<T>) tag);
    }
}
