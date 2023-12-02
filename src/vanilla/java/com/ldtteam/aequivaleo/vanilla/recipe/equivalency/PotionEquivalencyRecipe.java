package com.ldtteam.aequivaleo.vanilla.recipe.equivalency;

import com.ldtteam.aequivaleo.api.compound.container.registry.ICompoundContainerFactoryManager;
import com.ldtteam.aequivaleo.api.recipe.equivalency.GenericRecipeEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.vanilla.api.recipe.equivalency.IPotionEquivalencyRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PotionEquivalencyRecipe extends GenericRecipeEquivalencyRecipe implements IPotionEquivalencyRecipe {

    private final Potion potion;
    private final Item containerItem;

    public PotionEquivalencyRecipe(
            ItemStack input,
            ItemStack reagent,
            ItemStack output
    ) {
        super(
                new ResourceLocation(
                        Objects.requireNonNull(BuiltInRegistries.POTION.getKey(PotionUtils.getPotion(output))).toString().replace(":", "_") + "/in/" + Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(output.getItem())).toString().replace(":", "_")
                ),
                Stream.of(input, reagent)
                        .map(IRecipeIngredient::from).collect(Collectors.toSet()),
                Stream.of(input, reagent).map(ItemStack::getCraftingRemainingItem).filter(Predicate.not(ItemStack::isEmpty))
                        .map(ICompoundContainerFactoryManager.getInstance()::wrapInContainer).collect(Collectors.toSet()),
                Set.of(ICompoundContainerFactoryManager.getInstance().wrapInContainer(output))
        );
        this.potion = PotionUtils.getPotion(output);
        this.containerItem = output.getItem();
    }

    @Override
    public Potion getPotion() {
        return potion;
    }

    @Override
    public Item getContainerItem() {
        return containerItem;
    }
}
