package com.ldtteam.aequivaleo.api.util;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraftforge.common.ForgeTagHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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
        return switch (collectionId.toString())
                 {
                     case "minecraft:block" -> BlockTags.getAllTags();
                     case "minecraft:item" -> ItemTags.getAllTags();
                     case "minecraft:entity_type" -> EntityTypeTags.getAllTags();
                     case "minecraft:fluid" -> FluidTags.getAllTags();
                     default -> SerializationTags.getInstance().getOrEmpty(ResourceKey.createRegistryKey(collectionId));
                 };
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

        final Optional<ResourceLocation> targetedCustomRegistry = ForgeTagHandler.getCustomTagTypeNames().stream()
          .map(ResourceKey::createRegistryKey)
          .filter(registryName -> isContainedIn(SerializationTags.getInstance().getOrEmpty(registryName), tag))
          .map(ResourceKey::location)
          .findFirst();

        if (targetedCustomRegistry.isPresent())
            return targetedCustomRegistry.get();

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

    public static boolean areTagsEqual(final Tag<?> leftTag, final Tag<?> rightTag) {
        try {
            return leftTag.getValues().stream().allMatch(element -> isContainedInTag(element, rightTag)) &&
                     rightTag.getValues().stream().allMatch(element -> isContainedInTag(element, leftTag));
        } catch (Exception ex) {
            return false; //Likely tags are not bound yet. Just bail out.
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> boolean  isContainedInTag(final Object candidate, final Tag<T> tag) {
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
