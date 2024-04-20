package com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient;

import com.google.common.base.Suppliers;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents a tag ingredient.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class TagIngredient implements IRecipeIngredient
{

    private final ResourceKey<? extends Registry<?>>                   registryName;
    private final TagKey<?>                                     tagName;
    private final Supplier<SortedSet<ICompoundContainer<?>>> containers;
    private final double                                     count;

    /**
     * Creates a new tag ingredient.
     *
     * @param registryName The registry name.
     * @param tagName The tag name.
     * @param count The count.
     */
    public TagIngredient(final ResourceLocation registryName, final ResourceLocation tagName, final double count) {
        this.registryName = (ResourceKey<Registry<?>>) (ResourceKey) ResourceKey.createRegistryKey(registryName);
        this.tagName = TagKey.create((ResourceKey<? extends Registry<Object>>) this.registryName, tagName);
        this.count = count;

        this.containers = Suppliers.memoize(() -> ServerLifecycleHooks.getCurrentServer().registryAccess().registryOrThrow(this.registryName)
                 .getOrCreateTag((TagKey<Object>) this.tagName)
                 .stream()
                 .map(e -> IAequivaleoAPI.getInstance().getCompoundContainerFactoryManager().wrapInContainer(e.value(), 1))
                 .collect(Collectors.toCollection(TreeSet::new)));
    }

    @Override
    public SortedSet<ICompoundContainer<?>> getCandidates()
    {
        return containers.get();
    }

    @Override
    public Double getRequiredCount()
    {
        return count;
    }

    /**
     * Gets the registry name.
     *
     * @return The registry name.
     */
    public ResourceLocation getRegistryName()
    {
        return registryName.registry();
    }

    /**
     * Gets the tag name.
     *
     * @return The tag name.
     */
    public ResourceLocation getTagName()
    {
        return tagName.location();
    }
}
