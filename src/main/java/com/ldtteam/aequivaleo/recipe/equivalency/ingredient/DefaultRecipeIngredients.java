package com.ldtteam.aequivaleo.recipe.equivalency.ingredient;

import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IDefaultRecipeIngredients;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.ISimpleRecipeIngredientBuilder;
import net.minecraft.tags.TagKey;

import java.util.function.Consumer;

public class DefaultRecipeIngredients implements IDefaultRecipeIngredients {
    
    private static final DefaultRecipeIngredients INSTANCE = new DefaultRecipeIngredients();
    
    public static DefaultRecipeIngredients getInstance() {
        return INSTANCE;
    }
    
    private DefaultRecipeIngredients() {
    }
    
    @Override
    public IRecipeIngredient from(ICompoundContainer<?> source) {
        return new SimpleIngredientBuilder().from(source).createIngredient();
    }
    
    @Override
    public IRecipeIngredient from(Object source, double count) {
        return new SimpleIngredientBuilder().from(
                ICompoundContainer.from(source)
        ).withCount(count).createIngredient();
    }
    
    @Override
    public IRecipeIngredient tagged(TagKey<?> key, double count) {
        return new SimpleIngredientBuilder().from(ICompoundContainer.from(key)).withCount(count).createIngredient();
    }
    
    @Override
    public IRecipeIngredient from(Consumer<ISimpleRecipeIngredientBuilder> ingredientBuilderConsumer) {
        final SimpleIngredientBuilder builder = new SimpleIngredientBuilder();
        ingredientBuilderConsumer.accept(builder);
        return builder.createIngredient();
    }
}
