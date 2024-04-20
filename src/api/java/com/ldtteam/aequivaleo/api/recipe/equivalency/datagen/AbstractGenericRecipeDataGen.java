package com.ldtteam.aequivaleo.api.recipe.equivalency.datagen;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.container.ICompoundContainer;
import com.ldtteam.aequivaleo.api.recipe.equivalency.IGenericRecipeEquivalencyRecipe;
import com.ldtteam.aequivaleo.api.recipe.equivalency.data.GenericRecipeData;
import com.ldtteam.aequivaleo.api.recipe.equivalency.data.GenericRecipeDataBuilder;
import com.ldtteam.aequivaleo.api.recipe.equivalency.ingredient.IRecipeIngredient;
import com.ldtteam.aequivaleo.api.util.Constants;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Defines a recipe data generator that can be used to generate recipe data for the aequivaleo only.
 * <p>
 *     This generator should be used when you only want recipes to show up during aequivaleo's analysis
 *     but not in the game itself. Allowing you to alter the way information flows through the system,
 *     without altering the game itself.
 * </p>
 * <p>
 *     The recipes are stored as a {@link GenericRecipeData} object, which is a simple object that contains
 *     the inputs, required known outputs and outputs of a recipe.
 * </p>
 */
@SuppressWarnings("SameParameterValue")
public abstract class AbstractGenericRecipeDataGen implements DataProvider
{
    private final DataGenerator dataGenerator;
    private final WorldData generalData = new WorldData(new ResourceLocation(Constants.MOD_ID, "general")) {
        @Override
        public String getPath()
        {
            return "general";
        }
    };
    private final Map<ResourceLocation, WorldData> worldDataMap = Maps.newHashMap();

    /**
     * Creates a new recipe data generator.
     *
     * @param dataGenerator The data generator to use.
     */
    protected AbstractGenericRecipeDataGen(final DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    /**
     * Runs the generator using the given cache.
     *
     * @param cache The cache to use.
     * @return A future that completes when the generator is done.
     */
    @Override
    public CompletableFuture<?> run(@NotNull final CachedOutput cache)
    {
        this.calculateDataToSave();

        final Gson gson = IAequivaleoAPI.getInstance().getGson(ICondition.IContext.EMPTY);

        final List<CompletableFuture<?>> futures = new ArrayList<>();

        futures.add(this.writeData(
          cache,
          gson,
          generalData
        ));

        for (WorldData worldData : worldDataMap.values())
        {
            futures.add(this.writeData(
              cache,
              gson,
              worldData
            ));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<?> writeData(
      final CachedOutput cache,
      final Gson gson,
      final WorldData worldData
    )
    {
        final List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Map.Entry<ResourceLocation, GenericRecipeData> entry : worldData.getRecipes().entrySet())
        {
            ResourceLocation name = entry.getKey();

            final Path itemPath = dataGenerator.getPackOutput().getOutputFolder().resolve(String.format("data/%s/aequivaleo/recipes/%s", name.getNamespace(), worldData.getPath())).resolve(String.format("%s.json", name.getPath()));

            futures.add(DataProvider.saveStable(
              cache,
              gson.toJsonTree(entry.getValue()),
              itemPath
            ));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Invoked so that an implementer of this class can calculate the data to save.
     */
    protected abstract void calculateDataToSave();

    /**
     * Adds a recipe to the generator.
     *
     * @param worldId The id of the world to add the recipe to.
     * @param name The name of the recipe.
     * @param inputs The inputs of the recipe.
     * @param outputs The outputs of the recipe.
     */
    protected void saveData(
      final ResourceLocation worldId,
      final ResourceLocation name,
      final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> outputs) {
        this.saveData(
          worldId,
          name,
          inputs,
          Collections.emptySet(),
          outputs
        );
    }
/**
     * Adds a recipe to the generator.
     *
     * @param worldId The id of the world to add the recipe to.
     * @param recipeEquivalencyRecipe The recipe to add.
     */
    protected void saveData(
      final ResourceLocation worldId,
      final IGenericRecipeEquivalencyRecipe recipeEquivalencyRecipe
    ) {
        this.saveData(
          worldId,
          recipeEquivalencyRecipe.getRecipeName(),
          recipeEquivalencyRecipe.getInputs(),
          recipeEquivalencyRecipe.getRequiredKnownOutputs(),
          recipeEquivalencyRecipe.getOutputs()
        );
    }

    /**
     * Adds a recipe to the generator.
     *
     * @param worldId The id of the world to add the recipe to.
     * @param name The name of the recipe.
     * @param inputs The inputs of the recipe.
     * @param requiredKnownOutputs The required known outputs of the recipe.
     * @param outputs The outputs of the recipe.
     */
    protected void saveData(
      final ResourceLocation worldId,
      final ResourceLocation name,
      final Set<IRecipeIngredient> inputs,
      final Set<ICompoundContainer<?>> requiredKnownOutputs,
      final Set<ICompoundContainer<?>> outputs) {
        this.saveData(
          worldId,
          name,
          inputs,
          requiredKnownOutputs,
          outputs,
          Sets.newHashSet()
        );
    }

    /**
     * Adds a recipe to the generator.
     *
     * @param worldId The id of the world to add the recipe to.
     * @param name The name of the recipe.
     * @param inputs The inputs of the recipe.
     * @param requiredKnownOutputs The required known outputs of the recipe.
     * @param outputs The outputs of the recipe.
     * @param conditions The conditions of the recipe.
     */
    protected void saveData(
            final ResourceLocation worldId,
            final ResourceLocation name,
            final Set<IRecipeIngredient> inputs,
            final Set<ICompoundContainer<?>> requiredKnownOutputs,
            final Set<ICompoundContainer<?>> outputs,
            final Set<ICondition> conditions) {
        this.saveData(
                worldId,
                name,
                new GenericRecipeDataBuilder()
                        .setInputs(inputs)
                        .setRequiredKnownOutputs(requiredKnownOutputs)
                        .setOutputs(outputs)
                        .setConditions(conditions)
        );
    }

    /**
     * Adds a recipe to the generator.
     *
     * @param worldId The id of the world to add the recipe to.
     * @param name The name of the recipe.
     * @param builder The builder to create the recipe data with.
     */
    protected void saveData(
            final ResourceLocation worldId,
            final ResourceLocation name,
            final GenericRecipeDataBuilder builder) {
        this.worldDataMap.computeIfAbsent(worldId, WorldData::new).recipes.put(name,
          builder
            .createGenericRecipeData());
    }

    /**
     * Adds a recipe to the generator.
     *
     * @param name The name of the recipe.
     * @param inputs The inputs of the recipe.
     * @param outputs The outputs of the recipe.
     */
    protected void saveData(
            final ResourceLocation name,
            final Set<IRecipeIngredient> inputs,
            final Set<ICompoundContainer<?>> outputs) {
        this.saveData(
                name,
                inputs,
                Collections.emptySet(),
                outputs
        );
    }

    /**
     * Adds a recipe to the generator.
     *
     * @param recipeEquivalencyRecipe The recipe to add.
     */
    protected void saveData(
            final IGenericRecipeEquivalencyRecipe recipeEquivalencyRecipe
    ) {
        this.saveData(
                recipeEquivalencyRecipe.getRecipeName(),
                recipeEquivalencyRecipe.getInputs(),
                recipeEquivalencyRecipe.getRequiredKnownOutputs(),
                recipeEquivalencyRecipe.getOutputs()
        );
    }

    /**
     * Adds a recipe to the generator.
     *
     * @param name The name of the recipe.
     * @param inputs The inputs of the recipe.
     * @param requiredKnownOutputs The required known outputs of the recipe.
     * @param outputs The outputs of the recipe.
     */
    protected void saveData(
            final ResourceLocation name,
            final Set<IRecipeIngredient> inputs,
            final Set<ICompoundContainer<?>> requiredKnownOutputs,
            final Set<ICompoundContainer<?>> outputs) {
        this.saveData(
                name,
                inputs,
                requiredKnownOutputs,
                outputs,
                Sets.newHashSet()
        );
    }

    /**
     * Adds a recipe to the generator.
     *
     * @param name The name of the recipe.
     * @param inputs The inputs of the recipe.
     * @param requiredKnownOutputs The required known outputs of the recipe.
     * @param outputs The outputs of the recipe.
     * @param conditions The conditions of the recipe.
     */
    
    protected void saveData(
            final ResourceLocation name,
            final Set<IRecipeIngredient> inputs,
            final Set<ICompoundContainer<?>> requiredKnownOutputs,
            final Set<ICompoundContainer<?>> outputs,
            final Set<ICondition> conditions) {
        this.saveData(
                name,
                new GenericRecipeDataBuilder()
                        .setInputs(inputs)
                        .setRequiredKnownOutputs(requiredKnownOutputs)
                        .setOutputs(outputs)
                        .setConditions(conditions)
        );
    }

    /**
     * Adds a recipe to the generator.
     *
     * @param name The name of the recipe.
     * @param builder The builder to create the recipe data with.
     */
    protected void saveData(
            final ResourceLocation name,
            final GenericRecipeDataBuilder builder
    ) {
        generalData.recipes.put(name,
          builder
            .createGenericRecipeData());
    }

    private static class WorldData {
        private final ResourceLocation                         worldId;
        private final Map<ResourceLocation, GenericRecipeData> recipes = Maps.newHashMap();

        private WorldData(final ResourceLocation worldId) {this.worldId = worldId;}

        public ResourceLocation getWorldId()
        {
            return worldId;
        }

        public Map<ResourceLocation, GenericRecipeData> getRecipes()
        {
            return recipes;
        }

        public String getPath() {
            return worldId.getNamespace() + "/" + worldId.getPath();
        }
    }

}
