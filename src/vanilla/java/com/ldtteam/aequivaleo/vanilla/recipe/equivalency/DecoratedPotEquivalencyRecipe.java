package com.ldtteam.aequivaleo.vanilla.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.recipe.equivalency.GenericRecipeEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.SimpleIngredientBuilder;
import com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency.IDecoratedPotEquivalencyRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.DecoratedPotRecipe;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DecoratedPotEquivalencyRecipe extends GenericRecipeEquivalencyRecipe implements IDecoratedPotEquivalencyRecipe {

    private static final String RECIPE_NAME_TEMPLATE = "decorated_pot_%s";

    public DecoratedPotEquivalencyRecipe(final ServerLevel world, final DecoratedPotBlockEntity.Decorations decorations) {
        super(toRecipeName(world, decorations), toIngredients(decorations), Set.of(), toOutput(decorations));
    }

    private static ResourceLocation toRecipeName(final ServerLevel world, final DecoratedPotBlockEntity.Decorations decorations) {
        return new ResourceLocation(String.format(RECIPE_NAME_TEMPLATE, decorations.m_284195_()
                .map(item -> world.registryAccess().registryOrThrow(Registries.ITEM).getKey(item))
                .filter(Objects::nonNull)
                .map(ResourceLocation::toString)
                .map(name -> name.replace(":", "_"))
                .collect(Collectors.joining("_"))));
    }

    private static Set<IRecipeIngredient> toIngredients(DecoratedPotBlockEntity.Decorations decorations) {
        return decorations.m_284195_()
                .map(item -> ICompoundContainerFactoryManager.getInstance().wrapInContainer(item.getDefaultInstance(), 1d))
                .map(container -> new SimpleIngredientBuilder().from(container).createIngredient())
                .collect(Collectors.toSet());
    }

    private static Set<ICompoundContainer<?>> toOutput(DecoratedPotBlockEntity.Decorations decorations) {
        ItemStack itemstack = DecoratedPotRecipe.m_284234_(decorations);
        return Set.of(ICompoundContainerFactoryManager.getInstance().wrapInContainer(itemstack, 1d));
    }
}
