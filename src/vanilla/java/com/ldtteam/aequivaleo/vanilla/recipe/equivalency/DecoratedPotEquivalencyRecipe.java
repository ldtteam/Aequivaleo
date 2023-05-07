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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DecoratedPotEquivalencyRecipe extends GenericRecipeEquivalencyRecipe implements IDecoratedPotEquivalencyRecipe {

    private static final String RECIPE_NAME_TEMPLATE = "decorated_pot_%s";

    public DecoratedPotEquivalencyRecipe(final ServerLevel world, final Item... inputs) {
        super(toRecipeName(world, inputs), toIngredients(inputs), Set.of(), toOutput(inputs));
    }

    private static ResourceLocation toRecipeName(final ServerLevel world, final Item... inputs) {
        return new ResourceLocation(String.format(RECIPE_NAME_TEMPLATE, Arrays.stream(inputs)
                .map(item -> world.registryAccess().registryOrThrow(Registries.ITEM).getKey(item))
                .filter(Objects::nonNull)
                .map(ResourceLocation::toString)
                .map(name -> name.replace(":", "_"))
                .collect(Collectors.joining("_"))));
    }

    private static Set<IRecipeIngredient> toIngredients(Item... inputs) {
        return Arrays.stream(inputs)
                .map(item -> ICompoundContainerFactoryManager.getInstance().wrapInContainer(item.getDefaultInstance(), 1d))
                .map(container -> new SimpleIngredientBuilder().from(container).createIngredient())
                .collect(Collectors.toSet());
    }

    private static Set<ICompoundContainer<?>> toOutput(Item... inputs) {
        if (inputs.length != 4)
            throw new IllegalArgumentException("DecoratedPotEquivalencyRecipe must have 4 inputs");

        ItemStack itemstack = Items.DECORATED_POT.getDefaultInstance();
        CompoundTag compoundtag = new CompoundTag();
        DecoratedPotBlockEntity.saveShards(List.of(inputs), compoundtag);
        BlockItem.setBlockEntityData(itemstack, BlockEntityType.DECORATED_POT, compoundtag);

        return Set.of(ICompoundContainerFactoryManager.getInstance().wrapInContainer(itemstack, 1d));
    }
}
