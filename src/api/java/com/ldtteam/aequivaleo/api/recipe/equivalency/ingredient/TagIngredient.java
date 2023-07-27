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

@SuppressWarnings({"unchecked", "rawtypes"})
public class TagIngredient implements IRecipeIngredient
{

    private final ResourceKey<? extends Registry<?>>                   registryName;
    private final TagKey<?>                                     tagName;
    private final Supplier<SortedSet<ICompoundContainer<?>>> containers;
    private final double                                     count;

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

    public ResourceLocation getRegistryName()
    {
        return registryName.registry();
    }

    public ResourceLocation getTagName()
    {
        return tagName.location();
    }
}
