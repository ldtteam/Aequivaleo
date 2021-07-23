package com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.StaticTags;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TagIngredient implements IRecipeIngredient
{

    private final ResourceLocation tagType;
    private final ResourceLocation tagName;
    private final SortedSet<ICompoundContainer<?>> containers;
    private final double count;

    public TagIngredient(final ResourceLocation tagType, final ResourceLocation tagName, final double count) {
        this.tagType = tagType;
        this.tagName = tagName;
        this.count = count;

        this.containers = new TreeSet<>();

        Optional.ofNullable(StaticTags.get(tagType))
          .map(StaticTagHelper::getAllTags)
          .map(TagCollection::getAllTags)
          .filter(c -> c.containsKey(tagName))
          .ifPresent(t -> addContainersFrom(t.get(tagName)));
    }

    private void addContainersFrom(final Tag<?> tag)
    {
        this.containers.addAll(
          tag.getValues().stream().map(e -> IAequivaleoAPI.getInstance().getCompoundContainerFactoryManager().wrapInContainer(e, 1)).collect(Collectors.toSet())
        );
    }

    @Override
    public SortedSet<ICompoundContainer<?>> getCandidates()
    {
        return containers;
    }

    @Override
    public Double getRequiredCount()
    {
        return count;
    }

    public ResourceLocation getTagType()
    {
        return tagType;
    }

    public ResourceLocation getTagName()
    {
        return tagName;
    }
}
