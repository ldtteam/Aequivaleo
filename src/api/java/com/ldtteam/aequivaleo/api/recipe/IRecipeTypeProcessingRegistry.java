package com.ldtteam.aequivaleo.api.recipe;

import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

import java.util.Set;

/**
 * Registry used to register recipes types from vanilla to be
 * processed in a given way.
 *
 * All ways of processing a given recipe are handled by plugin.
 * See the vanilla plugin for an example.
 */
public interface IRecipeTypeProcessingRegistry
{

    static IRecipeTypeProcessingRegistry getInstance() {
        return IAequivaleoAPI.getInstance().getRecipeTypeProcessingRegistry();
    }

    /**
     * Used to register a given set of vanilla recipe types as a type of recipe.
     * And as such defines the way the recipe type is processed.
     *
     * @param type The type of recipe, as in how to process it.
     * @param vanillaTypes The vanilla recipe types that should be processed in a given way.
     * @return The registry.
     */
    IRecipeTypeProcessingRegistry registerAs(final ResourceLocation type, final IRecipeType<?>... vanillaTypes);

    /**
     * Returns all registered vanilla recipe types which should be processed by the given type.
     *
     * @param type The type.
     * @return All registered vanilla recipe.
     */
    Set<IRecipeType<?>> getRecipeTypesToBeProcessedAs(final ResourceLocation type);

    /**
     * Returns all known types, which have registrations for special handling, from the registry.
     *
     * @return All registered minecraft recipe types.
     */
    Set<IRecipeType<?>> getAllKnownTypes();
}
